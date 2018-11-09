

package developer.elkane.udacity.wizflow;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import developer.elkane.udacity.wizflow.async.UpdateWidgetsTask;
import developer.elkane.udacity.wizflow.async.UpdaterTask;
import developer.elkane.udacity.wizflow.async.bus.PasswordRemovedEvent;
import developer.elkane.udacity.wizflow.async.bus.SwitchFragmentEvent;
import developer.elkane.udacity.wizflow.async.notes.NoteProcessorDelete;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.helpers.NotesHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.ONStyle;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.PasswordHelper;
import developer.elkane.udacity.wizflow.utils.SystemHelper;
import edu.emory.mathcs.backport.java.util.Arrays;


public class MainActivity extends BaseActivity implements OnDateSetListener, OnTimeSetListener {

    private static boolean isPasswordAccepted = false;
    public final String FRAGMENT_DRAWER_TAG = "fragment_drawer";
    public final String FRAGMENT_LIST_TAG = "fragment_list";
    public final String FRAGMENT_DETAIL_TAG = "fragment_detail";
    public final String FRAGMENT_SKETCH_TAG = "fragment_sketch";
    public Uri sketchUri;
    @BindView(R.id.crouton_handle)
    ViewGroup croutonViewContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.WizFlowTheme_Notes);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initUI();

        new UpdaterTask(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPasswordAccepted) {
            init();
        } else {
            checkPassword();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    private void initUI() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    private void checkPassword() {
        if (prefs.getString(Constants.PREF_PASSWORD, null) != null
                && prefs.getBoolean("settings_password_access", false)) {
            PasswordHelper.requestPassword(this, passwordConfirmed -> {
                if (passwordConfirmed) {
                    init();
                } else {
                    finish();
                }
            });
        } else {
            init();
        }
    }


    public void onEvent(PasswordRemovedEvent passwordRemovedEvent) {
        showMessage(R.string.password_successfully_removed, ONStyle.ALERT);
        init();
    }


    private void init() {
        isPasswordAccepted = true;

        getFragmentManagerInstance();

        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManagerInstance()
                .findFragmentById(R.id.navigation_drawer);
        if (mNavigationDrawerFragment == null) {
            FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
            fragmentTransaction.replace(R.id.navigation_drawer, new NavigationDrawerFragment(),
                    FRAGMENT_DRAWER_TAG).commit();
        }

        if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG) == null) {
            FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, new ListFragment(), FRAGMENT_LIST_TAG).commit();
        }

        handleIntents();
    }

    private FragmentManager getFragmentManagerInstance() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        return mFragmentManager;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() == null) {
            intent.setAction(Constants.ACTION_START_APP);
        }
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntents();
        Log.d(Constants.TAG, "onNewIntent");
    }


    public MenuItem getSearchMenuItem() {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            return ((ListFragment) f).getSearchMenuItem();
        } else {
            return null;
        }
    }


    public void editTag(Category tag) {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            ((ListFragment) f).editCategory(tag);
        }
    }


    public void initNotesList(Intent intent) {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            ((ListFragment) f).toggleSearchLabel(false);
            ((ListFragment) f).initNotesList(intent);
        }
    }


    public void commitPending() {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            ((ListFragment) f).commitPending();
        }
    }


    private Fragment checkFragmentInstance(int id, Object instanceClass) {
        Fragment result = null;
        Fragment fragment = getFragmentManagerInstance().findFragmentById(id);
        if (instanceClass.equals(fragment.getClass())) {
            result = fragment;
        }
        return result;
    }


    public void onBackPressed() {

        Fragment f = checkFragmentInstance(R.id.fragment_container, SketchFragment.class);
        if (f != null) {
            ((SketchFragment) f).save();

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

            getFragmentManagerInstance().popBackStack();
            return;
        }

        f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
        if (f != null) {
            ((DetailFragment) f).goBack = true;
            ((DetailFragment) f).saveAndExit((DetailFragment) f);
            return;
        }

        f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            if (prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null &&
                    !getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                getDrawerLayout().openDrawer(GravityCompat.START);
            } else if (!prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null &&
                    getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                getDrawerLayout().closeDrawer(GravityCompat.START);
            } else {
                if (!((ListFragment) f).closeFab()) {
                    isPasswordAccepted = false;
                    super.onBackPressed();
                }
            }
            return;
        }
        super.onBackPressed();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("navigationTmp", navigationTmp);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
    }


    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }


    public ActionBarDrawerToggle getDrawerToggle() {
        if (getFragmentManagerInstance().findFragmentById(R.id.navigation_drawer) != null) {
            return ((NavigationDrawerFragment) getFragmentManagerInstance().findFragmentById(R.id.navigation_drawer)).mDrawerToggle;
        } else {
            return null;
        }
    }


    public void finishActionMode() {
        ListFragment fragment = (ListFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG);
        if (fragment != null) {
            fragment.finishActionMode();
        }
    }


    Toolbar getToolbar() {
        return this.toolbar;
    }


    private void handleIntents() {
        Intent i = getIntent();

        if (i.getAction() == null) return;

        if (Constants.ACTION_RESTART_APP.equals(i.getAction())) {
            SystemHelper.restartApp(getApplicationContext(), MainActivity.class);
        }

        if (receivedIntent(i)) {
            Note note = i.getParcelableExtra(Constants.INTENT_NOTE);
            if (note == null) {
                note = DbHelper.getInstance().getNote(i.getIntExtra(Constants.INTENT_KEY, 0));
            }
            if (note != null && noteAlreadyOpened(note)) {
                return;
            }
            if (note == null) {
                note = new Note();
            }
            switchToDetail(note);
            return;
        }

        if (Constants.ACTION_SEND_AND_EXIT.equals(i.getAction())) {
            saveAndExit(i);
            return;
        }

        if (Intent.ACTION_VIEW.equals(i.getAction())) {
            switchToList();
            return;
        }

        if (Constants.ACTION_SHORTCUT_WIDGET.equals(i.getAction())) {
            switchToDetail(new Note());
            return;
        }
    }


    private void saveAndExit(Intent i) {
        Note note = new Note();
        note.setTitle(i.getStringExtra(Intent.EXTRA_SUBJECT));
        note.setContent(i.getStringExtra(Intent.EXTRA_TEXT));
        DbHelper.getInstance().updateNote(note, true);
        showToast(getString(R.string.note_updated), Toast.LENGTH_SHORT);
        finish();
    }


    private boolean receivedIntent(Intent i) {
        return Constants.ACTION_SHORTCUT.equals(i.getAction())
                || Constants.ACTION_NOTIFICATION_CLICK.equals(i.getAction())
                || Constants.ACTION_WIDGET.equals(i.getAction())
                || Constants.ACTION_WIDGET_TAKE_PHOTO.equals(i.getAction())
                || ((Intent.ACTION_SEND.equals(i.getAction())
                || Intent.ACTION_SEND_MULTIPLE.equals(i.getAction())
                || Constants.INTENT_GOOGLE_NOW.equals(i.getAction()))
                && i.getType() != null)
                || i.getAction().contains(Constants.ACTION_NOTIFICATION_CLICK);
    }


    private boolean noteAlreadyOpened(Note note) {
        DetailFragment detailFragment = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        return detailFragment != null && NotesHelper.haveSameId(note, detailFragment.getCurrentNote());
    }


    public void switchToList() {
        FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
        animateTransition(transaction, TRANSITION_HORIZONTAL);
        ListFragment mListFragment = new ListFragment();
        transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
                (FRAGMENT_DETAIL_TAG).commitAllowingStateLoss();
        if (getDrawerToggle() != null) {
            getDrawerToggle().setDrawerIndicatorEnabled(false);
        }
        getFragmentManagerInstance().getFragments();
        EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
    }


    public void switchToDetail(Note note) {
        FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
        animateTransition(transaction, TRANSITION_HORIZONTAL);
        DetailFragment mDetailFragment = new DetailFragment();
        Bundle b = new Bundle();
        b.putParcelable(Constants.INTENT_NOTE, note);
        mDetailFragment.setArguments(b);
        if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG) == null) {
            transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                    .addToBackStack(FRAGMENT_LIST_TAG)
                    .commitAllowingStateLoss();
        } else {
            getFragmentManagerInstance().popBackStackImmediate();
            transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                    .addToBackStack(FRAGMENT_DETAIL_TAG)
                    .commitAllowingStateLoss();
        }
    }


    public void shareNote(Note note) {

        String titleText = note.getTitle();

        String contentText = titleText
                + System.getProperty("line.separator")
                + note.getContent();


        Intent shareIntent = new Intent();
        if (note.getAttachmentsList().size() == 0) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

        } else if (note.getAttachmentsList().size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType(note.getAttachmentsList().get(0).getMime_type());
            shareIntent.putExtra(Intent.EXTRA_STREAM, note.getAttachmentsList().get(0).getUri());

        } else if (note.getAttachmentsList().size() > 1) {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<>();
            HashMap<String, Boolean> mimeTypes = new HashMap<>();
            for (Attachment attachment : note.getAttachmentsList()) {
                uris.add(attachment.getUri());
                mimeTypes.put(attachment.getMime_type(), true);
            }
            if (mimeTypes.size() > 1) {
                shareIntent.setType("*/*");
            } else {
                shareIntent.setType((String) mimeTypes.keySet().toArray()[0]);
            }

            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, contentText);

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_message_chooser)));
    }


    public void deleteNote(Note note) {
        new NoteProcessorDelete(Arrays.asList(new Note[]{note})).process();
        BaseActivity.notifyAppWidgets(this);
        Log.d(Constants.TAG, "Deleted permanently note with id '" + note.get_id() + "'");
    }


    public void updateWidgets() {
        new UpdateWidgetsTask(getApplicationContext())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void showMessage(int messageId, Style style) {
        showMessage(getString(messageId), style);
    }


    public void showMessage(String message, Style style) {
        runOnUiThread(() -> Crouton.makeText(this, message, style, croutonViewContainer).show());
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        DetailFragment f = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        if (f != null && f.isAdded()) {
            f.onTimeSetListener.onTimeSet(view, hourOfDay, minute);
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        DetailFragment f = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        if (f != null && f.isAdded() && f.onDateSetListener != null) {
            f.onDateSetListener.onDateSet(view, year, monthOfYear, dayOfMonth);
        }
    }
}
