
package developer.elkane.udacity.wizflow.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.async.upgrade.UpgradeProcessor;
import developer.elkane.udacity.wizflow.helpers.NotesHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.Stats;
import developer.elkane.udacity.wizflow.models.Tag;
import developer.elkane.udacity.wizflow.utils.AssetUtils;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Navigation;
import developer.elkane.udacity.wizflow.utils.Security;
import developer.elkane.udacity.wizflow.utils.TagsHelper;


public class DbHelper extends SQLiteOpenHelper {

    public static final String TABLE_NOTES = "notes";
    public static final String KEY_ID = "creation";
    public static final String KEY_CREATION = "creation";
    public static final String KEY_LAST_MODIFICATION = "last_modification";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_ARCHIVED = "archived";
    public static final String KEY_TRASHED = "trashed";
    public static final String KEY_REMINDER = "alarm";
    public static final String KEY_REMINDER_FIRED = "reminder_fired";
    public static final String KEY_RECURRENCE_RULE = "recurrence_rule";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CATEGORY = "category_id";
    public static final String KEY_LOCKED = "locked";
    public static final String KEY_CHECKLIST = "checklist";
    public static final String TABLE_ATTACHMENTS = "attachments";
    public static final String KEY_ATTACHMENT_ID = "attachment_id";
    public static final String KEY_ATTACHMENT_URI = "uri";
    public static final String KEY_ATTACHMENT_NAME = "name";
    public static final String KEY_ATTACHMENT_SIZE = "size";
    public static final String KEY_ATTACHMENT_LENGTH = "length";
    public static final String KEY_ATTACHMENT_MIME_TYPE = "mime_type";
    public static final String KEY_ATTACHMENT_NOTE_ID = "note_id";
    public static final String TABLE_CATEGORY = "categories";
    public static final String KEY_CATEGORY_ID = "category_id";
    public static final String KEY_CATEGORY_NAME = "name";
    public static final String KEY_CATEGORY_DESCRIPTION = "description";
    public static final String KEY_CATEGORY_COLOR = "color";
    private static final String DATABASE_NAME = Constants.DATABASE_NAME;
    private static final int DATABASE_VERSION = 501;
    private static final String SQL_DIR = "sql";
    private static final String CREATE_QUERY = "create.sql";
    private static final String UPGRADE_QUERY_PREFIX = "upgrade-";
    private static final String UPGRADE_QUERY_SUFFIX = ".sql";
    private static DbHelper instance = null;
    private final Context mContext;
    private final SharedPreferences prefs;
    private SQLiteDatabase db;


    private DbHelper(Context mContext) {
        super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = mContext;
        this.prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }

    public static synchronized DbHelper getInstance() {
        return getInstance(WizFlow.getAppContext());
    }

