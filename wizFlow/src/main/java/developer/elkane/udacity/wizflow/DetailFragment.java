
package developer.elkane.udacity.wizflow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.neopixl.pixlui.components.edittext.EditText;
import com.neopixl.pixlui.components.textview.TextView;
import com.pushbullet.android.extension.MessagingExtension;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Style;
import developer.elkane.udacity.wizflow.async.AttachmentTask;
import developer.elkane.udacity.wizflow.async.bus.NotesUpdatedEvent;
import developer.elkane.udacity.wizflow.async.bus.PushbulletReplyEvent;
import developer.elkane.udacity.wizflow.async.bus.SwitchFragmentEvent;
import developer.elkane.udacity.wizflow.async.notes.NoteProcessorDelete;
import developer.elkane.udacity.wizflow.async.notes.SaveNoteTask;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.helpers.AttachmentsHelper;
import developer.elkane.udacity.wizflow.helpers.PermissionsHelper;
import developer.elkane.udacity.wizflow.helpers.date.DateHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.ONStyle;
import developer.elkane.udacity.wizflow.models.Tag;
import developer.elkane.udacity.wizflow.models.adapters.AttachmentAdapter;
import developer.elkane.udacity.wizflow.models.adapters.NavDrawerCategoryAdapter;
import developer.elkane.udacity.wizflow.models.adapters.PlacesAutoCompleteAdapter;
import developer.elkane.udacity.wizflow.models.listeners.OnAttachingFileListener;
import developer.elkane.udacity.wizflow.models.listeners.OnGeoUtilResultListener;
import developer.elkane.udacity.wizflow.models.listeners.OnNoteSaved;
import developer.elkane.udacity.wizflow.models.listeners.OnReminderPickedListener;
import developer.elkane.udacity.wizflow.models.views.ExpandableHeightGridView;
import developer.elkane.udacity.wizflow.utils.AlphaManager;
import developer.elkane.udacity.wizflow.utils.ConnectionManager;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Display;
import developer.elkane.udacity.wizflow.utils.FileHelper;
import developer.elkane.udacity.wizflow.utils.Fonts;
import developer.elkane.udacity.wizflow.utils.GeocodeHelper;
import developer.elkane.udacity.wizflow.utils.IntentChecker;
import developer.elkane.udacity.wizflow.utils.KeyboardUtils;
import developer.elkane.udacity.wizflow.utils.PasswordHelper;
import developer.elkane.udacity.wizflow.utils.ReminderHelper;
import developer.elkane.udacity.wizflow.utils.ShortcutHelper;
import developer.elkane.udacity.wizflow.utils.StorageHelper;
import developer.elkane.udacity.wizflow.utils.TagsHelper;
import developer.elkane.udacity.wizflow.utils.TextHelper;
import developer.elkane.udacity.wizflow.utils.date.DateUtils;
import developer.elkane.udacity.wizflow.utils.date.ReminderPickers;
import it.feio.android.checklistview.exceptions.ViewNotSupportedException;
import it.feio.android.checklistview.interfaces.CheckListChangedListener;
import it.feio.android.checklistview.models.CheckListView;
import it.feio.android.checklistview.models.CheckListViewItem;
import it.feio.android.checklistview.models.ChecklistManager;
import it.feio.android.pixlui.links.TextLinkClickListener;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;


