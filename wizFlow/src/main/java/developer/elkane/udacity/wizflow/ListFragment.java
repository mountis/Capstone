
package developer.elkane.udacity.wizflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neopixl.pixlui.components.textview.TextView;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import developer.elkane.udacity.wizflow.async.bus.CategoriesUpdatedEvent;
import developer.elkane.udacity.wizflow.async.bus.NavigationUpdatedNavDrawerClosedEvent;
import developer.elkane.udacity.wizflow.async.bus.NotesLoadedEvent;
import developer.elkane.udacity.wizflow.async.bus.NotesMergeEvent;
import developer.elkane.udacity.wizflow.async.bus.PasswordRemovedEvent;
import developer.elkane.udacity.wizflow.async.notes.NoteLoaderTask;
import developer.elkane.udacity.wizflow.async.notes.NoteProcessorArchive;
import developer.elkane.udacity.wizflow.async.notes.NoteProcessorCategorize;
import developer.elkane.udacity.wizflow.async.notes.NoteProcessorDelete;
import developer.elkane.udacity.wizflow.async.notes.NoteProcessorTrash;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.helpers.NotesHelper;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.ONStyle;
import developer.elkane.udacity.wizflow.models.Tag;
import developer.elkane.udacity.wizflow.models.UndoBarController;
import developer.elkane.udacity.wizflow.models.adapters.NavDrawerCategoryAdapter;
import developer.elkane.udacity.wizflow.models.adapters.NoteAdapter;
import developer.elkane.udacity.wizflow.models.holders.NoteViewHolder;
import developer.elkane.udacity.wizflow.models.listeners.OnViewTouchedListener;
import developer.elkane.udacity.wizflow.models.views.Fab;
import developer.elkane.udacity.wizflow.models.views.InterceptorLinearLayout;
import developer.elkane.udacity.wizflow.utils.AnimationsHelper;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.IntentChecker;
import developer.elkane.udacity.wizflow.utils.KeyboardUtils;
import developer.elkane.udacity.wizflow.utils.Navigation;
import developer.elkane.udacity.wizflow.utils.PasswordHelper;
import developer.elkane.udacity.wizflow.utils.ReminderHelper;
import developer.elkane.udacity.wizflow.utils.TagsHelper;
import developer.elkane.udacity.wizflow.utils.TextHelper;
import it.feio.android.pixlui.links.UrlCompleter;
import it.feio.android.simplegallery.util.BitmapUtils;

import static android.support.v4.view.ViewCompat.animate;


public class ListFragment extends BaseFragment implements OnViewTouchedListener, UndoBarController.UndoListener {

    private static final int REQUEST_CODE_CATEGORY = 1;
    private static final int REQUEST_CODE_CATEGORY_NOTES = 2;
    private static final int REQUEST_CODE_ADD_ALARMS = 3;

    @BindView(R.id.list_root)
    InterceptorLinearLayout listRoot;
    @BindView(R.id.list)
    DynamicListView list;
    @BindView(R.id.search_layout)
    View searchLayout;
    @BindView(R.id.search_query)
    android.widget.TextView searchQueryView;
    @BindView(R.id.search_cancel)
    ImageView searchCancel;
    @BindView(R.id.empty_list)
    TextView empyListItem;
    @BindView(R.id.expanded_image)
    ImageView expandedImageView;
    @BindView(R.id.fab)
    View fabView;
    @BindView(R.id.undobar)
    View undoBarView;
    @BindView(R.id.progress_wheel)
    ProgressWheel progress_wheel;
    @BindView(R.id.snackbar_placeholder)
    View snackBarPlaceholder;

    NoteViewHolder noteViewHolder;

    private List<Note> selectedNotes = new ArrayList<>();
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private Menu menu;
    private AnimationDrawable jinglesAnimation;
    private int listViewPosition;
    private int listViewPositionOffset = 16;
    private boolean sendToArchive;
    private SharedPreferences prefs;
    private ListFragment mFragment;
    private android.support.v7.view.ActionMode actionMode;
    private boolean keepActionMode = false;

    private boolean undoTrash = false;
    private boolean undoArchive = false;
    private boolean undoCategorize = false;
    private Category undoCategorizeCategory = null;
    private SortedMap<Integer, Note> undoNotesMap = new TreeMap<>();
    private Map<Note, Category> undoCategoryMap = new HashMap<>();
    private Map<Note, Boolean> undoArchivedMap = new HashMap<>();

    private String searchQuery;
    private String searchQueryInstant;
    private String searchTags;
    private boolean searchUncompleteChecklists;
    private boolean goBackOnToggleSearchLabel = false;
    private boolean searchLabelActive = false;