    public static synchronized DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context);
        }
        return instance;
    }

    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    public SQLiteDatabase getDatabase() {
        return getDatabase(false);
    }


    public SQLiteDatabase getDatabase(boolean forceWritable) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            if (db.isReadOnly() && forceWritable) {
                db = getWritableDatabase();
            }
            return db;
        } catch (IllegalStateException e) {
            return this.db;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.i(Constants.TAG, "Database creation");
            execSqlFile(CREATE_QUERY, db);
        } catch (IOException exception) {
            throw new RuntimeException("Database creation failed", exception);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.db = db;
        Log.i(Constants.TAG, "Upgrading database version from " + oldVersion + " to " + newVersion);

        UpgradeProcessor.process(oldVersion, newVersion);

        try {
            for (String sqlFile : AssetUtils.list(SQL_DIR, mContext.getAssets())) {
                if (sqlFile.startsWith(UPGRADE_QUERY_PREFIX)) {
                    int fileVersion = Integer.parseInt(sqlFile.substring(UPGRADE_QUERY_PREFIX.length(),
                            sqlFile.length() - UPGRADE_QUERY_SUFFIX.length()));
                    if (fileVersion > oldVersion && fileVersion <= newVersion) {
                        execSqlFile(sqlFile, db);
                    }
                }
            }
            Log.i(Constants.TAG, "Database upgrade successful");

        } catch (IOException e) {
            throw new RuntimeException("Database upgrade failed", e);
        }
    }


    public Note updateNote(Note note, boolean updateLastModification) {
        SQLiteDatabase db = getDatabase(true);

        String content;
        if (note.isLocked()) {
            content = Security.encrypt(note.getContent(), prefs.getString(Constants.PREF_PASSWORD, ""));
        } else {
            content = note.getContent();
        }

        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_CONTENT, content);
        values.put(KEY_CREATION, note.getCreation() != null ? note.getCreation() : Calendar.getInstance()
                .getTimeInMillis());
        values.put(KEY_LAST_MODIFICATION, updateLastModification ? Calendar
                .getInstance().getTimeInMillis() : (note.getLastModification() != null ? note.getLastModification() :
                Calendar
                        .getInstance().getTimeInMillis()));
        values.put(KEY_ARCHIVED, note.isArchived());
        values.put(KEY_TRASHED, note.isTrashed());
        values.put(KEY_REMINDER, note.getAlarm());
        values.put(KEY_REMINDER_FIRED, note.isReminderFired());
        values.put(KEY_RECURRENCE_RULE, note.getRecurrenceRule());
        values.put(KEY_LATITUDE, note.getLatitude());
        values.put(KEY_LONGITUDE, note.getLongitude());
        values.put(KEY_ADDRESS, note.getAddress());
        values.put(KEY_CATEGORY, note.getCategory() != null ? note.getCategory().getId() : null);
        boolean locked = note.isLocked() != null ? note.isLocked() : false;
        values.put(KEY_LOCKED, locked);
        boolean checklist = note.isChecklist() != null ? note.isChecklist() : false;
        values.put(KEY_CHECKLIST, checklist);

        db.insertWithOnConflict(TABLE_NOTES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(Constants.TAG, "Updated note titled '" + note.getTitle() + "'");

        List<Attachment> deletedAttachments = note.getAttachmentsListOld();
        for (Attachment attachment : note.getAttachmentsList()) {
            updateAttachment(note.get_id() != null ? note.get_id() : values.getAsLong(KEY_CREATION), attachment, db);
            deletedAttachments.remove(attachment);
        }
        for (Attachment attachmentDeleted : deletedAttachments) {
            db.delete(TABLE_ATTACHMENTS, KEY_ATTACHMENT_ID + " = ?",
                    new String[]{String.valueOf(attachmentDeleted.getId())});
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        note.setCreation(note.getCreation() != null ? note.getCreation() : values.getAsLong(KEY_CREATION));
        note.setLastModification(values.getAsLong(KEY_LAST_MODIFICATION));

        return note;
    }


    protected void execSqlFile(String sqlFile, SQLiteDatabase db) throws SQLException, IOException {
        Log.i(Constants.TAG, "  exec sql file: {}" + sqlFile);
        for (String sqlInstruction : SqlParser.parseSqlFile(SQL_DIR + "/" + sqlFile, mContext.getAssets())) {
            Log.v(Constants.TAG, "    sql: {}" + sqlInstruction);
            try {
                db.execSQL(sqlInstruction);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error executing command: " + sqlInstruction, e);
            }
        }
    }


    public Attachment updateAttachment(Attachment attachment) {
        return updateAttachment(-1, attachment, getDatabase(true));
    }


    public Attachment updateAttachment(long noteId, Attachment attachment, SQLiteDatabase db) {
        ContentValues valuesAttachments = new ContentValues();
        valuesAttachments.put(KEY_ATTACHMENT_ID, attachment.getId() != null ? attachment.getId() : Calendar
                .getInstance().getTimeInMillis());
        valuesAttachments.put(KEY_ATTACHMENT_NOTE_ID, noteId);
        valuesAttachments.put(KEY_ATTACHMENT_URI, attachment.getUri().toString());
        valuesAttachments.put(KEY_ATTACHMENT_MIME_TYPE, attachment.getMime_type());
        valuesAttachments.put(KEY_ATTACHMENT_NAME, attachment.getName());
        valuesAttachments.put(KEY_ATTACHMENT_SIZE, attachment.getSize());
        valuesAttachments.put(KEY_ATTACHMENT_LENGTH, attachment.getLength());
        db.insertWithOnConflict(TABLE_ATTACHMENTS, KEY_ATTACHMENT_ID, valuesAttachments, SQLiteDatabase.CONFLICT_REPLACE);
        return attachment;
    }


    public Note getNote(long id) {

        String whereCondition = " WHERE "
                + KEY_ID + " = " + id;

        List<Note> notes = getNotes(whereCondition, true);
        Note note;
        if (notes.size() > 0) {
            note = notes.get(0);
        } else {
            note = null;
        }
        return note;
    }


    public List<Note> getAllNotes(Boolean checkNavigation) {
        String whereCondition = "";
        if (checkNavigation) {
            int navigation = Navigation.getNavigation();
            switch (navigation) {
                case Navigation.NOTES:
                    return getNotesActive();
                case Navigation.ARCHIVE:
                    return getNotesArchived();
                case Navigation.REMINDERS:
                    return getNotesWithReminder(prefs.getBoolean(Constants.PREF_FILTER_PAST_REMINDERS, false));
                case Navigation.TRASH:
                    return getNotesTrashed();
                case Navigation.UNCATEGORIZED:
                    return getNotesUncategorized();
                case Navigation.CATEGORY:
                    return getNotesByCategory(Navigation.getCategory());
                default:
                    return getNotes(whereCondition, true);
            }
        } else {
            return getNotes(whereCondition, true);
        }

    }


    public List<Note> getNotesActive() {
        String whereCondition = " WHERE " + KEY_ARCHIVED + " IS NOT 1 AND " + KEY_TRASHED + " IS NOT 1 ";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesArchived() {
        String whereCondition = " WHERE " + KEY_ARCHIVED + " = 1 AND " + KEY_TRASHED + " IS NOT 1 ";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesTrashed() {
        String whereCondition = " WHERE " + KEY_TRASHED + " = 1 ";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesUncategorized() {
        String whereCondition = " WHERE "
                + "(" + KEY_CATEGORY_ID + " IS NULL OR " + KEY_CATEGORY_ID + " == 0) "
                + "AND " + KEY_TRASHED + " IS NOT 1";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesWithLocation() {
        String whereCondition = " WHERE " + KEY_LONGITUDE + " IS NOT NULL "
                + "AND " + KEY_LONGITUDE + " != 0 ";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotes(String whereCondition, boolean order) {
        List<Note> noteList = new ArrayList<>();

        String sort_column, sort_order = "";

        if (Navigation.checkNavigation(Navigation.REMINDERS)) {
            sort_column = KEY_REMINDER;
        } else {
            sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, KEY_TITLE);
        }
        if (order) {
            sort_order = KEY_TITLE.equals(sort_column) || KEY_REMINDER.equals(sort_column) ? " ASC " : " DESC ";
        }

        sort_column = KEY_TITLE.equals(sort_column) ? KEY_TITLE + "||" + KEY_CONTENT : sort_column;

        sort_column = KEY_REMINDER.equals(sort_column) ? "IFNULL(" + KEY_REMINDER + ", " +
                "" + Constants.TIMESTAMP_UNIX_EPOCH + ")" : sort_column;

        String query = "SELECT "
                + KEY_CREATION + ","
                + KEY_LAST_MODIFICATION + ","
                + KEY_TITLE + ","
                + KEY_CONTENT + ","
                + KEY_ARCHIVED + ","
                + KEY_TRASHED + ","
                + KEY_REMINDER + ","
                + KEY_REMINDER_FIRED + ","
                + KEY_RECURRENCE_RULE + ","
                + KEY_LATITUDE + ","
                + KEY_LONGITUDE + ","
                + KEY_ADDRESS + ","
                + KEY_LOCKED + ","
                + KEY_CHECKLIST + ","
                + KEY_CATEGORY + ","
                + KEY_CATEGORY_NAME + ","
                + KEY_CATEGORY_DESCRIPTION + ","
                + KEY_CATEGORY_COLOR
                + " FROM " + TABLE_NOTES
                + " LEFT JOIN " + TABLE_CATEGORY + " USING( " + KEY_CATEGORY + ") "
                + whereCondition
                + (order ? " ORDER BY " + sort_column + sort_order : "");

        Log.v(Constants.TAG, "Query: " + query);

        Cursor cursor = null;
        try {
            cursor = getDatabase().rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    int i = 0;
                    Note note = new Note();
                    note.setCreation(cursor.getLong(i++));
                    note.setLastModification(cursor.getLong(i++));
                    note.setTitle(cursor.getString(i++));
                    note.setContent(cursor.getString(i++));
                    note.setArchived("1".equals(cursor.getString(i++)));
                    note.setTrashed("1".equals(cursor.getString(i++)));
                    note.setAlarm(cursor.getString(i++));
                    note.setReminderFired(cursor.getInt(i++));
                    note.setRecurrenceRule(cursor.getString(i++));
                    note.setLatitude(cursor.getString(i++));
                    note.setLongitude(cursor.getString(i++));
                    note.setAddress(cursor.getString(i++));
                    note.setLocked("1".equals(cursor.getString(i++)));
                    note.setChecklist("1".equals(cursor.getString(i++)));

                    if (note.isLocked()) {
                        note.setContent(Security.decrypt(note.getContent(), prefs.getString(Constants.PREF_PASSWORD,
                                "")));
                    }

                    long categoryId = cursor.getLong(i++);
                    if (categoryId != 0) {
                        Category category = new Category(categoryId, cursor.getString(i++),
                                cursor.getString(i++), cursor.getString(i++));
                        note.setCategory(category);
                    }

                    note.setAttachmentsList(getNoteAttachments(note));

                    noteList.add(note);

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }

        Log.v(Constants.TAG, "Query: Retrieval finished!");
        return noteList;
    }


    public void archiveNote(Note note, boolean archive) {
        note.setArchived(archive);
        updateNote(note, false);
    }


    public void trashNote(Note note, boolean trash) {
        note.setTrashed(trash);
        updateNote(note, false);
    }


    public boolean deleteNote(Note note) {
        return deleteNote(note, false);
    }


    public boolean deleteNote(Note note, boolean keepAttachments) {
        int deletedNotes;
        boolean result = true;
        SQLiteDatabase db = getDatabase(true);
        deletedNotes = db.delete(TABLE_NOTES, KEY_ID + " = ?", new String[]{String.valueOf(note.get_id())});
        if (!keepAttachments) {
            int deletedAttachments = db.delete(TABLE_ATTACHMENTS, KEY_ATTACHMENT_NOTE_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
            result = result && deletedAttachments == note.getAttachmentsList().size();
        }
        result = result && deletedNotes == 1;
        return result;
    }


    public void emptyTrash() {
        for (Note note : getNotesTrashed()) {
            deleteNote(note);
        }
    }


    public List<Note> getNotesByPattern(String pattern) {
        String escapedPattern = StringEscapeUtils.escapeSql(pattern);
        int navigation = Navigation.getNavigation();
        String whereCondition = " WHERE "
                + KEY_TRASHED + (navigation == Navigation.TRASH ? " IS 1" : " IS NOT 1")
                + (navigation == Navigation.ARCHIVE ? " AND " + KEY_ARCHIVED + " IS 1" : "")
                + (navigation == Navigation.CATEGORY ? " AND " + KEY_CATEGORY + " = " + Navigation.getCategory() : "")
                + (navigation == Navigation.UNCATEGORIZED ? " AND (" + KEY_CATEGORY + " IS NULL OR " + KEY_CATEGORY_ID
                + " == 0) " : "")
                + (Navigation.checkNavigation(Navigation.REMINDERS) ? " AND " + KEY_REMINDER + " IS NOT NULL" : "")
                + " AND ("
                + " ( " + KEY_LOCKED + " IS NOT 1 AND (" + KEY_TITLE + " LIKE '%" + escapedPattern + "%' " + " OR " +
                KEY_CONTENT + " LIKE '%" + escapedPattern + "%' ))"
                + " OR ( " + KEY_LOCKED + " = 1 AND " + KEY_TITLE + " LIKE '%" + escapedPattern + "%' )"
                + ")";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesWithReminder(boolean filterPastReminders) {
        String whereCondition = " WHERE " + KEY_REMINDER
                + (filterPastReminders ? " >= " + Calendar.getInstance().getTimeInMillis() : " IS NOT NULL")
                + " AND " + KEY_ARCHIVED + " IS NOT 1"
                + " AND " + KEY_TRASHED + " IS NOT 1";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesWithReminderNotFired() {
        String whereCondition = " WHERE " + KEY_REMINDER + " IS NOT NULL"
                + " AND " + KEY_REMINDER_FIRED + " IS NOT 1"
                + " AND " + KEY_ARCHIVED + " IS NOT 1"
                + " AND " + KEY_TRASHED + " IS NOT 1";
        return getNotes(whereCondition, true);
    }


    public List<Note> getNotesWithLock(boolean locked) {
        String whereCondition = " WHERE " + KEY_LOCKED + (locked ? " = 1 " : " IS NOT 1 ");
        return getNotes(whereCondition, true);
    }


    public List<Note> getTodayReminders() {
        String whereCondition = " WHERE DATE(" + KEY_REMINDER + "/1000, 'unixepoch') = DATE('now') AND " +
                KEY_TRASHED + " IS NOT 1";
        return getNotes(whereCondition, false);
    }


    public ArrayList<Attachment> getNoteAttachments(Note note) {
        String whereCondition = " WHERE " + KEY_ATTACHMENT_NOTE_ID + " = " + note.get_id();
        return getAttachments(whereCondition);
    }


    public List<Note> getChecklists() {
        String whereCondition = " WHERE " + KEY_CHECKLIST + " = 1";
        return getNotes(whereCondition, false);
    }


    public List<Note> getMasked() {
        String whereCondition = " WHERE " + KEY_LOCKED + " = 1";
        return getNotes(whereCondition, false);
    }


    public List<Note> getNotesByCategory(Long categoryId) {
        List<Note> notes;
        boolean filterArchived = prefs.getBoolean(Constants.PREF_FILTER_ARCHIVED_IN_CATEGORIES + categoryId, false);
        try {
            String whereCondition = " WHERE "
                    + KEY_CATEGORY_ID + " = " + categoryId
                    + " AND " + KEY_TRASHED + " IS NOT 1"
                    + (filterArchived ? " AND " + KEY_ARCHIVED + " IS NOT 1" : "");
            notes = getNotes(whereCondition, true);
        } catch (NumberFormatException e) {
            notes = getAllNotes(true);
        }
        return notes;
    }


    public List<Tag> getTags() {
        return getTags(null);
    }


    public List<Tag> getTags(Note note) {
        List<Tag> tags = new ArrayList<>();
        HashMap<String, Integer> tagsMap = new HashMap<>();

        String whereCondition = " WHERE "
                + (note != null ? KEY_ID + " = " + note.get_id() + " AND " : "")
                + "(" + KEY_CONTENT + " LIKE '%#%' OR " + KEY_TITLE + " LIKE '%#%' " + ")"
                + " AND " + KEY_TRASHED + " IS " + (Navigation.checkNavigation(Navigation.TRASH) ? "" : " NOT ") + " 1";
        List<Note> notesRetrieved = getNotes(whereCondition, true);

        for (Note noteRetrieved : notesRetrieved) {
            HashMap<String, Integer> tagsRetrieved = TagsHelper.retrieveTags(noteRetrieved);
            for (String s : tagsRetrieved.keySet()) {
                int count = tagsMap.get(s) == null ? 0 : tagsMap.get(s);
                tagsMap.put(s, ++count);
            }
        }

        for (String s : tagsMap.keySet()) {
            Tag tag = new Tag(s, tagsMap.get(s));
            tags.add(tag);
        }

        Collections.sort(tags, (tag1, tag2) -> tag1.getText().compareToIgnoreCase(tag2.getText()));
        return tags;
    }


    public List<Note> getNotesByTag(String tag) {
        if (tag.contains(",")) {
            return getNotesByTag(tag.split(","));
        } else {
            return getNotesByTag(new String[]{tag});
        }
    }


    public List<Note> getNotesByTag(String[] tags) {
        StringBuilder whereCondition = new StringBuilder();
        whereCondition.append(" WHERE ");
        for (int i = 0; i < tags.length; i++) {
            if (i != 0) {
                whereCondition.append(" AND ");
            }
            whereCondition.append("(" + KEY_CONTENT + " LIKE '%").append(tags[i]).append("%' OR ").append(KEY_TITLE)
                    .append(" LIKE '%").append(tags[i]).append("%')");
        }
        whereCondition.append(" AND " + KEY_TRASHED + " IS ").append(Navigation.checkNavigation(Navigation.TRASH) ?
                "" : "" +
                " NOT ").append(" 1");

        return rx.Observable.from(getNotes(whereCondition.toString(), true))
                .map(note -> {
                    boolean matches = rx.Observable.from(tags)
                            .all(tag -> {
                                Pattern p = Pattern.compile(".*(\\s|^)" + tag + "(\\s|$).*", Pattern.MULTILINE);
                                return p.matcher((note.getTitle() + " " + note.getContent())).find();
                            }).toBlocking().single();
                    return matches ? note : null;
                })
                .filter(note -> note != null)
                .toList().toBlocking().single();
    }

    public List<Note> getNotesByUncompleteChecklist() {
        String whereCondition = " WHERE " + KEY_CHECKLIST + " = 1 AND " + KEY_CONTENT + " LIKE '%" + it.feio.android
                .checklistview.interfaces.Constants.UNCHECKED_SYM + "%'";
        return getNotes(whereCondition, true);
    }


    public ArrayList<Attachment> getAllAttachments() {
        return getAttachments("");
    }


    public ArrayList<Attachment> getAttachments(String whereCondition) {

        ArrayList<Attachment> attachmentsList = new ArrayList<>();
        String sql = "SELECT "
                + KEY_ATTACHMENT_ID + ","
                + KEY_ATTACHMENT_URI + ","
                + KEY_ATTACHMENT_NAME + ","
                + KEY_ATTACHMENT_SIZE + ","
                + KEY_ATTACHMENT_LENGTH + ","
                + KEY_ATTACHMENT_MIME_TYPE
                + " FROM " + TABLE_ATTACHMENTS
                + whereCondition;
        SQLiteDatabase db;
        Cursor cursor = null;

        try {

            cursor = getDatabase().rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                Attachment mAttachment;
                do {
                    mAttachment = new Attachment(cursor.getLong(0),
                            Uri.parse(cursor.getString(1)), cursor.getString(2), cursor.getInt(3),
                            (long) cursor.getInt(4), cursor.getString(5));
                    attachmentsList.add(mAttachment);
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return attachmentsList;
    }


    public ArrayList<Category> getCategories() {
        ArrayList<Category> categoriesList = new ArrayList<>();
        String sql = "SELECT "
                + KEY_CATEGORY_ID + ","
                + KEY_CATEGORY_NAME + ","
                + KEY_CATEGORY_DESCRIPTION + ","
                + KEY_CATEGORY_COLOR + ","
                + " COUNT(" + KEY_ID + ") count"
                + " FROM " + TABLE_CATEGORY
                + " LEFT JOIN ("
                + " SELECT " + KEY_ID + ", " + KEY_CATEGORY
                + " FROM " + TABLE_NOTES
                + " WHERE " + KEY_TRASHED + " IS NOT 1"
                + ") USING( " + KEY_CATEGORY + ") "
                + " GROUP BY "
                + KEY_CATEGORY_ID + ","
                + KEY_CATEGORY_NAME + ","
                + KEY_CATEGORY_DESCRIPTION + ","
                + KEY_CATEGORY_COLOR
                + " ORDER BY IFNULL(NULLIF(" + KEY_CATEGORY_NAME + ", ''),'zzzzzzzz') ";

        Cursor cursor = null;
        try {
            cursor = getDatabase().rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    categoriesList.add(new Category(cursor.getLong(0),
                            cursor.getString(1), cursor.getString(2), cursor
                            .getString(3), cursor.getInt(4)));
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return categoriesList;
    }


    public Category updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_ID, category.getId() != null ? category.getId() : Calendar.getInstance()
                .getTimeInMillis());
        values.put(KEY_CATEGORY_NAME, category.getName());
        values.put(KEY_CATEGORY_DESCRIPTION, category.getDescription());
        values.put(KEY_CATEGORY_COLOR, category.getColor());
        getDatabase(true).insertWithOnConflict(TABLE_CATEGORY, KEY_CATEGORY_ID, values, SQLiteDatabase
                .CONFLICT_REPLACE);
        return category;
    }


    public long deleteCategory(Category category) {
        long deleted;

        SQLiteDatabase db = getDatabase(true);
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, "");

        db.update(TABLE_NOTES, values, KEY_CATEGORY + " = ?",
                new String[]{String.valueOf(category.getId())});

        deleted = db.delete(TABLE_CATEGORY, KEY_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(category.getId())});
        return deleted;
    }


    public Category getCategory(Long id) {
        Category category = null;
        String sql = "SELECT "
                + KEY_CATEGORY_ID + ","
                + KEY_CATEGORY_NAME + ","
                + KEY_CATEGORY_DESCRIPTION + ","
                + KEY_CATEGORY_COLOR
                + " FROM " + TABLE_CATEGORY
                + " WHERE " + KEY_CATEGORY_ID + " = " + id;

        Cursor cursor = null;
        try {
            cursor = getDatabase().rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                category = new Category(cursor.getLong(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3));
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return category;
    }


    public int getCategorizedCount(Category category) {
        int count = 0;
        String sql = "SELECT COUNT(*)"
                + " FROM " + TABLE_NOTES
                + " WHERE " + KEY_CATEGORY + " = " + category.getId();

        Cursor cursor = null;
        try {
            cursor = getDatabase().rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return count;
    }


    public Stats getStats() {
        Stats mStats = new Stats();

        mStats.setCategories(getCategories().size());

        int notesActive = 0, notesArchived = 0, notesTrashed = 0, reminders = 0, remindersFuture = 0, checklists = 0,
                notesMasked = 0, tags = 0, locations = 0;
        int totalWords = 0, totalChars = 0, maxWords = 0, maxChars = 0, avgWords = 0, avgChars = 0;
        int words, chars;
        List<Note> notes = getAllNotes(false);
        for (Note note : notes) {
            if (note.isTrashed()) {
                notesTrashed++;
            } else if (note.isArchived()) {
                notesArchived++;
            } else {
                notesActive++;
            }
            if (note.getAlarm() != null && Long.parseLong(note.getAlarm()) > 0) {
                if (Long.parseLong(note.getAlarm()) > Calendar.getInstance().getTimeInMillis()) {
                    remindersFuture++;
                } else {
                    reminders++;
                }
            }
            if (note.isChecklist()) {
                checklists++;
            }
            if (note.isLocked()) {
                notesMasked++;
            }
            tags += TagsHelper.retrieveTags(note).size();
            if (note.getLongitude() != null && note.getLongitude() != 0) {
                locations++;
            }
            words = NotesHelper.getWords(note);
            chars = NotesHelper.getChars(note);
            if (words > maxWords) {
                maxWords = words;
            }
            if (chars > maxChars) {
                maxChars = chars;
            }
            totalWords += words;
            totalChars += chars;
        }
        mStats.setNotesActive(notesActive);
        mStats.setNotesArchived(notesArchived);
        mStats.setNotesTrashed(notesTrashed);
        mStats.setReminders(reminders);
        mStats.setRemindersFutures(remindersFuture);
        mStats.setNotesChecklist(checklists);
        mStats.setNotesMasked(notesMasked);
        mStats.setTags(tags);
        mStats.setLocation(locations);
        avgWords = totalWords / (notes.size() != 0 ? notes.size() : 1);
        avgChars = totalChars / (notes.size() != 0 ? notes.size() : 1);

        mStats.setWords(totalWords);
        mStats.setWordsMax(maxWords);
        mStats.setWordsAvg(avgWords);
        mStats.setChars(totalChars);
        mStats.setCharsMax(maxChars);
        mStats.setCharsAvg(avgChars);

        int attachmentsAll = 0, images = 0, videos = 0, audioRecordings = 0, sketches = 0, files = 0;
        List<Attachment> attachments = getAllAttachments();
        for (Attachment attachment : attachments) {
            if (Constants.MIME_TYPE_IMAGE.equals(attachment.getMime_type())) {
                images++;
            } else if (Constants.MIME_TYPE_VIDEO.equals(attachment.getMime_type())) {
                videos++;
            } else if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMime_type())) {
                audioRecordings++;
            } else if (Constants.MIME_TYPE_SKETCH.equals(attachment.getMime_type())) {
                sketches++;
            } else if (Constants.MIME_TYPE_FILES.equals(attachment.getMime_type())) {
                files++;
            }
        }
        mStats.setAttachments(attachmentsAll);
        mStats.setImages(images);
        mStats.setVideos(videos);
        mStats.setAudioRecordings(audioRecordings);
        mStats.setSketches(sketches);
        mStats.setFiles(files);

        return mStats;
    }


    public void setReminderFired(long noteId, boolean fired) {
        ContentValues values = new ContentValues();
        values.put(KEY_REMINDER_FIRED, fired);
        getDatabase(true).update(TABLE_NOTES, values, KEY_ID + " = ?", new String[]{String.valueOf(noteId)});
    }


}
