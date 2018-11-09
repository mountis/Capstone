
package developer.elkane.udacity.wizflow.utils;

public interface ConstantsBase {

    String DATABASE_NAME = "wizflow";
    String APP_STORAGE_DIRECTORY_SB_SYNC = "db_sync";

    long UPDATE_MIN_FREQUENCY = 24L * 60L * 60L * 1000L; // 1 day

    int SWIPE_MARGIN = 30;
    int SWIPE_OFFSET = 100;

    int FAB_ANIMATION_TIME = 200;

    String MASK_CHAR = "*";

    int THUMBNAIL_SIZE = 300;

    String DATE_FORMAT_SORTABLE = "yyyyMMdd_HHmmss_SSS";
    String DATE_FORMAT_SORTABLE_OLD = "yyyyMMddHHmmss";
    String DATE_FORMAT_EXPORT = "yyyy.MM.dd-HH.mm";

    String INTENT_KEY = "note_id";
    String INTENT_NOTE = "note";
    String GALLERY_TITLE = "gallery_title";
    String GALLERY_CLICKED_IMAGE = "gallery_clicked_image";
    String GALLERY_IMAGES = "gallery_images";
    String INTENT_CATEGORY = "category";
    String INTENT_GOOGLE_NOW = "com.google.android.gm.action.AUTO_SEND";
    String INTENT_WIDGET = "widget_id";
    String INTENT_UPDATE_DASHCLOCK = "update_dashclock";

    String ACTION_START_APP = "action_start_app";
    String ACTION_RESTART_APP = "action_restart_app";
    String ACTION_DISMISS = "action_dismiss";
    String ACTION_SNOOZE = "action_snooze";
    String ACTION_POSTPONE = "action_postpone";
    String ACTION_SHORTCUT = "action_shortcut";
    String ACTION_WIDGET = "action_widget";
    String ACTION_WIDGET_TAKE_PHOTO = "action_widget_take_photo";
    String ACTION_WIDGET_SHOW_LIST = "action_widget_show_list";
    String ACTION_SHORTCUT_WIDGET = "action_shortcut_widget";
    String ACTION_NOTIFICATION_CLICK = "action_notification_click";
    String ACTION_MERGE = "action_merge";
    String ACTION_FAB_TAKE_PHOTO = "action_fab_take_photo";

    String ACTION_SEND_AND_EXIT = "action_send_and_exit";
    String ACTION_SEARCH_UNCOMPLETE_CHECKLISTS = "action_search_uncomplete_checklists";

    String PREF_LANG = "settings_language";
    String PREF_LAST_UPDATE_CHECK = "last_update_check";
    String PREF_NAVIGATION = "navigation";
    String PREF_SORTING_COLUMN = "sorting_column";
    String PREF_PASSWORD = "password";
    String PREF_PASSWORD_QUESTION = "password_question";
    String PREF_PASSWORD_ANSWER = "password_answer";
    String PREF_KEEP_CHECKED = "keep_checked";
    String PREF_KEEP_CHECKMARKS = "show_checkmarks";
    String PREF_EXPANDED_VIEW = "expanded_view";
    String PREF_COLORS_APP_DEFAULT = "strip";
    String PREF_WIDGET_PREFIX = "widget_";
    String PREF_SHOW_UNCATEGORIZED = "settings_show_uncategorized";
    String PREF_AUTO_LOCATION = "settings_auto_location";
    String PREF_FILTER_PAST_REMINDERS = "settings_filter_past_reminders";
    String PREF_FILTER_ARCHIVED_IN_CATEGORIES = "settings_filter_archived_in_categories";
    String PREF_DYNAMIC_MENU = "settings_dynamic_menu";
    String PREF_CURRENT_APP_VERSION = "settings_current_app_version";
    String PREF_FAB_EXPANSION_BEHAVIOR = "settings_fab_expansion_behavior";
    String PREF_ATTANCHEMENTS_ON_BOTTOM = "settings_attachments_on_bottom";
    String PREF_SNOOZE_DEFAULT = "10";
    String PREF_TOUR_COMPLETE = "pref_tour_complete";
    String PREF_ENABLE_SWIPE = "settings_enable_swipe";
    String PREF_SEND_ANALYTICS = "settings_send_analytics";
    String PREF_PRETTIFIED_DATES = "settings_prettified_dates";

    String MIME_TYPE_IMAGE = "image/jpeg";
    String MIME_TYPE_AUDIO = "audio/amr";
    String MIME_TYPE_VIDEO = "video/mp4";
    String MIME_TYPE_SKETCH = "image/png";
    String MIME_TYPE_FILES = "file/*";

    String MIME_TYPE_IMAGE_EXT = ".jpeg";
    String MIME_TYPE_AUDIO_EXT = ".amr";
    String MIME_TYPE_VIDEO_EXT = ".mp4";
    String MIME_TYPE_SKETCH_EXT = ".png";
    String MIME_TYPE_CONTACT_EXT = ".vcf";

    String TIMESTAMP_UNIX_EPOCH = "0";
    String TIMESTAMP_UNIX_EPOCH_FAR = "18464193800000";

    int MENU_SORT_GROUP_ID = 11998811;

    String MERGED_NOTES_SEPARATOR = "----------------------";
    String PROPERTIES_PARAMS_SEPARATOR = ",";
}