    private NoteAdapter listAdapter;
    private UndoBarController ubc;
    private Fab fab;
    private MainActivity mainActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
        setHasOptionsMenu(true);
        setRetainInstance(false);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this, 1);
    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("listViewPosition")) {
                listViewPosition = savedInstanceState.getInt("listViewPosition");
                listViewPositionOffset = savedInstanceState.getInt("listViewPositionOffset");
                searchQuery = savedInstanceState.getString("searchQuery");
                searchTags = savedInstanceState.getString("searchTags");
            }
            keepActionMode = false;
        }
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        prefs = mainActivity.prefs;
        if (savedInstanceState != null) {
            mainActivity.navigationTmp = savedInstanceState.getString("navigationTmp");
        }
        init();
    }


    private void init() {
        initEasterEgg();
        initListView();
        ubc = new UndoBarController(undoBarView, this);

        initNotesList(mainActivity.getIntent());
        initFab();
        initTitle();

        prefs = mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }


    private void initFab() {
        fab = new Fab(fabView, list, prefs.getBoolean(Constants.PREF_FAB_EXPANSION_BEHAVIOR, false));
        fab.setOnFabItemClickedListener(id -> {
            View v = mainActivity.findViewById(id);
            switch (id) {
                case R.id.fab_expand_menu_button:
                    editNote(new Note(), v);
                    break;
                case R.id.fab_note:
                    editNote(new Note(), v);
                    break;
                case R.id.fab_camera:
                    Intent i = mainActivity.getIntent();
                    i.setAction(Constants.ACTION_FAB_TAKE_PHOTO);
                    mainActivity.setIntent(i);
                    editNote(new Note(), v);
                    break;
                case R.id.fab_checklist:
                    Note note = new Note();
                    note.setChecklist(true);
                    editNote(note, v);
                    break;
            }
        });
    }


    boolean closeFab() {
        if (fab != null && fab.isExpanded()) {
            fab.performToggle();
            return true;
        }
        return false;
    }


    private void initTitle() {
        String[] navigationList = getResources().getStringArray(R.array.navigation_list);
        String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
        String navigation = mainActivity.navigationTmp != null ? mainActivity.navigationTmp : prefs.getString
                (Constants.PREF_NAVIGATION, navigationListCodes[0]);
        int index = Arrays.asList(navigationListCodes).indexOf(navigation);
        String title;
        if (index >= 0 && index < navigationListCodes.length) {
            title = navigationList[index];
        } else {
            Category category = DbHelper.getInstance().getCategory(Long.parseLong(navigation));
            title = category != null ? category.getName() : "";
        }
        title = title == null ? getString(R.string.title_activity_list) : title;
        mainActivity.setActionBarTitle(title);
    }


    private void initEasterEgg() {
        empyListItem.setOnClickListener(v -> {
            if (jinglesAnimation == null) {
                jinglesAnimation = (AnimationDrawable) empyListItem.getCompoundDrawables()[1];
                empyListItem.post(() -> {
                    if (jinglesAnimation != null) jinglesAnimation.start();
                });
            } else {
                stopJingles();
            }
        });
    }


    private void stopJingles() {
        if (jinglesAnimation != null) {
            jinglesAnimation.stop();
            jinglesAnimation = null;
            empyListItem.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.jingles_animation, 0, 0);

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        searchQueryInstant = searchQuery;
        stopJingles();
        Crouton.cancelAllCroutons();
        closeFab();
        if (!keepActionMode) {
            commitPending();
            list.clearChoices();
            if (getActionMode() != null) {
                getActionMode().finish();
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        refreshListScrollPosition();
        outState.putInt("listViewPosition", listViewPosition);
        outState.putInt("listViewPositionOffset", listViewPositionOffset);
        outState.putString("searchQuery", searchQuery);
        outState.putString("searchTags", searchTags);
    }


    private void refreshListScrollPosition() {
        if (list != null) {
            listViewPosition = list.getFirstVisiblePosition();
            View v = list.getChildAt(0);
            listViewPositionOffset = (v == null) ? (int) getResources().getDimension(R.dimen.vertical_margin) : v.getTop();
        }
    }


    @SuppressWarnings("static-access")
    @Override
    public void onResume() {
        super.onResume();
        if (Intent.ACTION_SEARCH.equals(mainActivity.getIntent().getAction())) {
            initNotesList(mainActivity.getIntent());
        }
    }

    public void finishActionMode() {
        if (getActionMode() != null) {
            getActionMode().finish();
        }
    }

    private void toggleListViewItem(View view, int position) {
        Note note = listAdapter.getItem(position);
        LinearLayout cardLayout = (LinearLayout) view.findViewById(R.id.card_layout);
        if (!getSelectedNotes().contains(note)) {
            getSelectedNotes().add(note);
            listAdapter.addSelectedItem(position);
            cardLayout.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
        } else {
            getSelectedNotes().remove(note);
            listAdapter.removeSelectedItem(position);
            listAdapter.restoreDrawable(note, cardLayout);
        }
        prepareActionModeMenu();

        if (getSelectedNotes().size() == 0) {
            finishActionMode();
        }

    }

    private void initListView() {
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setItemsCanFocus(false);

        list.setOnItemLongClickListener((arg0, view, position, arg3) -> {
            if (getActionMode() != null) {
                return false;
            }
            mainActivity.startSupportActionMode(new ModeCallback());
            toggleListViewItem(view, position);
            setCabTitle();
            return true;
        });

        list.setOnItemClickListener((arg0, view, position, arg3) -> {
            if (getActionMode() == null) {
                editNote(listAdapter.getItem(position), view);
                return;
            }
            toggleListViewItem(view, position);
            setCabTitle();
        });

        listRoot.setOnViewTouchedListener(this);
    }

    private ImageView getZoomListItemView(View view, Note note) {
        if (expandedImageView != null) {
            View targetView = null;
            if (note.getAttachmentsList().size() > 0) {
                targetView = view.findViewById(R.id.attachmentThumbnail);
            }
            if (targetView == null && note.getCategory() != null) {
                targetView = view.findViewById(R.id.category_marker);
            }
            if (targetView == null) {
                targetView = new ImageView(mainActivity);
                targetView.setBackgroundColor(Color.WHITE);
            }
            targetView.setDrawingCacheEnabled(true);
            targetView.buildDrawingCache();
            Bitmap bmp = targetView.getDrawingCache();
            expandedImageView.setBackgroundColor(BitmapUtils.getDominantColor(bmp));
        }
        return expandedImageView;
    }

    private AnimatorListenerAdapter buildAnimatorListenerAdapter(final Note note) {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                editNote2(note);
            }
        };
    }

    @Override
    public void onViewTouchOccurred(MotionEvent ev) {
        Log.v(Constants.TAG, "Notes list: onViewTouchOccurred " + ev.getAction());
        commitPending();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        initSearchView(menu);
    }

    private void initSortingSubmenu() {
        final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
        final String[] arrayDialog = getResources().getStringArray(R.array.sortable_columns_human_readable);
        int selected = Arrays.asList(arrayDb).indexOf(prefs.getString(Constants.PREF_SORTING_COLUMN, arrayDb[0]));

        SubMenu sortMenu = this.menu.findItem(R.id.menu_sort).getSubMenu();
        for (int i = 0; i < arrayDialog.length; i++) {
            if (sortMenu.findItem(i) == null) {
                sortMenu.add(Constants.MENU_SORT_GROUP_ID, i, i, arrayDialog[i]);
            }
            if (i == selected) sortMenu.getItem(i).setChecked(true);
        }
        sortMenu.setGroupCheckable(Constants.MENU_SORT_GROUP_ID, true, true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        setActionItemsVisibility(menu, false);
    }

    private void prepareActionModeMenu() {
        Menu menu = getActionMode().getMenu();
        int navigation = Navigation.getNavigation();
        boolean showArchive = navigation == Navigation.NOTES || navigation == Navigation.REMINDERS || navigation ==
                Navigation.UNCATEGORIZED || navigation == Navigation.CATEGORY;
        boolean showUnarchive = navigation == Navigation.ARCHIVE || navigation == Navigation.UNCATEGORIZED ||
                navigation == Navigation.CATEGORY;

        if (navigation == Navigation.TRASH) {
            menu.findItem(R.id.menu_untrash).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
        } else {
            if (getSelectedCount() == 1) {
                menu.findItem(R.id.menu_share).setVisible(true);
                menu.findItem(R.id.menu_merge).setVisible(false);
                menu.findItem(R.id.menu_archive)
                        .setVisible(showArchive && !getSelectedNotes().get(0).isArchived
                                ());
                menu.findItem(R.id.menu_unarchive)
                        .setVisible(showUnarchive && getSelectedNotes().get(0).isArchived
                                ());
            } else {
                menu.findItem(R.id.menu_share).setVisible(false);
                menu.findItem(R.id.menu_merge).setVisible(true);
                menu.findItem(R.id.menu_archive).setVisible(showArchive);
                menu.findItem(R.id.menu_unarchive).setVisible(showUnarchive);

            }
            menu.findItem(R.id.menu_add_reminder).setVisible(true);
            menu.findItem(R.id.menu_category).setVisible(true);
            menu.findItem(R.id.menu_uncomplete_checklists).setVisible(false);
            menu.findItem(R.id.menu_tags).setVisible(true);
            menu.findItem(R.id.menu_trash).setVisible(true);
        }
        menu.findItem(R.id.menu_select_all).setVisible(true);

        setCabTitle();
    }

    private int getSelectedCount() {
        return getSelectedNotes().size();
    }

    private void setCabTitle() {
        if (getActionMode() != null) {
            int title = getSelectedCount();
            getActionMode().setTitle(String.valueOf(title));
        }
    }

    @SuppressLint("NewApi")
    private void initSearchView(final Menu menu) {

        if (mainActivity == null) return;

        searchMenuItem = menu.findItem(R.id.menu_search);

        SearchManager searchManager = (SearchManager) mainActivity.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(mainActivity.getComponentName()));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> setActionItemsVisibility(menu, hasFocus));

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            boolean searchPerformed = false;


            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchQuery = null;
                if (searchLayout.getVisibility() == View.VISIBLE) {
                    toggleSearchLabel(false);
                }
                mainActivity.getIntent().setAction(Intent.ACTION_MAIN);
                initNotesList(mainActivity.getIntent());
                mainActivity.supportInvalidateOptionsMenu();
                return true;
            }


            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                searchView.setOnQueryTextListener(new OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String arg0) {

                        return prefs.getBoolean("settings_instant_search", false);
                    }


                    @Override
                    public boolean onQueryTextChange(String pattern) {

                        if (prefs.getBoolean("settings_instant_search", false) && searchLayout != null &&
                                searchPerformed && mFragment.isAdded()) {
                            searchTags = null;
                            searchQuery = pattern;
                            NoteLoaderTask.getInstance().execute("getNotesByPattern", pattern);
                            return true;
                        } else {
                            searchPerformed = true;
                            return false;
                        }
                    }
                });
                return true;
            }
        });
    }

    private void setActionItemsVisibility(Menu menu, boolean searchViewHasFocus) {

        boolean drawerOpen = mainActivity.getDrawerLayout() != null && mainActivity.getDrawerLayout().isDrawerOpen
                (GravityCompat.START);
        boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);

        int navigation = Navigation.getNavigation();
        boolean navigationReminders = navigation == Navigation.REMINDERS;
        boolean navigationArchive = navigation == Navigation.ARCHIVE;
        boolean navigationTrash = navigation == Navigation.TRASH;
        boolean navigationCategory = navigation == Navigation.CATEGORY;

        boolean filterPastReminders = prefs.getBoolean(Constants.PREF_FILTER_PAST_REMINDERS, true);
        boolean filterArchivedInCategory = navigationCategory && prefs.getBoolean(Constants
                .PREF_FILTER_ARCHIVED_IN_CATEGORIES + Navigation.getCategory(), false);

        if (isFabAllowed()) {
            fab.setAllowed(true);
            fab.showFab();
        } else {
            fab.setAllowed(false);
            fab.hideFab();
        }
        menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_filter).setVisible(!drawerOpen && !filterPastReminders && navigationReminders &&
                !searchViewHasFocus);
        menu.findItem(R.id.menu_filter_remove).setVisible(!drawerOpen && filterPastReminders && navigationReminders
                && !searchViewHasFocus);
        menu.findItem(R.id.menu_filter_category).setVisible(!drawerOpen && !filterArchivedInCategory &&
                navigationCategory && !searchViewHasFocus);
        menu.findItem(R.id.menu_filter_category_remove).setVisible(!drawerOpen && filterArchivedInCategory &&
                navigationCategory && !searchViewHasFocus);
        menu.findItem(R.id.menu_sort).setVisible(!drawerOpen && !navigationReminders && !searchViewHasFocus);
        menu.findItem(R.id.menu_expanded_view).setVisible(!drawerOpen && !expandedView && !searchViewHasFocus);
        menu.findItem(R.id.menu_contracted_view).setVisible(!drawerOpen && expandedView && !searchViewHasFocus);
        menu.findItem(R.id.menu_empty_trash).setVisible(!drawerOpen && navigationTrash);
        menu.findItem(R.id.menu_uncomplete_checklists).setVisible(searchViewHasFocus);
        menu.findItem(R.id.menu_tags).setVisible(searchViewHasFocus);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Integer[] protectedActions = {R.id.menu_empty_trash};
        if (Arrays.asList(protectedActions).contains(item.getItemId())) {
            mainActivity.requestPassword(mainActivity, getSelectedNotes(), passwordConfirmed -> {
                if (passwordConfirmed) {
                    performAction(item, null);
                }
            });
        } else {
            performAction(item, null);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean performAction(MenuItem item, ActionMode actionMode) {
        if (actionMode == null) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    if (mainActivity.getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                        mainActivity.getDrawerLayout().closeDrawer(GravityCompat.START);
                    } else {
                        mainActivity.getDrawerLayout().openDrawer(GravityCompat.START);
                    }
                    break;
                case R.id.menu_filter:
                    filterReminders(true);
                    break;
                case R.id.menu_filter_remove:
                    filterReminders(false);
                    break;
                case R.id.menu_filter_category:
                    filterCategoryArchived(true);
                    break;
                case R.id.menu_filter_category_remove:
                    filterCategoryArchived(false);
                case R.id.menu_uncomplete_checklists:
                    filterByUncompleteChecklists();
                    break;
                case R.id.menu_tags:
                    filterByTags();
                    break;
                case R.id.menu_sort:
                    initSortingSubmenu();
                    break;
                case R.id.menu_expanded_view:
                    switchNotesView();
                    break;
                case R.id.menu_contracted_view:
                    switchNotesView();
                    break;
                case R.id.menu_empty_trash:
                    emptyTrash();
                    break;
                default:
                    Log.e(Constants.TAG, "Wrong element choosen: " + item.getItemId());
            }
        } else {
            switch (item.getItemId()) {
                case R.id.menu_category:
                    categorizeNotes();
                    break;
                case R.id.menu_tags:
                    tagNotes();
                    break;
                case R.id.menu_share:
                    share();
                    break;
                case R.id.menu_merge:
                    merge();
                    break;
                case R.id.menu_archive:
                    archiveNotes(true);
                    break;
                case R.id.menu_unarchive:
                    archiveNotes(false);
                    break;
                case R.id.menu_trash:
                    trashNotes(true);
                    break;
                case R.id.menu_untrash:
                    trashNotes(false);
                    break;
                case R.id.menu_delete:
                    deleteNotes();
                    break;
                case R.id.menu_select_all:
                    selectAllNotes();
                    break;
                case R.id.menu_add_reminder:
                    addReminders();
                    break;
            }
        }

        checkSortActionPerformed(item);

        return super.onOptionsItemSelected(item);
    }

    private void addReminders() {
        Intent intent = new Intent(WizFlow.getAppContext(), SnoozeActivity.class);
        intent.setAction(Constants.ACTION_POSTPONE);
        intent.putExtra(Constants.INTENT_NOTE, selectedNotes.toArray(new Note[selectedNotes.size()]));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_ADD_ALARMS);
    }

    private void switchNotesView() {
        boolean expandedView = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true);
        prefs.edit().putBoolean(Constants.PREF_EXPANDED_VIEW, !expandedView).commit();
        initNotesList(mainActivity.getIntent());
        mainActivity.supportInvalidateOptionsMenu();
    }

    void editNote(final Note note, final View view) {
        if (note.isLocked() && !prefs.getBoolean("settings_password_access", false)) {
            PasswordHelper.requestPassword(mainActivity, passwordConfirmed -> {
                if (passwordConfirmed) {
                    note.setPasswordChecked(true);
                    AnimationsHelper.zoomListItem(mainActivity, view, getZoomListItemView(view, note),
                            listRoot, buildAnimatorListenerAdapter(note));
                }
            });
        } else {
            AnimationsHelper.zoomListItem(mainActivity, view, getZoomListItemView(view, note),
                    listRoot, buildAnimatorListenerAdapter(note));
        }
    }

    void editNote2(Note note) {
        if (note.get_id() == null) {
            Log.d(Constants.TAG, "Adding new note");
            try {
                if (Navigation.checkNavigation(Navigation.CATEGORY) || !TextUtils.isEmpty(mainActivity.navigationTmp)) {
                    String categoryId = (String) ObjectUtils.defaultIfNull(mainActivity.navigationTmp,
                            Navigation.getCategory().toString());
                    note.setCategory(DbHelper.getInstance().getCategory(Long.parseLong(categoryId)));
                }
            } catch (NumberFormatException e) {
                Log.v(Constants.TAG, "Maybe was not a category!");
            }
        } else {
            Log.d(Constants.TAG, "Editing note with id: " + note.get_id());
        }

        refreshListScrollPosition();

        mainActivity.switchToDetail(note);
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_CODE_CATEGORY:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mainActivity.showMessage(R.string.category_saved, ONStyle.CONFIRM);
                        EventBus.getDefault().post(new CategoriesUpdatedEvent());
                        break;
                    case Activity.RESULT_FIRST_USER:
                        mainActivity.showMessage(R.string.category_deleted, ONStyle.ALERT);
                        break;
                    default:
                        break;
                }

                break;

            case REQUEST_CODE_CATEGORY_NOTES:
                if (intent != null) {
                    Category tag = intent.getParcelableExtra(Constants.INTENT_CATEGORY);
                    categorizeNotesExecute(tag);
                }
                break;

            case REQUEST_CODE_ADD_ALARMS:
                list.clearChoices();
                selectedNotes.clear();
                finishActionMode();
                list.invalidateViews();
                break;

            default:
                break;
        }

    }

    private void checkSortActionPerformed(MenuItem item) {
        if (item.getGroupId() == Constants.MENU_SORT_GROUP_ID) {
            final String[] arrayDb = getResources().getStringArray(R.array.sortable_columns);
            prefs.edit().putString(Constants.PREF_SORTING_COLUMN, arrayDb[item.getOrder()]).apply();
            initNotesList(mainActivity.getIntent());
            listViewPositionOffset = 16;
            listViewPosition = 0;
            restoreListScrollPosition();
            toggleSearchLabel(false);
            mainActivity.updateWidgets();
        } else {
            ((WizFlow) getActivity().getApplication()).getAnalyticsHelper().trackActionFromResourceId(getActivity(),
                    item.getItemId());
        }
    }

    private void emptyTrash() {
        new MaterialDialog.Builder(mainActivity)
                .content(R.string.empty_trash_confirmation)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {

                        boolean mustDeleteLockedNotes = false;
                        for (int i = 0; i < listAdapter.getCount(); i++) {
                            selectedNotes.add(listAdapter.getItem(i));
                            mustDeleteLockedNotes = mustDeleteLockedNotes || listAdapter.getItem(i).isLocked();
                        }
                        if (mustDeleteLockedNotes) {
                            mainActivity.requestPassword(mainActivity, getSelectedNotes(),
                                    passwordConfirmed -> {
                                        if (passwordConfirmed) {
                                            deleteNotesExecute();
                                        }
                                    });
                        } else {
                            deleteNotesExecute();
                        }
                    }
                }).build().show();
    }

    void initNotesList(Intent intent) {
        Log.d(Constants.TAG, "initNotesList intent: " + intent.getAction());

        progress_wheel.setAlpha(1);
        list.setAlpha(0);
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getCategories() != null
                && intent.getCategories().contains(Intent.CATEGORY_BROWSABLE)) {
            searchTags = intent.getDataString().replace(UrlCompleter.HASHTAG_SCHEME, "");
            goBackOnToggleSearchLabel = true;
        }

        if (Constants.ACTION_SHORTCUT_WIDGET.equals(intent.getAction())) {
            return;
        }

        searchQuery = searchQueryInstant;
        searchQueryInstant = null;
        if (searchTags != null || searchQuery != null || searchUncompleteChecklists
                || IntentChecker.checkAction(intent, Intent.ACTION_SEARCH, Constants.ACTION_SEARCH_UNCOMPLETE_CHECKLISTS)) {

            if (searchTags != null && intent.getStringExtra(SearchManager.QUERY) == null) {
                searchQuery = searchTags;
                NoteLoaderTask.getInstance().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getNotesByTag",
                        searchQuery);
            } else if (searchUncompleteChecklists || Constants.ACTION_SEARCH_UNCOMPLETE_CHECKLISTS.equals(intent.getAction())) {
                searchQuery = getContext().getResources().getString(R.string.uncompleted_checklists);
                searchUncompleteChecklists = true;
                NoteLoaderTask.getInstance().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getNotesByUncompleteChecklist");
            } else {
                if (intent.getStringExtra(SearchManager.QUERY) != null) {
                    searchQuery = intent.getStringExtra(SearchManager.QUERY);
                    searchTags = null;
                }
                NoteLoaderTask.getInstance().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getNotesByPattern",
                        searchQuery);
            }

            toggleSearchLabel(true);

        } else {
            if ((Constants.ACTION_WIDGET_SHOW_LIST.equals(intent.getAction()) && intent
                    .hasExtra(Constants.INTENT_WIDGET))
                    || !TextUtils.isEmpty(mainActivity.navigationTmp)) {
                String widgetId = intent.hasExtra(Constants.INTENT_WIDGET) ? intent.getExtras()
                        .get(Constants.INTENT_WIDGET).toString() : null;
                if (widgetId != null) {
                    String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
                    String categoryId = TextHelper.checkIntentCategory(sqlCondition);
                    mainActivity.navigationTmp = !TextUtils.isEmpty(categoryId) ? categoryId : null;
                }
                intent.removeExtra(Constants.INTENT_WIDGET);
                if (mainActivity.navigationTmp != null) {
                    Long categoryId = Long.parseLong(mainActivity.navigationTmp);
                    NoteLoaderTask.getInstance().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            "getNotesByCategory", categoryId);
                } else {
                    NoteLoaderTask.getInstance().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getAllNotes", true);
                }

            } else {
                NoteLoaderTask.getInstance().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getAllNotes", true);
            }
        }
    }

    public void toggleSearchLabel(boolean activate) {
        if (activate) {
            searchQueryView.setText(Html.fromHtml(getString(R.string.search) + ":<b> " + searchQuery + "</b>"));
            searchLayout.setVisibility(View.VISIBLE);
            searchCancel.setOnClickListener(v -> toggleSearchLabel(false));
            searchLabelActive = true;
        } else {
            if (searchLabelActive) {
                searchLabelActive = false;
                AnimationsHelper.expandOrCollapse(searchLayout, false);
                searchTags = null;
                searchQuery = null;
                searchUncompleteChecklists = false;
                if (!goBackOnToggleSearchLabel) {
                    mainActivity.getIntent().setAction(Intent.ACTION_MAIN);
                    if (searchView != null) {
                        MenuItemCompat.collapseActionView(searchMenuItem);
                    }
                    initNotesList(mainActivity.getIntent());
                } else {
                    mainActivity.onBackPressed();
                }
                goBackOnToggleSearchLabel = false;
                if (Intent.ACTION_VIEW.equals(mainActivity.getIntent().getAction())) {
                    mainActivity.getIntent().setAction(null);
                }
            }
        }
    }

    public void onEvent(NavigationUpdatedNavDrawerClosedEvent navigationUpdatedNavDrawerClosedEvent) {
        listViewPosition = 0;
        listViewPositionOffset = 16;
        initNotesList(mainActivity.getIntent());
        setActionItemsVisibility(menu, false);
    }

    public void onEvent(CategoriesUpdatedEvent categoriesUpdatedEvent) {
        initNotesList(mainActivity.getIntent());
    }

    public void onEvent(NotesLoadedEvent notesLoadedEvent) {
        int layoutSelected = prefs.getBoolean(Constants.PREF_EXPANDED_VIEW, true) ? R.layout.note_layout_expanded
                : R.layout.note_layout;
        listAdapter = new NoteAdapter(mainActivity, layoutSelected, notesLoadedEvent.notes);

        View noteLayout = LayoutInflater.from(mainActivity).inflate(layoutSelected, null, false);
        noteViewHolder = new NoteViewHolder(noteLayout);

        if (Navigation.getNavigation() != Navigation.UNCATEGORIZED && prefs.getBoolean(Constants.PREF_ENABLE_SWIPE,
                true)) {
            list.enableSwipeToDismiss((viewGroup, reverseSortedPositions) -> {

                finishActionMode();

                for (int position : reverseSortedPositions) {
                    Note note;
                    try {
                        note = listAdapter.getItem(position);
                    } catch (IndexOutOfBoundsException e) {
                        Log.d(Constants.TAG, "Please stop swiping in the zone beneath the last card");
                        continue;
                    }

                    if (note != null && note.isLocked()) {
                        PasswordHelper.requestPassword(mainActivity, passwordConfirmed -> {
                            if (!passwordConfirmed) {
                                onUndo(null);
                            }
                        });
                    }

                    getSelectedNotes().add(note);

                    if (Navigation.checkNavigation(Navigation.TRASH)) {
                        trashNotes(false);
                    } else if (Navigation.checkNavigation(Navigation.CATEGORY)) {
                        categorizeNotesExecute(null);
                    } else {
                        if (prefs.getBoolean("settings_swipe_to_trash", false)
                                || Navigation.checkNavigation(Navigation.ARCHIVE)) {
                            trashNotes(true);
                        } else {
                            archiveNotes(true);
                        }
                    }
                }
            });
        } else {
            list.disableSwipeToDismiss();
        }
        list.setAdapter(listAdapter);

        if (notesLoadedEvent.notes.size() == 0) list.setEmptyView(empyListItem);

        if (list != null && notesLoadedEvent.notes.size() > 0) {
            if (Navigation.checkNavigation(Navigation.REMINDERS)) {
                listViewPosition = listAdapter.getClosestNotePosition();
            }
            restoreListScrollPosition();
        }

        animateListView();

        closeFab();
    }

    public void onEvent(PasswordRemovedEvent passwordRemovedEvent) {
        initNotesList(mainActivity.getIntent());
    }

    private void animateListView() {
        if (!WizFlow.isDebugBuild()) {
            animate(progress_wheel).setDuration(getResources().getInteger(R.integer.list_view_fade_anim)).alpha(0);
            animate(list).setDuration(getResources().getInteger(R.integer.list_view_fade_anim)).alpha(1);
        } else {
            progress_wheel.setVisibility(View.INVISIBLE);
            list.setAlpha(1);
        }
    }

    private void restoreListScrollPosition() {
        if (list.getCount() > listViewPosition) {
            list.setSelectionFromTop(listViewPosition, listViewPositionOffset);
            new Handler().postDelayed(fab::showFab, 150);
        } else {
            list.setSelectionFromTop(0, 0);
        }
    }

    public void trashNotes(boolean trash) {
        int selectedNotesSize = getSelectedNotes().size();

        if (trash) {
            trackModifiedNotes(getSelectedNotes());
            for (Note note : getSelectedNotes()) {
                listAdapter.remove(note);
                ReminderHelper.removeReminder(WizFlow.getAppContext(), note);
            }
        } else {
            trashNote(getSelectedNotes(), false);
        }

        if (listAdapter.getCount() == 0)
            list.setEmptyView(empyListItem);

        finishActionMode();

        if (trash) {
            mainActivity.showMessage(R.string.note_trashed, ONStyle.WARN);
        } else {
            mainActivity.showMessage(R.string.note_untrashed, ONStyle.INFO);
        }

        if (trash) {
            ubc.showUndoBar(false, selectedNotesSize + " " + getString(R.string.trashed), null);
            fab.hideFab();
            undoTrash = true;
        } else {
            getSelectedNotes().clear();
        }
    }

    private android.support.v7.view.ActionMode getActionMode() {
        return actionMode;
    }

    private List<Note> getSelectedNotes() {
        return selectedNotes;
    }

    @SuppressLint("NewApi")
    protected void trashNote(List<Note> notes, boolean trash) {
        listAdapter.remove(notes);
        new NoteProcessorTrash(notes, trash).process();
    }

    private void selectAllNotes() {
        for (int i = 0; i < list.getChildCount(); i++) {
            LinearLayout v = (LinearLayout) list.getChildAt(i).findViewById(R.id.card_layout);
            v.setBackgroundColor(getResources().getColor(R.color.list_bg_selected));
        }
        selectedNotes.clear();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            selectedNotes.add(listAdapter.getItem(i));
            listAdapter.addSelectedItem(i);
        }
        prepareActionModeMenu();
        setCabTitle();
    }

    private void deleteNotes() {
        new MaterialDialog.Builder(mainActivity)
                .content(R.string.delete_note_confirmation)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> mainActivity.requestPassword(mainActivity, getSelectedNotes(),
                        passwordConfirmed -> {
                            if (passwordConfirmed) {
                                deleteNotesExecute();
                            }
                        }))
                .build()
                .show();
    }

    private void deleteNotesExecute() {
        listAdapter.remove(getSelectedNotes());
        new NoteProcessorDelete(getSelectedNotes()).process();
        list.clearChoices();
        selectedNotes.clear();
        finishActionMode();
        if (listAdapter.getCount() == 0)
            list.setEmptyView(empyListItem);
        mainActivity.showMessage(R.string.note_deleted, ONStyle.ALERT);
    }

    public void archiveNotes(boolean archive) {
        int selectedNotesSize = getSelectedNotes().size();
        sendToArchive = archive;

        if (!archive) {
            archiveNote(getSelectedNotes(), false);
        } else {
            trackModifiedNotes(getSelectedNotes());
        }

        for (Note note : getSelectedNotes()) {
            if (archive) {
                undoArchivedMap.put(note, note.isArchived());
            }

            if (Navigation.checkNavigation(Navigation.NOTES)
                    || (Navigation.checkNavigation(Navigation.ARCHIVE) && !archive)
                    || (Navigation.checkNavigation(Navigation.CATEGORY) && prefs.getBoolean(Constants
                    .PREF_FILTER_ARCHIVED_IN_CATEGORIES + Navigation.getCategory(), false))) {
                listAdapter.remove(note);
            } else {
                note.setArchived(archive);
                listAdapter.replace(note, listAdapter.getPosition(note));
            }
        }

        listAdapter.notifyDataSetChanged();
        finishActionMode();

        if (listAdapter.getCount() == 0) list.setEmptyView(empyListItem);

        int msg = archive ? R.string.note_archived : R.string.note_unarchived;
        Style style = archive ? ONStyle.WARN : ONStyle.INFO;
        mainActivity.showMessage(msg, style);

        if (archive) {
            ubc.showUndoBar(false, selectedNotesSize + " " + getString(R.string.archived), null);
            fab.hideFab();
            undoArchive = true;
        } else {
            getSelectedNotes().clear();
        }
    }

    private void trackModifiedNotes(List<Note> modifiedNotesToTrack) {
        for (Note note : modifiedNotesToTrack) {
            undoNotesMap.put(listAdapter.getPosition(note), note);
        }
    }

    private void archiveNote(List<Note> notes, boolean archive) {
        new NoteProcessorArchive(notes, archive).process();
        if (!Navigation.checkNavigation(Navigation.CATEGORY)) {
            listAdapter.remove(notes);
        }
        Log.d(Constants.TAG, "Notes" + (archive ? "archived" : "restored from archive"));
    }

    void editCategory(Category category) {
        Intent categoryIntent = new Intent(mainActivity, CategoryActivity.class);
        categoryIntent.putExtra(Constants.INTENT_CATEGORY, category);
        startActivityForResult(categoryIntent, REQUEST_CODE_CATEGORY);
    }

    private void categorizeNotes() {
        final ArrayList<Category> categories = DbHelper.getInstance().getCategories();

        final MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                .title(R.string.categorize_as)
                .adapter(new NavDrawerCategoryAdapter(mainActivity, categories), null)
                .positiveText(R.string.add_category)
                .positiveColorRes(R.color.colorPrimary)
                .negativeText(R.string.remove_category)
                .negativeColorRes(R.color.colorAccent)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        keepActionMode = true;
                        Intent intent = new Intent(mainActivity, CategoryActivity.class);
                        intent.putExtra("noHome", true);
                        startActivityForResult(intent, REQUEST_CODE_CATEGORY_NOTES);
                    }


                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        categorizeNotesExecute(null);
                    }
                }).build();

        ListView dialogList = dialog.getListView();
        assert dialogList != null;
        dialogList.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            categorizeNotesExecute(categories.get(position));
        });

        dialog.show();
    }

    private void categorizeNotesExecute(Category category) {
        if (category != null) {
            categorizeNote(getSelectedNotes(), category);
        } else {
            trackModifiedNotes(getSelectedNotes());
        }
        for (Note note : getSelectedNotes()) {
            if (category == null) {
                undoCategoryMap.put(note, note.getCategory());
            }
            if ((Navigation.checkNavigation(Navigation.CATEGORY) && !Navigation.checkNavigationCategory(category)) ||
                    Navigation.checkNavigation(Navigation.UNCATEGORIZED)) {
                listAdapter.remove(note);
            } else {
                note.setCategory(category);
                listAdapter.replace(note, listAdapter.getPosition(note));
            }
        }

        finishActionMode();

        if (listAdapter.getCount() == 0)
            list.setEmptyView(empyListItem);

        String msg;
        if (category != null) {
            msg = getResources().getText(R.string.notes_categorized_as) + " '" + category.getName() + "'";
        } else {
            msg = getResources().getText(R.string.notes_category_removed).toString();
        }
        mainActivity.showMessage(msg, ONStyle.INFO);

        if (category == null) {
            ubc.showUndoBar(false, getString(R.string.notes_category_removed), null);
            fab.hideFab();
            undoCategorize = true;
            undoCategorizeCategory = null;
        } else {
            getSelectedNotes().clear();
        }
    }

    private void categorizeNote(List<Note> notes, Category category) {
        new NoteProcessorCategorize(notes, category).process();
    }

    private void tagNotes() {

        final List<Tag> tags = DbHelper.getInstance().getTags();

        if (tags.size() == 0) {
            finishActionMode();
            mainActivity.showMessage(R.string.no_tags_created, ONStyle.WARN);
            return;
        }

        final Integer[] preSelectedTags = TagsHelper.getPreselectedTagsArray(selectedNotes, tags);

        new MaterialDialog.Builder(mainActivity)
                .title(R.string.select_tags)
                .items(TagsHelper.getTagsArray(tags))
                .positiveText(R.string.ok)
                .itemsCallbackMultiChoice(preSelectedTags, (dialog, which, text) -> {
                    dialog.dismiss();
                    tagNotesExecute(tags, which, preSelectedTags);
                    return false;
                }).build().show();
    }

    private void tagNotesExecute(List<Tag> tags, Integer[] selectedTags, Integer[] preSelectedTags) {

        for (Note note : getSelectedNotes()) {
            tagNote(tags, selectedTags, note);
        }

        list.clearChoices();

        list.invalidateViews();

        if (listAdapter.getCount() == 0)
            list.setEmptyView(empyListItem);

        if (getActionMode() != null) {
            getActionMode().finish();
        }

        mainActivity.showMessage(R.string.tags_added, ONStyle.INFO);
    }

    private void tagNote(List<Tag> tags, Integer[] selectedTags, Note note) {

        Pair<String, List<Tag>> taggingResult = TagsHelper.addTagToNote(tags, selectedTags, note);

        if (note.isChecklist()) {
            note.setTitle(note.getTitle() + System.getProperty("line.separator") + taggingResult.first);
        } else {
            StringBuilder sb = new StringBuilder(note.getContent());
            if (sb.length() > 0) {
                sb.append(System.getProperty("line.separator"))
                        .append(System.getProperty("line.separator"));
            }
            sb.append(taggingResult.first);
            note.setContent(sb.toString());
        }

        Pair<String, String> titleAndContent = TagsHelper.removeTag(note.getTitle(), note.getContent(),
                taggingResult.second);
        note.setTitle(titleAndContent.first);
        note.setContent(titleAndContent.second);

        DbHelper.getInstance().updateNote(note, false);
    }

    @Override
    public void onUndo(Parcelable undoToken) {
        for (Integer notePosition : undoNotesMap.keySet()) {
            Note currentNote = undoNotesMap.get(notePosition);
            if ((undoCategorize && !Navigation.checkNavigationCategory(undoCategoryMap.get(currentNote)))
                    || undoArchive && !Navigation.checkNavigation(Navigation.NOTES)) {
                if (undoCategorize) {
                    currentNote.setCategory(undoCategoryMap.get(currentNote));
                } else if (undoArchive) {
                    currentNote.setArchived(undoArchivedMap.get(currentNote));
                }
                listAdapter.replace(currentNote, listAdapter.getPosition(currentNote));
            } else {
                list.insert(notePosition, currentNote);
            }
        }

        listAdapter.notifyDataSetChanged();

        selectedNotes.clear();
        undoNotesMap.clear();

        undoTrash = false;
        undoArchive = false;
        undoCategorize = false;
        undoNotesMap.clear();
        undoCategoryMap.clear();
        undoArchivedMap.clear();
        undoCategorizeCategory = null;
        Crouton.cancelAllCroutons();

        if (getActionMode() != null) {
            getActionMode().finish();
        }
        ubc.hideUndoBar(false);
        fab.showFab();
    }

    void commitPending() {
        if (undoTrash || undoArchive || undoCategorize) {

            List<Note> notesList = new ArrayList<>(undoNotesMap.values());
            if (undoTrash)
                trashNote(notesList, true);
            else if (undoArchive)
                archiveNote(notesList, sendToArchive);
            else if (undoCategorize)
                categorizeNote(notesList, undoCategorizeCategory);

            undoTrash = false;
            undoArchive = false;
            undoCategorize = false;
            undoCategorizeCategory = null;

            selectedNotes.clear();
            undoNotesMap.clear();
            undoCategoryMap.clear();
            undoArchivedMap.clear();
            list.clearChoices();

            ubc.hideUndoBar(false);
            fab.showFab();

            Log.d(Constants.TAG, "Changes committed");
        }
        mainActivity.updateWidgets();
    }

    private void share() {
        for (final Note note : getSelectedNotes()) {
            mainActivity.shareNote(note);
        }

        getSelectedNotes().clear();
        if (getActionMode() != null) {
            getActionMode().finish();
        }
    }

    public void merge() {
        new MaterialDialog.Builder(mainActivity)
                .title(R.string.delete_merged)
                .positiveText(R.string.ok)
                .negativeText(R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EventBus.getDefault().post(new NotesMergeEvent(false));
                    }


                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        EventBus.getDefault().post(new NotesMergeEvent(true));
                    }
                }).build().show();
    }

    public void onEventAsync(NotesMergeEvent notesMergeEvent) {

        final Note finalMergedNote = NotesHelper.mergeNotes(getSelectedNotes(), notesMergeEvent.keepMergedNotes);
        new Handler(Looper.getMainLooper()).post(() -> {

            if (!notesMergeEvent.keepMergedNotes) {
                ArrayList<String> notesIds = new ArrayList<>();
                for (Note selectedNote : getSelectedNotes()) {
                    notesIds.add(String.valueOf(selectedNote.get_id()));
                }
                mainActivity.getIntent().putExtra("merged_notes", notesIds);
            }

            getSelectedNotes().clear();
            if (getActionMode() != null) {
                getActionMode().finish();
            }

            mainActivity.getIntent().setAction(Constants.ACTION_MERGE);
            mainActivity.switchToDetail(finalMergedNote);
        });
    }

    private void filterReminders(boolean filter) {
        prefs.edit().putBoolean(Constants.PREF_FILTER_PAST_REMINDERS, filter).apply();
        initNotesList(mainActivity.getIntent());
        mainActivity.supportInvalidateOptionsMenu();
    }

    private void filterCategoryArchived(boolean filter) {
        if (filter) {
            prefs.edit().putBoolean(Constants.PREF_FILTER_ARCHIVED_IN_CATEGORIES + Navigation.getCategory(), true).apply();
        } else {
            prefs.edit().remove(Constants.PREF_FILTER_ARCHIVED_IN_CATEGORIES + Navigation.getCategory()).apply();
        }
        initNotesList(mainActivity.getIntent());
        mainActivity.supportInvalidateOptionsMenu();
    }

    private void filterByUncompleteChecklists() {
        initNotesList(new Intent(Constants.ACTION_SEARCH_UNCOMPLETE_CHECKLISTS));
    }

    private void filterByTags() {

        final List<Tag> tags = TagsHelper.getAllTags();

        if (tags.size() == 0) {
            mainActivity.showMessage(R.string.no_tags_created, ONStyle.WARN);
            return;
        }

        new MaterialDialog.Builder(mainActivity)
                .title(R.string.select_tags)
                .items(TagsHelper.getTagsArray(tags))
                .positiveText(R.string.ok)
                .itemsCallbackMultiChoice(new Integer[]{}, (dialog, which, text) -> {
                    List<String> selectedTags = new ArrayList<>();
                    for (Integer aWhich : which) {
                        selectedTags.add(tags.get(aWhich).getText());
                    }

                    searchTags = selectedTags.toString().substring(1, selectedTags.toString().length() - 1)
                            .replace(" ", "");
                    Intent intent = mainActivity.getIntent();

                    searchView.clearFocus();
                    KeyboardUtils.hideKeyboard(searchView);

                    intent.removeExtra(SearchManager.QUERY);
                    initNotesList(intent);
                    return false;
                }).build().show();
    }

    public MenuItem getSearchMenuItem() {
        return searchMenuItem;
    }

    private boolean isFabAllowed() {
        return isFabAllowed(false);
    }

    private boolean isFabAllowed(boolean actionModeFinishing) {

        boolean isAllowed = true;

        isAllowed = isAllowed && (getActionMode() == null || actionModeFinishing);
        int navigation = Navigation.getNavigation();
        isAllowed = isAllowed && navigation != Navigation.ARCHIVE && navigation != Navigation.REMINDERS && navigation
                != Navigation.TRASH;
        isAllowed = isAllowed && mainActivity.getDrawerLayout() != null && !mainActivity.getDrawerLayout().isDrawerOpen
                (GravityCompat.START);

        return isAllowed;
    }

    private final class ModeCallback implements android.support.v7.view.ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
            actionMode = mode;
            fab.setAllowed(isFabAllowed());
            fab.hideFab();
            return true;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (int i = 0; i < listAdapter.getSelectedItems().size(); i++) {
                int key = listAdapter.getSelectedItems().keyAt(i);
                View v = list.getChildAt(key - list.getFirstVisiblePosition());
                if (listAdapter.getCount() > key && listAdapter.getItem(key) != null && v != null) {
                    listAdapter.restoreDrawable(listAdapter.getItem(key), v.findViewById(R.id.card_layout));
                }
            }

            selectedNotes.clear();
            listAdapter.clearSelectedItems();
            list.clearChoices();

            fab.setAllowed(isFabAllowed(true));
            if (undoNotesMap.size() == 0) {
                fab.showFab();
            }

            actionMode = null;
            Log.d(Constants.TAG, "Closed multiselection contextual menu");
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            prepareActionModeMenu();
            return true;
        }


        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            Integer[] protectedActions = {R.id.menu_select_all, R.id.menu_merge};
            if (!Arrays.asList(protectedActions).contains(item.getItemId())) {
                mainActivity.requestPassword(mainActivity, getSelectedNotes(),
                        passwordConfirmed -> {
                            if (passwordConfirmed) {
                                performAction(item, mode);
                            }
                        });
            } else {
                performAction(item, mode);
            }
            return true;
        }
    }


}