public class DetailFragment extends BaseFragment implements OnReminderPickedListener, OnTouchListener,
        OnAttachingFileListener, TextWatcher, CheckListChangedListener, OnNoteSaved,
        OnGeoUtilResultListener {

    private static final int TAKE_PHOTO = 1;
    private static final int TAKE_VIDEO = 2;
    private static final int SET_PASSWORD = 3;
    private static final int SKETCH = 4;
    private static final int CATEGORY = 5;
    private static final int DETAIL = 6;
    private static final int FILES = 7;
    private static final int RC_READ_EXTERNAL_STORAGE_PERMISSION = 1;
    public OnDateSetListener onDateSetListener;
    public OnTimeSetListener onTimeSetListener;
    public boolean goBack = false;
    @BindView(R.id.detail_root)
    ViewGroup root;
    @BindView(R.id.detail_title)
    EditText title;
    @BindView(R.id.detail_content)
    EditText content;
    @BindView(R.id.detail_attachments_above)
    ViewStub attachmentsAbove;
    @BindView(R.id.detail_attachments_below)
    ViewStub attachmentsBelow;
    @Nullable
    @BindView(R.id.gridview)
    ExpandableHeightGridView mGridView;
    @BindView(R.id.location)
    TextView locationTextView;
    @BindView(R.id.detail_timestamps)
    View timestampsView;
    @BindView(R.id.reminder_layout)
    LinearLayout reminder_layout;
    @BindView(R.id.reminder_icon)
    ImageView reminderIcon;
    @BindView(R.id.datetime)
    TextView datetime;
    @BindView(R.id.detail_tile_card)
    View titleCardView;
    @BindView(R.id.content_wrapper)
    ScrollView scrollView;
    @BindView(R.id.creation)
    TextView creationTextView;
    @BindView(R.id.last_modification)
    TextView lastModificationTextView;
    @BindView(R.id.title_wrapper)
    View titleWrapperView;
    @BindView(R.id.tag_marker)
    View tagMarkerView;
    @BindView(R.id.detail_wrapper)
    ViewManager detailWrapperView;
    @BindView(R.id.snackbar_placeholder)
    View snackBarPlaceholder;
    View toggleChecklistView;
    private Uri attachmentUri;
    private AttachmentAdapter mAttachmentAdapter;
    private MaterialDialog attachmentDialog;
    private Note note;
    private Note noteTmp;
    private Note noteOriginal;
    private String recordName;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean isRecording = false;
    private View isPlayingView = null;
    private Bitmap recordingBitmap;
    private ChecklistManager mChecklistManager;
    private String exitMessage;
    private Style exitCroutonStyle = ONStyle.CONFIRM;
    private boolean afterSavedReturnsToList = true;
    private boolean showKeyboard = false;
    private boolean swiping;
    private int startSwipeX;
    private SharedPreferences prefs;
    private boolean orientationChanged;
    private long audioRecordingTimeStart;
    private long audioRecordingTime;
    private DetailFragment mFragment;
    private Attachment sketchEdited;
    private int contentLineCounter = 1;
    private int contentCursorPosition;
    private ArrayList<String> mergedNotesIds;
    private MainActivity mainActivity;
    TextLinkClickListener textLinkClickListener = new TextLinkClickListener() {
        @Override
        public void onTextLinkClick(View view, final String clickedString, final String url) {
            new MaterialDialog.Builder(mainActivity)
                    .content(clickedString)
                    .negativeColorRes(R.color.colorPrimary)
                    .positiveText(R.string.open)
                    .negativeText(R.string.copy)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            boolean error = false;
                            Intent intent = null;
                            try {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            } catch (NullPointerException e) {
                                error = true;
                            }

                            if (intent == null
                                    || error
                                    || !IntentChecker
                                    .isAvailable(
                                            mainActivity,
                                            intent,
                                            new String[]{PackageManager.FEATURE_CAMERA})) {
                                mainActivity.showMessage(R.string.no_application_can_perform_this_action,
                                        ONStyle.ALERT);

                            } else {
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                    mainActivity
                                            .getSystemService(Activity.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("text label",
                                    clickedString);
                            clipboard.setPrimaryClip(clip);
                        }
                    }).build().show();
            View clickedView = noteTmp.isChecklist() ? toggleChecklistView : content;
            clickedView.clearFocus();
            KeyboardUtils.hideKeyboard(clickedView);
            new Handler().post(() -> {
                View clickedView1 = noteTmp.isChecklist() ? toggleChecklistView : content;
                KeyboardUtils.hideKeyboard(clickedView1);
            });
        }
    };
    private boolean activityPausing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.CHILDREN));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        GeocodeHelper.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        activityPausing = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        prefs = mainActivity.prefs;

        mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainActivity.getToolbar().setNavigationOnClickListener(v -> navigateUp());

        if (NavigationDrawerFragment.isDoublePanelActive()) {
            mainActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        } else {
            mainActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        if (savedInstanceState != null) {
            noteTmp = savedInstanceState.getParcelable("noteTmp");
            note = savedInstanceState.getParcelable("note");
            noteOriginal = savedInstanceState.getParcelable("noteOriginal");
            attachmentUri = savedInstanceState.getParcelable("attachmentUri");
            orientationChanged = savedInstanceState.getBoolean("orientationChanged");
        }

        if (mainActivity.sketchUri != null) {
            Attachment mAttachment = new Attachment(mainActivity.sketchUri, Constants.MIME_TYPE_SKETCH);
            addAttachment(mAttachment);
            mainActivity.sketchUri = null;
            if (sketchEdited != null) {
                noteTmp.getAttachmentsList().remove(sketchEdited);
                sketchEdited = null;
            }
        }

        init();

        setHasOptionsMenu(true);
        setRetainInstance(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (noteTmp != null) {
            noteTmp.setTitle(getNoteTitle());
            noteTmp.setContent(getNoteContent());
            outState.putParcelable("noteTmp", noteTmp);
            outState.putParcelable("note", note);
            outState.putParcelable("noteOriginal", noteOriginal);
            outState.putParcelable("attachmentUri", attachmentUri);
            outState.putBoolean("orientationChanged", orientationChanged);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();

        activityPausing = true;

        if (!goBack) {
            saveNote(this);
        }

        if (toggleChecklistView != null) {
            KeyboardUtils.hideKeyboard(toggleChecklistView);
            content.clearFocus();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation != newConfig.orientation) {
            orientationChanged = true;
        }
    }

    private void init() {

        handleIntents();

        if (noteOriginal == null) {
            noteOriginal = getArguments().getParcelable(Constants.INTENT_NOTE);
        }

        if (note == null) {
            note = new Note(noteOriginal);
        }

        if (noteTmp == null) {
            noteTmp = new Note(note);
        }

        if (noteTmp.isLocked() && !noteTmp.isPasswordChecked()) {
            checkNoteLock(noteTmp);
            return;
        }

        initViews();
    }

    private void checkNoteLock(Note note) {
        if (note.isLocked()
                && prefs.getString(Constants.PREF_PASSWORD, null) != null
                && !prefs.getBoolean("settings_password_access", false)) {
            PasswordHelper.requestPassword(mainActivity, passwordConfirmed -> {
                if (passwordConfirmed) {
                    noteTmp.setPasswordChecked(true);
                    init();
                } else {
                    goBack = true;
                    goHome();
                }
            });
        } else {
            noteTmp.setPasswordChecked(true);
            init();
        }
    }

    private void handleIntents() {
        Intent i = mainActivity.getIntent();

        if (IntentChecker.checkAction(i, Constants.ACTION_MERGE)) {
            noteOriginal = new Note();
            note = new Note(noteOriginal);
            noteTmp = getArguments().getParcelable(Constants.INTENT_NOTE);
            if (i.getStringArrayListExtra("merged_notes") != null) {
                mergedNotesIds = i.getStringArrayListExtra("merged_notes");
            }
        }

        if (IntentChecker.checkAction(i, Constants.ACTION_SHORTCUT, Constants.ACTION_NOTIFICATION_CLICK)) {
            afterSavedReturnsToList = false;
            try {
                noteOriginal = DbHelper.getInstance().getNote(i.getLongExtra(Constants.INTENT_KEY, 0));
                note = new Note(noteOriginal);
                noteTmp = new Note(noteOriginal);
            } catch (NullPointerException e) {
                mainActivity.showToast(getText(R.string.shortcut_note_deleted), Toast.LENGTH_LONG);
                mainActivity.finish();
            }
        }

        if (IntentChecker.checkAction(i, Constants.ACTION_WIDGET, Constants.ACTION_WIDGET_TAKE_PHOTO)) {

            afterSavedReturnsToList = false;
            showKeyboard = true;

            if (i.hasExtra(Constants.INTENT_WIDGET)) {
                String widgetId = i.getExtras().get(Constants.INTENT_WIDGET).toString();
                if (widgetId != null) {
                    String sqlCondition = prefs.getString(Constants.PREF_WIDGET_PREFIX + widgetId, "");
                    String categoryId = TextHelper.checkIntentCategory(sqlCondition);
                    if (categoryId != null) {
                        Category category;
                        try {
                            category = DbHelper.getInstance().getCategory(parseLong(categoryId));
                            noteTmp = new Note();
                            noteTmp.setCategory(category);
                        } catch (NumberFormatException e) {
                            Log.e(Constants.TAG, "Category with not-numeric value!", e);
                        }
                    }
                }
            }

            if (IntentChecker.checkAction(i, Constants.ACTION_WIDGET_TAKE_PHOTO)) {
                takePhoto();
            }
        }

        if (IntentChecker.checkAction(i, Constants.ACTION_FAB_TAKE_PHOTO)) {
            takePhoto();
        }

        if (IntentChecker.checkAction(i, Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE, Constants.INTENT_GOOGLE_NOW)
                && i.getType() != null) {

            afterSavedReturnsToList = false;

            if (noteTmp == null) noteTmp = new Note();

            String title = i.getStringExtra(Intent.EXTRA_SUBJECT);
            if (title != null) {
                noteTmp.setTitle(title);
            }

            String content = i.getStringExtra(Intent.EXTRA_TEXT);
            if (content != null) {
                noteTmp.setContent(content);
            }

            importAttachments(i);

        }

        if (IntentChecker.checkAction(i, Intent.ACTION_MAIN, Constants.ACTION_WIDGET_SHOW_LIST, Constants
                .ACTION_SHORTCUT_WIDGET, Constants.ACTION_WIDGET)) {
            showKeyboard = true;
        }

        i.setAction(null);
    }

    private void importAttachments(Intent i) {

        if (!i.hasExtra(Intent.EXTRA_STREAM)) return;

        if (i.getExtras().get(Intent.EXTRA_STREAM) instanceof Uri) {
            Uri uri = i.getParcelableExtra(Intent.EXTRA_STREAM);
            if (!Constants.INTENT_GOOGLE_NOW.equals(i.getAction())) {
                String name = FileHelper.getNameFromUri(mainActivity, uri);
                new AttachmentTask(this, uri, name, this).execute();
            }
        } else {
            ArrayList<Uri> uris = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            for (Uri uriSingle : uris) {
                String name = FileHelper.getNameFromUri(mainActivity, uriSingle);
                new AttachmentTask(this, uriSingle, name, this).execute();
            }
        }
    }

    @SuppressLint("NewApi")
    private void initViews() {

        root.setOnTouchListener(this);

        Fonts.overrideTextSize(mainActivity, prefs, root);

        setTagMarkerColor(noteTmp.getCategory());

        initViewTitle();

        initViewContent();

        initViewLocation();

        initViewAttachments();

        initViewReminder();

        initViewFooter();
    }

    private void initViewFooter() {
        String creation = DateHelper.getFormattedDate(noteTmp.getCreation(), prefs.getBoolean(Constants
                .PREF_PRETTIFIED_DATES, true));
        creationTextView.append(creation.length() > 0 ? getString(R.string.creation) + " " + creation : "");
        if (creationTextView.getText().length() == 0)
            creationTextView.setVisibility(View.GONE);

        String lastModification = DateHelper.getFormattedDate(noteTmp.getLastModification(), prefs.getBoolean(Constants
                .PREF_PRETTIFIED_DATES, true));
        lastModificationTextView.append(lastModification.length() > 0 ? getString(R.string.last_update) + " " +
                lastModification : "");
        if (lastModificationTextView.getText().length() == 0)
            lastModificationTextView.setVisibility(View.GONE);
    }

    private void initViewReminder() {

        reminder_layout.setOnClickListener(v -> {
            int pickerType = prefs.getBoolean("settings_simple_calendar", false) ? ReminderPickers.TYPE_AOSP :
                    ReminderPickers.TYPE_GOOGLE;
            ReminderPickers reminderPicker = new ReminderPickers(mainActivity, mFragment, pickerType);
            reminderPicker.pick(DateUtils.getPresetReminder(noteTmp.getAlarm()), noteTmp
                    .getRecurrenceRule());
            onDateSetListener = reminderPicker;
            onTimeSetListener = reminderPicker;
        });

        reminder_layout.setOnLongClickListener(v -> {
            MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                    .content(R.string.remove_reminder)
                    .positiveText(R.string.ok)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            ReminderHelper.removeReminder(WizFlow.getAppContext(), noteTmp);
                            noteTmp.setAlarm(null);
                            reminderIcon.setImageResource(R.drawable.ic_alarm_black_18dp);
                            datetime.setText("");
                        }
                    }).build();
            dialog.show();
            return true;
        });

        String reminderString = initReminder(noteTmp);
        if (!StringUtils.isEmpty(reminderString)) {
            reminderIcon.setImageResource(R.drawable.ic_alarm_add_black_18dp);
            datetime.setText(reminderString);
        }
    }

    private void initViewLocation() {

        DetailFragment detailFragment = this;

        if (isNoteLocationValid()) {
            if (TextUtils.isEmpty(noteTmp.getAddress())) {
                GeocodeHelper.getAddressFromCoordinates(new Location("sasd"), detailFragment);
            } else {
                locationTextView.setText(noteTmp.getAddress());
                locationTextView.setVisibility(View.VISIBLE);
            }
        }

        if (prefs.getBoolean(Constants.PREF_AUTO_LOCATION, false) && noteTmp.get_id() == null) {
            getLocation(detailFragment);
        }

        locationTextView.setOnClickListener(v -> {
            String uriString = "geo:" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude()
                    + "?q=" + noteTmp.getLatitude() + ',' + noteTmp.getLongitude();
            Intent locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            if (!IntentChecker.isAvailable(mainActivity, locationIntent, null)) {
                uriString = "http://maps.google.com/maps?q=" + noteTmp.getLatitude() + ',' + noteTmp
                        .getLongitude();
                locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            }
            startActivity(locationIntent);
        });
        locationTextView.setOnLongClickListener(v -> {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(mainActivity);
            builder.content(R.string.remove_location);
            builder.positiveText(R.string.ok);
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog materialDialog) {
                    noteTmp.setLatitude("");
                    noteTmp.setLongitude("");
                    fade(locationTextView, false);
                }
            });
            MaterialDialog dialog = builder.build();
            dialog.show();
            return true;
        });
    }

    private void getLocation(OnGeoUtilResultListener onGeoUtilResultListener) {
        PermissionsHelper.requestPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION, R.string
                .permission_coarse_location, snackBarPlaceholder, () -> GeocodeHelper.getLocation
                (onGeoUtilResultListener));
    }

    private void initViewAttachments() {

        if (prefs.getBoolean(Constants.PREF_ATTANCHEMENTS_ON_BOTTOM, false)) {
            attachmentsBelow.inflate();
        } else {
            attachmentsAbove.inflate();
        }
        mGridView = (ExpandableHeightGridView) root.findViewById(R.id.gridview);

        mAttachmentAdapter = new AttachmentAdapter(mainActivity, noteTmp.getAttachmentsList(), mGridView);

        mGridView.setAdapter(mAttachmentAdapter);
        mGridView.autoresize();

        mGridView.setOnItemClickListener((parent, v, position, id) -> {
            Attachment attachment = (Attachment) parent.getAdapter().getItem(position);
            Uri uri = attachment.getUri();
            Intent attachmentIntent;
            if (Constants.MIME_TYPE_FILES.equals(attachment.getMime_type())) {

                attachmentIntent = new Intent(Intent.ACTION_VIEW);
                attachmentIntent.setDataAndType(uri, StorageHelper.getMimeType(mainActivity,
                        attachment.getUri()));
                attachmentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
                        .FLAG_GRANT_WRITE_URI_PERMISSION);
                if (IntentChecker.isAvailable(mainActivity.getApplicationContext(), attachmentIntent, null)) {
                    startActivity(attachmentIntent);
                } else {
                    mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
                }

            } else if (Constants.MIME_TYPE_IMAGE.equals(attachment.getMime_type())
                    || Constants.MIME_TYPE_SKETCH.equals(attachment.getMime_type())
                    || Constants.MIME_TYPE_VIDEO.equals(attachment.getMime_type())) {
                noteTmp.setTitle(getNoteTitle());
                noteTmp.setContent(getNoteContent());
                String title1 = TextHelper.parseTitleAndContent(mainActivity,
                        noteTmp)[0].toString();
                int clickedImage = 0;
                ArrayList<Attachment> images = new ArrayList<>();
                for (Attachment mAttachment : noteTmp.getAttachmentsList()) {
                    if (Constants.MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())
                            || Constants.MIME_TYPE_SKETCH.equals(mAttachment.getMime_type())
                            || Constants.MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
                        images.add(mAttachment);
                        if (mAttachment.equals(attachment)) {
                            clickedImage = images.size() - 1;
                        }
                    }
                }
                attachmentIntent = new Intent(mainActivity, GalleryActivity.class);
                attachmentIntent.putExtra(Constants.GALLERY_TITLE, title1);
                attachmentIntent.putParcelableArrayListExtra(Constants.GALLERY_IMAGES, images);
                attachmentIntent.putExtra(Constants.GALLERY_CLICKED_IMAGE, clickedImage);
                startActivity(attachmentIntent);

            } else if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMime_type())) {
                playback(v, attachment.getUri());
            }

        });

        mGridView.setOnItemLongClickListener((parent, v, position, id) -> {
            if (mPlayer != null) return false;
            List<String> items = Arrays.asList(getResources().getStringArray(R.array.attachments_actions));
            if (!Constants.MIME_TYPE_SKETCH.equals(mAttachmentAdapter.getItem(position).getMime_type())) {
                items = items.subList(0, items.size() - 1);
            }
            Attachment attachment = mAttachmentAdapter.getItem(position);
            new MaterialDialog.Builder(mainActivity)
                    .title(attachment.getName() + " (" + AttachmentsHelper.getSize(attachment) + ")")
                    .items(items.toArray(new String[items.size()]))
                    .itemsCallback((materialDialog, view, i, charSequence) ->
                            performAttachmentAction(position, i))
                    .build()
                    .show();
            return true;
        });
    }

    private void performAttachmentAction(int attachmentPosition, int i) {
        switch (getResources().getStringArray(R.array.attachments_actions_values)[i]) {
            case "share":
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                Attachment attachment = mAttachmentAdapter.getItem(attachmentPosition);
                shareIntent.setType(StorageHelper.getMimeType(WizFlow.getAppContext(), attachment.getUri()));
                shareIntent.putExtra(Intent.EXTRA_STREAM, attachment.getUri());
                if (IntentChecker.isAvailable(WizFlow.getAppContext(), shareIntent, null)) {
                    startActivity(shareIntent);
                } else {
                    mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
                }
                break;
            case "delete":
                removeAttachment(attachmentPosition);
                mAttachmentAdapter.notifyDataSetChanged();
                mGridView.autoresize();
                break;
            case "delete all":
                new MaterialDialog.Builder(mainActivity)
                        .title(R.string.delete_all_attachments)
                        .positiveText(R.string.confirm)
                        .onPositive((materialDialog, dialogAction) -> removeAllAttachments())
                        .build()
                        .show();
                break;
            case "edit":
                takeSketch(mAttachmentAdapter.getItem(attachmentPosition));
                break;
            default:
                Log.w(Constants.TAG, "No action available");
        }
    }

    private void initViewTitle() {
        title.setText(noteTmp.getTitle());
        title.gatherLinksForText();
        title.setOnTextLinkClickListener(textLinkClickListener);
        title.setOnDragListener((v, event) -> {
            return true;
        });
        title.setOnEditorActionListener((v, actionId, event) -> {
            content.requestFocus();
            content.setSelection(content.getText().length());
            return false;
        });
        requestFocus(title);
    }

    private void initViewContent() {

        content.setText(noteTmp.getContent());
        content.gatherLinksForText();
        content.setOnTextLinkClickListener(textLinkClickListener);
        content.addTextChangedListener(this);

        toggleChecklistView = content;
        if (noteTmp.isChecklist()) {
            noteTmp.setChecklist(false);
            AlphaManager.setAlpha(toggleChecklistView, 0);
            toggleChecklist2();
        }
    }

    @SuppressWarnings("JavadocReference")
    private void requestFocus(final EditText view) {
        if (note.get_id() == null && !noteTmp.isChanged(note) && showKeyboard) {
            KeyboardUtils.showKeyboard(view);
        }
    }

    private void setTagMarkerColor(Category tag) {

        String colorsPref = prefs.getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);

        if (!"disabled".equals(colorsPref)) {

            ArrayList<View> target = new ArrayList<>();
            if ("complete".equals(colorsPref)) {
                target.add(titleWrapperView);
                target.add(scrollView);
            } else {
                target.add(tagMarkerView);
            }

            if (tag != null && tag.getColor() != null) {
                for (View view : target) {
                    view.setBackgroundColor(parseInt(tag.getColor()));
                }
            } else {
                for (View view : target) {
                    view.setBackgroundColor(Color.parseColor("#00000000"));
                }
            }
        }
    }

    private void displayLocationDialog() {
        getLocation(new OnGeoUtilResultListenerImpl(mainActivity, mFragment, noteTmp));
    }

    @Override
    public void onLocationRetrieved(Location location) {
        if (location == null) {
            mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
        }
        if (location != null) {
            noteTmp.setLatitude(location.getLatitude());
            noteTmp.setLongitude(location.getLongitude());
            if (!TextUtils.isEmpty(noteTmp.getAddress())) {
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(noteTmp.getAddress());
            } else {
                GeocodeHelper.getAddressFromCoordinates(location, mFragment);
            }
        }
    }

    @Override
    public void onLocationUnavailable() {
        mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
    }

    @Override
    public void onAddressResolved(String address) {
        if (TextUtils.isEmpty(address)) {
            if (!isNoteLocationValid()) {
                mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
                return;
            }
            address = noteTmp.getLatitude() + ", " + noteTmp.getLongitude();
        }
        if (!GeocodeHelper.areCoordinates(address)) {
            noteTmp.setAddress(address);
        }
        locationTextView.setVisibility(View.VISIBLE);
        locationTextView.setText(address);
        fade(locationTextView, true);
    }

    @Override
    public void onCoordinatesResolved(Location location, String address) {
        if (location != null) {
            noteTmp.setLatitude(location.getLatitude());
            noteTmp.setLongitude(location.getLongitude());
            noteTmp.setAddress(address);
            locationTextView.setVisibility(View.VISIBLE);
            locationTextView.setText(address);
            fade(locationTextView, true);
        } else {
            mainActivity.showMessage(R.string.location_not_found, ONStyle.ALERT);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        if (searchMenuItem != null) {
            MenuItemCompat.collapseActionView(searchMenuItem);
        }

        boolean newNote = noteTmp.get_id() == null;

        menu.findItem(R.id.menu_checklist_on).setVisible(!noteTmp.isChecklist());
        menu.findItem(R.id.menu_checklist_off).setVisible(noteTmp.isChecklist());
        menu.findItem(R.id.menu_lock).setVisible(!noteTmp.isLocked());
        menu.findItem(R.id.menu_unlock).setVisible(noteTmp.isLocked());
        // If note is trashed only this options will be available from menu
        if (noteTmp.isTrashed()) {
            menu.findItem(R.id.menu_untrash).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
            // Otherwise all other actions will be available
        } else {
            menu.findItem(R.id.menu_add_shortcut).setVisible(!newNote);
            menu.findItem(R.id.menu_archive).setVisible(!newNote && !noteTmp.isArchived());
            menu.findItem(R.id.menu_unarchive).setVisible(!newNote && noteTmp.isArchived());
            menu.findItem(R.id.menu_trash).setVisible(!newNote);
        }
    }

    @SuppressLint("NewApi")
    public boolean goHome() {
        stopPlaying();

        if (!afterSavedReturnsToList) {
            if (!TextUtils.isEmpty(exitMessage)) {
                mainActivity.showToast(exitMessage, Toast.LENGTH_SHORT);
            }
            mainActivity.finish();

        } else {

            if (!TextUtils.isEmpty(exitMessage) && exitCroutonStyle != null) {
                mainActivity.showMessage(exitMessage, exitCroutonStyle);
            }

            if (mainActivity != null && mainActivity.getSupportFragmentManager() != null) {
                mainActivity.getSupportFragmentManager().popBackStack();
                if (mainActivity.getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    mainActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                    if (mainActivity.getDrawerToggle() != null) {
                        mainActivity.getDrawerToggle().setDrawerIndicatorEnabled(true);
                    }
                    EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_attachment:
                showAttachmentsPopup();
                break;
            case R.id.menu_tag:
                addTags();
                break;
            case R.id.menu_category:
                categorizeNote();
                break;
            case R.id.menu_share:
                shareNote();
                break;
            case R.id.menu_checklist_on:
                toggleChecklist();
                break;
            case R.id.menu_checklist_off:
                toggleChecklist();
                break;
            case R.id.menu_lock:
                lockNote();
                break;
            case R.id.menu_unlock:
                lockNote();
                break;
            case R.id.menu_add_shortcut:
                addShortcut();
                break;
            case R.id.menu_archive:
                archiveNote(true);
                break;
            case R.id.menu_unarchive:
                archiveNote(false);
                break;
            case R.id.menu_trash:
                trashNote(true);
                break;
            case R.id.menu_untrash:
                trashNote(false);
                break;
            case R.id.menu_discard_changes:
                discard();
                break;
            case R.id.menu_delete:
                deleteNote();
                break;
            case R.id.menu_note_info:
                showNoteInfo();
                break;
            default:
                Log.w(Constants.TAG, "Invalid menu option selected");
        }

        ((WizFlow) getActivity().getApplication()).getAnalyticsHelper().trackActionFromResourceId(getActivity(),
                item.getItemId());

        return super.onOptionsItemSelected(item);
    }

    private void showNoteInfo() {
        noteTmp.setTitle(getNoteTitle());
        noteTmp.setContent(getNoteContent());
        Intent intent = new Intent(getContext(), NoteInfosActivity.class);
        intent.putExtra(Constants.INTENT_NOTE, (android.os.Parcelable) noteTmp);
        startActivity(intent);

    }

    private void navigateUp() {
        afterSavedReturnsToList = true;
        saveAndExit(this);
    }


    private void toggleChecklist() {

        if (!noteTmp.isChecklist()) {
            toggleChecklist2();
            return;
        }

        if (mChecklistManager.getCheckedCount() == 0) {
            toggleChecklist2(true, false);
            return;
        }

        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_remove_checklist_layout,
                (ViewGroup) getView().findViewById(R.id.layout_root));

        final CheckBox keepChecked = (CheckBox) layout.findViewById(R.id.checklist_keep_checked);
        final CheckBox keepCheckmarks = (CheckBox) layout.findViewById(R.id.checklist_keep_checkmarks);
        keepChecked.setChecked(prefs.getBoolean(Constants.PREF_KEEP_CHECKED, true));
        keepCheckmarks.setChecked(prefs.getBoolean(Constants.PREF_KEEP_CHECKMARKS, true));

        new MaterialDialog.Builder(mainActivity)
                .customView(layout, false)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        prefs.edit()
                                .putBoolean(Constants.PREF_KEEP_CHECKED, keepChecked.isChecked())
                                .putBoolean(Constants.PREF_KEEP_CHECKMARKS, keepCheckmarks.isChecked())
                                .apply();
                        toggleChecklist2();
                    }
                }).build().show();
    }


    private void toggleChecklist2() {
        boolean keepChecked = prefs.getBoolean(Constants.PREF_KEEP_CHECKED, true);
        boolean showChecks = prefs.getBoolean(Constants.PREF_KEEP_CHECKMARKS, true);
        toggleChecklist2(keepChecked, showChecks);
    }

    @SuppressLint("NewApi")
    private void toggleChecklist2(final boolean keepChecked, final boolean showChecks) {

        mChecklistManager = mChecklistManager == null ? new ChecklistManager(mainActivity) : mChecklistManager;
        int checkedItemsBehavior = Integer.valueOf(prefs.getString("settings_checked_items_behavior", String.valueOf
                (it.feio.android.checklistview.Settings.CHECKED_HOLD)));
        mChecklistManager
                .showCheckMarks(showChecks)
                .newEntryHint(getString(R.string.checklist_item_hint))
                .keepChecked(keepChecked)
                .undoBarContainerView(scrollView)
                .moveCheckedOnBottom(checkedItemsBehavior);

        mChecklistManager.setOnTextLinkClickListener(textLinkClickListener);
        mChecklistManager.addTextChangedListener(mFragment);
        mChecklistManager.setCheckListChangedListener(mFragment);

        View newView = null;
        try {
            newView = mChecklistManager.convert(toggleChecklistView);
        } catch (ViewNotSupportedException e) {
            Log.e(Constants.TAG, "Error switching checklist view", e);
        }

        if (newView != null) {
            mChecklistManager.replaceViews(toggleChecklistView, newView);
            toggleChecklistView = newView;
            animate(toggleChecklistView).alpha(1).scaleXBy(0).scaleX(1).scaleYBy(0).scaleY(1);
            noteTmp.setChecklist(!noteTmp.isChecklist());
        }
    }


    private void categorizeNote() {
        final ArrayList<Category> categories = DbHelper.getInstance().getCategories();

        String currentCategory = noteTmp.getCategory() != null ? String.valueOf(noteTmp.getCategory().getId()) : null;

        final MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                .title(R.string.categorize_as)
                .adapter(new NavDrawerCategoryAdapter(mainActivity, categories, currentCategory), null)
                .positiveText(R.string.add_category)
                .positiveColorRes(R.color.colorPrimary)
                .negativeText(R.string.remove_category)
                .negativeColorRes(R.color.colorAccent)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(mainActivity, CategoryActivity.class);
                        intent.putExtra("noHome", true);
                        startActivityForResult(intent, CATEGORY);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        noteTmp.setCategory(null);
                        setTagMarkerColor(null);
                    }
                })
                .build();

        dialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
            noteTmp.setCategory(categories.get(position));
            setTagMarkerColor(categories.get(position));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAttachmentsPopup() {
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.attachment_dialog, null);

        attachmentDialog = new MaterialDialog.Builder(mainActivity)
                .autoDismiss(false)
                .customView(layout, false)
                .build();
        attachmentDialog.show();

        android.widget.TextView cameraSelection = (android.widget.TextView) layout.findViewById(R.id.camera);
        cameraSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView recordingSelection = (android.widget.TextView) layout.findViewById(R.id.recording);
        toggleAudioRecordingStop(recordingSelection);
        recordingSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView videoSelection = (android.widget.TextView) layout.findViewById(R.id.video);
        videoSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView filesSelection = (android.widget.TextView) layout.findViewById(R.id.files);
        filesSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView sketchSelection = (android.widget.TextView) layout.findViewById(R.id.sketch);
        sketchSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView locationSelection = (android.widget.TextView) layout.findViewById(R.id.location);
        locationSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView timeStampSelection = (android.widget.TextView) layout.findViewById(R.id.timestamp);
        timeStampSelection.setOnClickListener(new AttachmentOnClickListener());
        android.widget.TextView pushbulletSelection = (android.widget.TextView) layout.findViewById(R.id.pushbullet);
        pushbulletSelection.setVisibility(View.VISIBLE);
        pushbulletSelection.setOnClickListener(new AttachmentOnClickListener());
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (!IntentChecker.isAvailable(mainActivity, intent, new String[]{PackageManager.FEATURE_CAMERA})) {
            mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

            return;
        }
        File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_IMAGE_EXT);
        if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        attachmentUri = Uri.fromFile(f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (!IntentChecker.isAvailable(mainActivity, takeVideoIntent, new String[]{PackageManager.FEATURE_CAMERA})) {
            mainActivity.showMessage(R.string.feature_not_available_on_this_device, ONStyle.ALERT);

            return;
        }
        File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_VIDEO_EXT);
        if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        attachmentUri = Uri.fromFile(f);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, attachmentUri);
        String maxVideoSizeStr = "".equals(prefs.getString("settings_max_video_size",
                "")) ? "0" : prefs.getString("settings_max_video_size", "");
        long maxVideoSize = parseLong(maxVideoSizeStr) * 1024L * 1024L;
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        startActivityForResult(takeVideoIntent, TAKE_VIDEO);
    }

    private void takeSketch(Attachment attachment) {

        File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_SKETCH_EXT);
        if (f == null) {
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
            return;
        }
        attachmentUri = Uri.fromFile(f);

        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        mainActivity.animateTransition(transaction, mainActivity.TRANSITION_HORIZONTAL);
        SketchFragment mSketchFragment = new SketchFragment();
        Bundle b = new Bundle();
        b.putParcelable(MediaStore.EXTRA_OUTPUT, attachmentUri);
        if (attachment != null) {
            b.putParcelable("base", attachment.getUri());
        }
        mSketchFragment.setArguments(b);
        transaction.replace(R.id.fragment_container, mSketchFragment, mainActivity.FRAGMENT_SKETCH_TAG)
                .addToBackStack(mainActivity.FRAGMENT_DETAIL_TAG).commit();
    }

    private void addTimestamp() {
        Editable editable = content.getText();
        int position = content.getSelectionStart();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String dateStamp = dateFormat.format(new Date().getTime()) + " ";
        if (noteTmp.isChecklist()) {
            if (mChecklistManager.getFocusedItemView() != null) {
                editable = mChecklistManager.getFocusedItemView().getEditText().getEditableText();
                position = mChecklistManager.getFocusedItemView().getEditText().getSelectionStart();
            } else {
                ((CheckListView) toggleChecklistView)
                        .addItem(dateStamp, false, mChecklistManager.getCount());
            }
        }
        String leadSpace = position == 0 ? "" : " ";
        dateStamp = leadSpace + dateStamp;
        editable.insert(position, dateStamp);
        Selection.setSelection(editable, position + dateStamp.length());
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Attachment attachment;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_IMAGE);
                    addAttachment(attachment);
                    mAttachmentAdapter.notifyDataSetChanged();
                    mGridView.autoresize();
                    break;
                case TAKE_VIDEO:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_VIDEO);
                    } else {
                        attachment = new Attachment(intent.getData(), Constants.MIME_TYPE_VIDEO);
                    }
                    addAttachment(attachment);
                    mAttachmentAdapter.notifyDataSetChanged();
                    mGridView.autoresize();
                    break;
                case FILES:
                    onActivityResultManageReceivedFiles(intent);
                    break;
                case SET_PASSWORD:
                    noteTmp.setPasswordChecked(true);
                    lockUnlock();
                    break;
                case SKETCH:
                    attachment = new Attachment(attachmentUri, Constants.MIME_TYPE_SKETCH);
                    addAttachment(attachment);
                    mAttachmentAdapter.notifyDataSetChanged();
                    mGridView.autoresize();
                    break;
                case CATEGORY:
                    mainActivity.showMessage(R.string.category_saved, ONStyle.CONFIRM);
                    Category category = intent.getParcelableExtra("category");
                    noteTmp.setCategory(category);
                    setTagMarkerColor(category);
                    break;
                case DETAIL:
                    mainActivity.showMessage(R.string.note_updated, ONStyle.CONFIRM);
                    break;
                default:
                    Log.e(Constants.TAG, "Wrong element choosen: " + requestCode);
            }
        }
    }

    private void onActivityResultManageReceivedFiles(Intent intent) {
        List<Uri> uris = new ArrayList<>();
        if (Build.VERSION.SDK_INT > 16 && intent.getClipData() != null) {
            for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                uris.add(intent.getClipData().getItemAt(i).getUri());
            }
        } else {
            uris.add(intent.getData());
        }
        for (Uri uri : uris) {
            String name = FileHelper.getNameFromUri(mainActivity, uri);
            new AttachmentTask(this, uri, name, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    private void discard() {
        if (!noteTmp.getAttachmentsList().equals(note.getAttachmentsList())) {
            for (Attachment newAttachment : noteTmp.getAttachmentsList()) {
                if (!note.getAttachmentsList().contains(newAttachment)) {
                    StorageHelper.delete(mainActivity, newAttachment.getUri().getPath());
                }
            }
        }

        goBack = true;

        if (!noteTmp.equals(noteOriginal)) {
            if (noteOriginal.get_id() == null) {
                mainActivity.deleteNote(noteTmp);
                goHome();
            } else {
                new SaveNoteTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noteOriginal);
            }
            MainActivity.notifyAppWidgets(mainActivity);
        } else {
            goHome();
        }
    }

    @SuppressLint("NewApi")
    private void archiveNote(boolean archive) {
        if (noteTmp.get_id() == null) {
            goHome();
            return;
        }

        noteTmp.setArchived(archive);
        goBack = true;
        exitMessage = archive ? getString(R.string.note_archived) : getString(R.string.note_unarchived);
        exitCroutonStyle = archive ? ONStyle.WARN : ONStyle.INFO;
        saveNote(this);
    }

    @SuppressLint("NewApi")
    private void trashNote(boolean trash) {
        if (noteTmp.get_id() == null) {
            goHome();
            return;
        }

        noteTmp.setTrashed(trash);
        goBack = true;
        exitMessage = trash ? getString(R.string.note_trashed) : getString(R.string.note_untrashed);
        exitCroutonStyle = trash ? ONStyle.WARN : ONStyle.INFO;
        if (trash) {
            ShortcutHelper.removeshortCut(WizFlow.getAppContext(), noteTmp);
            ReminderHelper.removeReminder(WizFlow.getAppContext(), noteTmp);
        } else {
            ReminderHelper.addReminder(WizFlow.getAppContext(), note);
        }
        saveNote(this);
    }

    private void deleteNote() {
        new MaterialDialog.Builder(mainActivity)
                .content(R.string.delete_note_confirmation)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        mainActivity.deleteNote(noteTmp);
                        Log.d(Constants.TAG, "Deleted note with id '" + noteTmp.get_id() + "'");
                        mainActivity.showMessage(R.string.note_deleted, ONStyle.ALERT);
                        goHome();
                    }
                }).build().show();
    }

    public void saveAndExit(OnNoteSaved mOnNoteSaved) {
        if (isAdded()) {
            exitMessage = getString(R.string.note_updated);
            exitCroutonStyle = ONStyle.CONFIRM;
            goBack = true;
            saveNote(mOnNoteSaved);
        }
    }


    void saveNote(OnNoteSaved mOnNoteSaved) {

        noteTmp.setTitle(getNoteTitle());
        noteTmp.setContent(getNoteContent());

        if (goBack && TextUtils.isEmpty(noteTmp.getTitle()) && TextUtils.isEmpty(noteTmp.getContent())
                && noteTmp.getAttachmentsList().size() == 0) {
            Log.d(Constants.TAG, "Empty note not saved");
            exitMessage = getString(R.string.empty_note_not_saved);
            exitCroutonStyle = ONStyle.INFO;
            goHome();
            return;
        }

        if (saveNotNeeded()) {
            exitMessage = "";
            if (goBack) {
                goHome();
            }
            return;
        }

        noteTmp.setAttachmentsListOld(note.getAttachmentsList());

        new SaveNoteTask(mOnNoteSaved, lastModificationUpdatedNeeded()).executeOnExecutor(AsyncTask
                .THREAD_POOL_EXECUTOR, noteTmp);
    }


    private boolean saveNotNeeded() {
        if (noteTmp.get_id() == null && prefs.getBoolean(Constants.PREF_AUTO_LOCATION, false)) {
            note.setLatitude(noteTmp.getLatitude());
            note.setLongitude(noteTmp.getLongitude());
        }
        return !noteTmp.isChanged(note) || (noteTmp.isLocked() && !noteTmp.isPasswordChecked());
    }


    private boolean lastModificationUpdatedNeeded() {
        note.setCategory(noteTmp.getCategory());
        note.setArchived(noteTmp.isArchived());
        note.setTrashed(noteTmp.isTrashed());
        note.setLocked(noteTmp.isLocked());
        return noteTmp.isChanged(note);
    }

    @Override
    public void onNoteSaved(Note noteSaved) {
        MainActivity.notifyAppWidgets(WizFlow.getAppContext());
        if (!activityPausing) {
            EventBus.getDefault().post(new NotesUpdatedEvent());
            deleteMergedNotes(mergedNotesIds);
            if (noteTmp.getAlarm() != null && !noteTmp.getAlarm().equals(note.getAlarm())) {
                ReminderHelper.showReminderMessage(noteTmp.getAlarm());
            }
        }
        note = new Note(noteSaved);
        if (goBack) {
            goHome();
        }
    }

    private void deleteMergedNotes(List<String> mergedNotesIds) {
        ArrayList<Note> notesToDelete = new ArrayList<Note>();
        if (mergedNotesIds != null) {
            for (String mergedNoteId : mergedNotesIds) {
                Note note = new Note();
                note.set_id(Long.valueOf(mergedNoteId));
                notesToDelete.add(note);
            }
            new NoteProcessorDelete(notesToDelete).process();
        }
    }

    private String getNoteTitle() {
        if (title != null && !TextUtils.isEmpty(title.getText())) {
            return title.getText().toString();
        } else {
            return "";
        }
    }

    private String getNoteContent() {
        String contentText = "";
        if (!noteTmp.isChecklist()) {
            View contentView = root.findViewById(R.id.detail_content);
            if (contentView instanceof EditText) {
                contentText = ((EditText) contentView).getText().toString();
            } else if (contentView instanceof android.widget.EditText) {
                contentText = ((android.widget.EditText) contentView).getText().toString();
            }
        } else {
            if (mChecklistManager != null) {
                mChecklistManager.keepChecked(true).showCheckMarks(true);
                contentText = mChecklistManager.getText();
            }
        }
        return contentText;
    }


    private void shareNote() {
        Note sharedNote = new Note(noteTmp);
        sharedNote.setTitle(getNoteTitle());
        sharedNote.setContent(getNoteContent());
        mainActivity.shareNote(sharedNote);
    }


    private void lockNote() {
        Log.d(Constants.TAG, "Locking or unlocking note " + note.get_id());

        if (prefs.getString(Constants.PREF_PASSWORD, null) == null) {
            Intent passwordIntent = new Intent(mainActivity, PasswordActivity.class);
            startActivityForResult(passwordIntent, SET_PASSWORD);
            return;
        }

        if (noteTmp.isPasswordChecked() || prefs.getBoolean("settings_password_access", false)) {
            lockUnlock();
            return;
        }

        PasswordHelper.requestPassword(mainActivity, passwordConfirmed -> {
            if (passwordConfirmed) {
                lockUnlock();
            }
        });
    }

    private void lockUnlock() {
        if (prefs.getString(Constants.PREF_PASSWORD, null) == null) {
            mainActivity.showMessage(R.string.password_not_set, ONStyle.WARN);
            return;
        }
        mainActivity.showMessage(R.string.save_note_to_lock_it, ONStyle.INFO);
        mainActivity.supportInvalidateOptionsMenu();
        noteTmp.setLocked(!noteTmp.isLocked());
        noteTmp.setPasswordChecked(true);
    }


    private String initReminder(Note note) {
        if (noteTmp.getAlarm() == null) {
            return "";
        }
        long reminder = parseLong(note.getAlarm());
        String rrule = note.getRecurrenceRule();
        if (!TextUtils.isEmpty(rrule)) {
            return DateHelper.getNoteRecurrentReminderText(reminder, rrule);
        } else {
            return DateHelper.getNoteReminderText(reminder);
        }
    }


    private void playback(View v, Uri uri) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            if (isPlayingView != v) {
                stopPlaying();
                isPlayingView = v;
                startPlaying(uri);
                replacePlayingAudioBitmap(v);
            } else {
                stopPlaying();
            }
        } else {
            isPlayingView = v;
            startPlaying(uri);
            replacePlayingAudioBitmap(v);
        }
    }

    private void replacePlayingAudioBitmap(View v) {
        Drawable d = ((ImageView) v.findViewById(R.id.gridview_item_picture)).getDrawable();
        if (BitmapDrawable.class.isAssignableFrom(d.getClass())) {
            recordingBitmap = ((BitmapDrawable) d).getBitmap();
        } else {
            recordingBitmap = ((GlideBitmapDrawable) d.getCurrent()).getBitmap();
        }
        ((ImageView) v.findViewById(R.id.gridview_item_picture)).setImageBitmap(ThumbnailUtils
                .extractThumbnail(BitmapFactory.decodeResource(mainActivity.getResources(),
                        R.drawable.stop), Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE));
    }

    private void startPlaying(Uri uri) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.setDataSource(mainActivity, uri);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(mp -> {
                mPlayer = null;
                if (isPlayingView != null) {
                    ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap
                            (recordingBitmap);
                    recordingBitmap = null;
                    isPlayingView = null;
                }
            });
        } catch (IOException e) {
            Log.e(Constants.TAG, "prepare() failed", e);
            mainActivity.showMessage(R.string.error, ONStyle.ALERT);
        }
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            if (isPlayingView != null) {
                ((ImageView) isPlayingView.findViewById(R.id.gridview_item_picture)).setImageBitmap(recordingBitmap);
            }
            isPlayingView = null;
            recordingBitmap = null;
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startRecording(View v) {
        PermissionsHelper.requestPermission(getActivity(), Manifest.permission.RECORD_AUDIO,
                R.string.permission_audio_recording, snackBarPlaceholder, () -> {

                    isRecording = true;
                    toggleAudioRecordingStop(v);

                    File f = StorageHelper.createNewAttachmentFile(mainActivity, Constants.MIME_TYPE_AUDIO_EXT);
                    if (f == null) {
                        mainActivity.showMessage(R.string.error, ONStyle.ALERT);
                        return;
                    }
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        mRecorder.setAudioEncodingBitRate(96000);
                        mRecorder.setAudioSamplingRate(44100);
                    }
                    recordName = f.getAbsolutePath();
                    mRecorder.setOutputFile(recordName);

                    try {
                        audioRecordingTimeStart = Calendar.getInstance().getTimeInMillis();
                        mRecorder.prepare();
                        mRecorder.start();
                    } catch (IOException | IllegalStateException e) {
                        Log.e(Constants.TAG, "prepare() failed", e);
                        mainActivity.showMessage(R.string.error, ONStyle.ALERT);
                    }
                });
    }

    private void toggleAudioRecordingStop(View v) {
        if (isRecording) {
            ((android.widget.TextView) v).setText(getString(R.string.stop));
            ((android.widget.TextView) v).setTextColor(Color.parseColor("#ff0000"));
        }
    }

    private void stopRecording() {
        isRecording = false;
        if (mRecorder != null) {
            mRecorder.stop();
            audioRecordingTime = Calendar.getInstance().getTimeInMillis() - audioRecordingTimeStart;
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void fade(final View v, boolean fadeIn) {

        int anim = R.animator.fade_out_support;
        int visibilityTemp = View.GONE;

        if (fadeIn) {
            anim = R.animator.fade_in_support;
            visibilityTemp = View.VISIBLE;
        }

        final int visibility = visibilityTemp;

        if (mainActivity != null) {
            Animation mAnimation = AnimationUtils.loadAnimation(mainActivity, anim);
            mAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(visibility);
                }
            });
            v.startAnimation(mAnimation);
        }
    }


    private void addShortcut() {
        ShortcutHelper.addShortcut(WizFlow.getAppContext(), noteTmp);
        mainActivity.showMessage(R.string.shortcut_added, ONStyle.INFO);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                Log.v(Constants.TAG, "MotionEvent.ACTION_DOWN");
                int w;

                Point displaySize = Display.getUsableSize(mainActivity);
                w = displaySize.x;

                if (x < Constants.SWIPE_MARGIN || x > w - Constants.SWIPE_MARGIN) {
                    swiping = true;
                    startSwipeX = x;
                }

                break;

            case MotionEvent.ACTION_UP:
                Log.v(Constants.TAG, "MotionEvent.ACTION_UP");
                if (swiping)
                    swiping = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (swiping) {
                    Log.v(Constants.TAG, "MotionEvent.ACTION_MOVE at position " + x + ", " + y);
                    if (Math.abs(x - startSwipeX) > Constants.SWIPE_OFFSET) {
                        swiping = false;
                        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
                        mainActivity.animateTransition(transaction, mainActivity.TRANSITION_VERTICAL);
                        DetailFragment mDetailFragment = new DetailFragment();
                        Bundle b = new Bundle();
                        b.putParcelable(Constants.INTENT_NOTE, new Note());
                        mDetailFragment.setArguments(b);
                        transaction.replace(R.id.fragment_container, mDetailFragment,
                                mainActivity.FRAGMENT_DETAIL_TAG).addToBackStack(mainActivity
                                .FRAGMENT_DETAIL_TAG).commit();
                    }
                }
                break;

            default:
                Log.e(Constants.TAG, "Wrong element choosen: " + event.getAction());
        }

        return true;
    }

    @Override
    public void onAttachingFileErrorOccurred(Attachment mAttachment) {
        mainActivity.showMessage(R.string.error_saving_attachments, ONStyle.ALERT);
        if (noteTmp.getAttachmentsList().contains(mAttachment)) {
            removeAttachment(mAttachment);
            mAttachmentAdapter.notifyDataSetChanged();
            mGridView.autoresize();
        }
    }

    private void addAttachment(Attachment attachment) {
        noteTmp.addAttachment(attachment);
    }

    private void removeAttachment(Attachment mAttachment) {
        noteTmp.removeAttachment(mAttachment);
    }

    private void removeAttachment(int position) {
        noteTmp.removeAttachment(noteTmp.getAttachmentsList().get(position));
    }

    private void removeAllAttachments() {
        noteTmp.setAttachmentsList(new ArrayList<>());
        mAttachmentAdapter = new AttachmentAdapter(mainActivity, new ArrayList<>(), mGridView);
        mGridView.invalidateViews();
        mGridView.setAdapter(mAttachmentAdapter);
    }

    @Override
    public void onAttachingFileFinished(Attachment mAttachment) {
        addAttachment(mAttachment);
        mAttachmentAdapter.notifyDataSetChanged();
        mGridView.autoresize();
    }

    @Override
    public void onReminderPicked(long reminder) {
        noteTmp.setAlarm(reminder);
        if (mFragment.isAdded()) {
            reminderIcon.setImageResource(R.drawable.ic_alarm_black_18dp);
            datetime.setText(DateHelper.getNoteReminderText(reminder));
        }
    }

    @Override
    public void onRecurrenceReminderPicked(String recurrenceRule) {
        noteTmp.setRecurrenceRule(recurrenceRule);
        if (!TextUtils.isEmpty(recurrenceRule)) {
            Log.d(Constants.TAG, "Recurrent reminder set: " + recurrenceRule);
            datetime.setText(DateHelper.getNoteRecurrentReminderText(parseLong(noteTmp
                    .getAlarm()), recurrenceRule));
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        scrollContent();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onCheckListChanged() {
        scrollContent();
    }

    private void scrollContent() {
        if (noteTmp.isChecklist()) {
            if (mChecklistManager.getCount() > contentLineCounter) {
                scrollView.scrollBy(0, 60);
            }
            contentLineCounter = mChecklistManager.getCount();
        } else {
            if (content.getLineCount() > contentLineCounter) {
                scrollView.scrollBy(0, 60);
            }
            contentLineCounter = content.getLineCount();
        }
    }

    private void addTags() {
        contentCursorPosition = getCursorIndex();

        final List<Tag> tags = TagsHelper.getAllTags();

        if (tags.size() == 0) {
            mainActivity.showMessage(R.string.no_tags_created, ONStyle.WARN);
            return;
        }

        final Note currentNote = new Note();
        currentNote.setTitle(getNoteTitle());
        currentNote.setContent(getNoteContent());
        Integer[] preselectedTags = TagsHelper.getPreselectedTagsArray(currentNote, tags);

        MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                .title(R.string.select_tags)
                .positiveText(R.string.ok)
                .items(TagsHelper.getTagsArray(tags))
                .itemsCallbackMultiChoice(preselectedTags, (dialog1, which, text) -> {
                    dialog1.dismiss();
                    tagNote(tags, which, currentNote);
                    return false;
                }).build();
        dialog.show();
    }

    private void tagNote(List<Tag> tags, Integer[] selectedTags, Note note) {
        Pair<String, List<Tag>> taggingResult = TagsHelper.addTagToNote(tags, selectedTags, note);

        StringBuilder sb;
        if (noteTmp.isChecklist()) {
            CheckListViewItem mCheckListViewItem = mChecklistManager.getFocusedItemView();
            if (mCheckListViewItem != null) {
                sb = new StringBuilder(mCheckListViewItem.getText());
                sb.insert(contentCursorPosition, " " + taggingResult.first + " ");
                mCheckListViewItem.setText(sb.toString());
                mCheckListViewItem.getEditText().setSelection(contentCursorPosition + taggingResult.first.length()
                        + 1);
            } else {
                title.append(" " + taggingResult.first);
            }
        } else {
            sb = new StringBuilder(getNoteContent());
            if (content.hasFocus()) {
                sb.insert(contentCursorPosition, " " + taggingResult.first + " ");
                content.setText(sb.toString());
                content.setSelection(contentCursorPosition + taggingResult.first.length() + 1);
            } else {
                if (getNoteContent().trim().length() > 0) {
                    sb.append(System.getProperty("line.separator"))
                            .append(System.getProperty("line.separator"));
                }
                sb.append(taggingResult.first);
                content.setText(sb.toString());
            }
        }

        if (taggingResult.second.size() > 0) {
            if (noteTmp.isChecklist()) {
                toggleChecklist2(true, true);
            }
            Pair<String, String> titleAndContent = TagsHelper.removeTag(getNoteTitle(), getNoteContent(),
                    taggingResult.second);
            title.setText(titleAndContent.first);
            content.setText(titleAndContent.second);
            if (noteTmp.isChecklist()) {
                toggleChecklist2();
            }
        }
    }

    private int getCursorIndex() {
        if (!noteTmp.isChecklist()) {
            return content.getSelectionStart();
        } else {
            CheckListViewItem mCheckListViewItem = mChecklistManager.getFocusedItemView();
            if (mCheckListViewItem != null) {
                return mCheckListViewItem.getEditText().getSelectionStart();
            } else {
                return 0;
            }
        }
    }

    public Note getCurrentNote() {
        return note;
    }

    private boolean isNoteLocationValid() {
        return noteTmp.getLatitude() != null
                && noteTmp.getLatitude() != 0
                && noteTmp.getLongitude() != null
                && noteTmp.getLongitude() != 0;
    }

    public void startGetContentAction() {
        Intent filesIntent;
        filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
        filesIntent.setType("*/*");
        startActivityForResult(filesIntent, FILES);
    }

    private void askReadExternalStoragePermission() {
        PermissionsHelper.requestPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE,
                R.string.permission_external_storage_detail_attachment,
                snackBarPlaceholder, this::startGetContentAction);
    }

    public void onEventMainThread(PushbulletReplyEvent pushbulletReplyEvent) {
        String text = getNoteContent() + System.getProperty("line.separator") + pushbulletReplyEvent.message;
        content.setText(text);
    }

    private static class OnGeoUtilResultListenerImpl implements OnGeoUtilResultListener {

        private final WeakReference<MainActivity> mainActivityWeakReference;
        private final WeakReference<DetailFragment> detailFragmentWeakReference;
        private final WeakReference<Note> noteTmpWeakReference;

        OnGeoUtilResultListenerImpl(MainActivity activity, DetailFragment mFragment, Note noteTmp) {
            this.mainActivityWeakReference = new WeakReference<>(activity);
            this.detailFragmentWeakReference = new WeakReference<>(mFragment);
            this.noteTmpWeakReference = new WeakReference<>(noteTmp);
        }

        @Override
        public void onAddressResolved(String address) {
        }

        @Override
        public void onCoordinatesResolved(Location location, String address) {
        }

        @Override
        public void onLocationUnavailable() {
            mainActivityWeakReference.get().showMessage(R.string.location_not_found, ONStyle.ALERT);
        }

        @Override
        public void onLocationRetrieved(Location location) {

            if (!checkWeakReferences()) {
                return;
            }

            if (location == null) {
                return;
            }
            if (!ConnectionManager.internetAvailable(mainActivityWeakReference.get())) {
                noteTmpWeakReference.get().setLatitude(location.getLatitude());
                noteTmpWeakReference.get().setLongitude(location.getLongitude());
                onAddressResolved("");
                return;
            }
            LayoutInflater inflater = mainActivityWeakReference.get().getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_location, null);
            final AutoCompleteTextView autoCompView = (AutoCompleteTextView) v.findViewById(R.id
                    .auto_complete_location);
            autoCompView.setHint(mainActivityWeakReference.get().getString(R.string.search_location));
            autoCompView.setAdapter(new PlacesAutoCompleteAdapter(mainActivityWeakReference.get(), R.layout
                    .simple_text_layout));
            final MaterialDialog dialog = new MaterialDialog.Builder(mainActivityWeakReference.get())
                    .customView(autoCompView, false)
                    .positiveText(R.string.use_current_location)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog materialDialog) {
                            if (TextUtils.isEmpty(autoCompView.getText().toString())) {
                                noteTmpWeakReference.get().setLatitude(location.getLatitude());
                                noteTmpWeakReference.get().setLongitude(location.getLongitude());
                                GeocodeHelper.getAddressFromCoordinates(location, detailFragmentWeakReference.get());
                            } else {
                                GeocodeHelper.getCoordinatesFromAddress(autoCompView.getText().toString(),
                                        detailFragmentWeakReference.get());
                            }
                        }
                    })
                    .build();
            autoCompView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() != 0) {
                        dialog.setActionButton(DialogAction.POSITIVE, mainActivityWeakReference.get().getString(R
                                .string.confirm));
                    } else {
                        dialog.setActionButton(DialogAction.POSITIVE, mainActivityWeakReference.get().getString(R
                                .string
                                .use_current_location));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            dialog.show();
        }

        private boolean checkWeakReferences() {
            return mainActivityWeakReference.get() != null && !mainActivityWeakReference.get().isFinishing()
                    && detailFragmentWeakReference.get() != null && noteTmpWeakReference.get() != null;
        }
    }

    @SuppressLint("InlinedApi")
    private class AttachmentOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.camera:
                    takePhoto();
                    break;
                case R.id.recording:
                    if (!isRecording) {
                        startRecording(v);
                    } else {
                        stopRecording();
                        Attachment attachment = new Attachment(Uri.fromFile(new File(recordName)), Constants
                                .MIME_TYPE_AUDIO);
                        attachment.setLength(audioRecordingTime);
                        addAttachment(attachment);
                        mAttachmentAdapter.notifyDataSetChanged();
                        mGridView.autoresize();
                    }
                    break;
                case R.id.video:
                    takeVideo();
                    break;
                case R.id.files:
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        startGetContentAction();
                    } else {
                        askReadExternalStoragePermission();
                    }
                    break;
                case R.id.sketch:
                    takeSketch(null);
                    break;
                case R.id.location:
                    displayLocationDialog();
                    break;
                case R.id.timestamp:
                    addTimestamp();
                    break;
                case R.id.pushbullet:
                    MessagingExtension.mirrorMessage(mainActivity, getString(R.string.app_name),
                            getString(R.string.pushbullet),
                            getNoteContent(), BitmapFactory.decodeResource(getResources(),
                                    R.drawable.ic_stat_literal_icon),
                            null, 0);
                    break;
                default:
                    Log.e(Constants.TAG, "Wrong element choosen: " + v.getId());
            }
            if (!isRecording) attachmentDialog.dismiss();
        }
    }
}



