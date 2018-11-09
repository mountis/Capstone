
package developer.elkane.udacity.wizflow.async;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.listeners.OnAttachingFileListener;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.GeocodeHelper;
import developer.elkane.udacity.wizflow.utils.NotificationsHelper;
import developer.elkane.udacity.wizflow.utils.ReminderHelper;
import developer.elkane.udacity.wizflow.utils.StorageHelper;
import developer.elkane.udacity.wizflow.utils.TextHelper;
import exceptions.ImportException;
import it.feio.android.springpadimporter.Importer;
import it.feio.android.springpadimporter.models.SpringpadAttachment;
import it.feio.android.springpadimporter.models.SpringpadComment;
import it.feio.android.springpadimporter.models.SpringpadElement;
import it.feio.android.springpadimporter.models.SpringpadItem;


public class DataBackupIntentService extends IntentService implements OnAttachingFileListener {

    public final static String INTENT_BACKUP_NAME = "backup_name";
    public final static String INTENT_BACKUP_INCLUDE_SETTINGS = "backup_include_settings";
    public final static String ACTION_DATA_EXPORT = "action_data_export";
    public final static String ACTION_DATA_IMPORT = "action_data_import";
    public final static String ACTION_DATA_IMPORT_SPRINGPAD = "action_data_import_springpad";
    public final static String ACTION_DATA_DELETE = "action_data_delete";
    public final static String EXTRA_SPRINGPAD_BACKUP = "extra_springpad_backup";

    private SharedPreferences prefs;
    private NotificationsHelper mNotificationsHelper;

    private int importedSpringpadNotes, importedSpringpadNotebooks;


    public DataBackupIntentService() {
        super("DataBackupIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        mNotificationsHelper = new NotificationsHelper(this)
                .createNotification(developer.elkane.udacity.wizflow.R.drawable.ic_content_save_white_24dp, getString(developer.elkane.udacity.wizflow.R.string.working), null)
                .setIndeterminate().setOngoing().show();

        if (ACTION_DATA_EXPORT.equals(intent.getAction())) {
            exportData(intent);
        } else if (ACTION_DATA_IMPORT.equals(intent.getAction())) {
            importData(intent);
        } else if (ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
            importDataFromSpringpad(intent);
        } else if (ACTION_DATA_DELETE.equals(intent.getAction())) {
            deleteData(intent);
        }
    }


    synchronized private void exportData(Intent intent) {

        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        StorageHelper.delete(this, backupDir.getAbsolutePath());

        backupDir = StorageHelper.getBackupDir(backupName);

        exportDB(backupDir);

        exportAttachments(backupDir);

        if (intent.getBooleanExtra(INTENT_BACKUP_INCLUDE_SETTINGS, true)) {
            exportSettings(backupDir);
        }

        String title = getString(developer.elkane.udacity.wizflow.R.string.data_export_completed);
        String text = backupDir.getPath();
        createNotification(intent, this, title, text, backupDir);
    }


    synchronized private void importData(Intent intent) {

        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        importDB(backupDir);

        importAttachments(backupDir);

        importSettings(backupDir);

        resetReminders();

        String title = getString(developer.elkane.udacity.wizflow.R.string.data_import_completed);
        String text = getString(developer.elkane.udacity.wizflow.R.string.click_to_refresh_application);
        createNotification(intent, this, title, text, backupDir);
    }


    synchronized private void importDataFromSpringpad(Intent intent) {
        String backupPath = intent.getStringExtra(EXTRA_SPRINGPAD_BACKUP);
        Importer importer = new Importer();
        try {
            importer.setZipProgressesListener(percentage -> mNotificationsHelper.setMessage(getString(developer.elkane.udacity.wizflow.R.string.extracted) + " " + percentage + "%").show());
            importer.doImport(backupPath);
            updateImportNotification(importer);
        } catch (ImportException e) {
            new NotificationsHelper(this)
                    .createNotification(developer.elkane.udacity.wizflow.R.drawable.ic_emoticon_sad_white_24dp,
                            getString(developer.elkane.udacity.wizflow.R.string.import_fail) + ": " + e.getMessage(), null).setLedActive().show();
            return;
        }
        List<SpringpadElement> elements = importer.getSpringpadNotes();

        if (elements == null || elements.size() == 0) {
            return;
        }

        HashMap<String, Category> categoriesWithUuid = new HashMap<>();

        for (SpringpadElement springpadElement : importer.getNotebooks()) {
            Category cat = new Category();
            cat.setName(springpadElement.getName());
            cat.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
            DbHelper.getInstance().updateCategory(cat);
            categoriesWithUuid.put(springpadElement.getUuid(), cat);

            importedSpringpadNotebooks++;
            updateImportNotification(importer);
        }
        Category defaulCategory = new Category();
        defaulCategory.setName("Springpad");
        defaulCategory.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
        DbHelper.getInstance().updateCategory(defaulCategory);

        Note note;
        Attachment mAttachment = null;
        Uri uri;
        for (SpringpadElement springpadElement : importer.getNotes()) {
            note = new Note();

            note.setTitle(springpadElement.getName());

            StringBuilder content = new StringBuilder();
            content.append(TextUtils.isEmpty(springpadElement.getText()) ? "" : Html.fromHtml(springpadElement
                    .getText()));
            content.append(TextUtils.isEmpty(springpadElement.getDescription()) ? "" : springpadElement
                    .getDescription());

            if (springpadElement.getType() == null) {
                Toast.makeText(this, getString(developer.elkane.udacity.wizflow.R.string.error), Toast.LENGTH_SHORT).show();
                continue;
            }

            if (springpadElement.getType().equals(SpringpadElement.TYPE_VIDEO)) {
                try {
                    content.append(System.getProperty("line.separator")).append(springpadElement.getVideos().get(0));
                } catch (IndexOutOfBoundsException e) {
                    content.append(System.getProperty("line.separator")).append(springpadElement.getUrl());
                }
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_TVSHOW)) {
                content.append(System.getProperty("line.separator")).append(
                        TextUtils.join(", ", springpadElement.getCast()));
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_BOOK)) {
                content.append(System.getProperty("line.separator")).append("Author: ")
                        .append(springpadElement.getAuthor()).append(System.getProperty("line.separator"))
                        .append("Publication date: ").append(springpadElement.getPublicationDate());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_RECIPE)) {
                content.append(System.getProperty("line.separator")).append("Ingredients: ")
                        .append(springpadElement.getIngredients()).append(System.getProperty("line.separator"))
                        .append("Directions: ").append(springpadElement.getDirections());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_BOOKMARK)) {
                content.append(System.getProperty("line.separator")).append(springpadElement.getUrl());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_BUSINESS)
                    && springpadElement.getPhoneNumbers() != null) {
                content.append(System.getProperty("line.separator")).append("Phone number: ")
                        .append(springpadElement.getPhoneNumbers().getPhone());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_PRODUCT)) {
                content.append(System.getProperty("line.separator")).append("Category: ")
                        .append(springpadElement.getCategory()).append(System.getProperty("line.separator"))
                        .append("Manufacturer: ").append(springpadElement.getManufacturer())
                        .append(System.getProperty("line.separator")).append("Price: ")
                        .append(springpadElement.getPrice());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_WINE)) {
                content.append(System.getProperty("line.separator")).append("Wine type: ")
                        .append(springpadElement.getWine_type()).append(System.getProperty("line.separator"))
                        .append("Varietal: ").append(springpadElement.getVarietal())
                        .append(System.getProperty("line.separator")).append("Price: ")
                        .append(springpadElement.getPrice());
            }
            if (springpadElement.getType().equals(SpringpadElement.TYPE_ALBUM)) {
                content.append(System.getProperty("line.separator")).append("Artist: ")
                        .append(springpadElement.getArtist());
            }
            for (SpringpadComment springpadComment : springpadElement.getComments()) {
                content.append(System.getProperty("line.separator")).append(springpadComment.getCommenter())
                        .append(" commented at 0").append(springpadComment.getDate()).append(": ")
                        .append(springpadElement.getArtist());
            }

            note.setContent(content.toString());

            if (springpadElement.getType().equals(SpringpadElement.TYPE_CHECKLIST)) {
                StringBuilder sb = new StringBuilder();
                String checkmark;
                for (SpringpadItem mSpringpadItem : springpadElement.getItems()) {
                    checkmark = mSpringpadItem.getComplete() ? it.feio.android.checklistview.interfaces.Constants
                            .CHECKED_SYM
                            : it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM;
                    sb.append(checkmark).append(mSpringpadItem.getName()).append(System.getProperty("line.separator"));
                }
                note.setContent(sb.toString());
                note.setChecklist(true);
            }

            String tags = springpadElement.getTags().size() > 0 ? "#"
                    + TextUtils.join(" #", springpadElement.getTags()) : "";
            if (note.isChecklist()) {
                note.setTitle(note.getTitle() + tags);
            } else {
                note.setContent(note.getContent() + System.getProperty("line.separator") + tags);
            }

            String address = springpadElement.getAddresses() != null ? springpadElement.getAddresses().getAddress()
                    : "";
            if (!TextUtils.isEmpty(address)) {
                try {
                    double[] coords = GeocodeHelper.getCoordinatesFromAddress(this, address);
                    note.setLatitude(coords[0]);
                    note.setLongitude(coords[1]);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "An error occurred trying to resolve address to coords during Springpad import");
                }
                note.setAddress(address);
            }

            if (springpadElement.getDate() != null) {
                note.setAlarm(springpadElement.getDate().getTime());
            }

            note.setCreation(springpadElement.getCreated().getTime());
            note.setLastModification(springpadElement.getModified().getTime());

            String image = springpadElement.getImage();
            if (!TextUtils.isEmpty(image)) {
                try {
                    File file = StorageHelper.createNewAttachmentFileFromHttp(this, image);
                    uri = Uri.fromFile(file);
                    String mimeType = StorageHelper.getMimeType(uri.getPath());
                    mAttachment = new Attachment(uri, mimeType);
                } catch (MalformedURLException e) {
                    uri = Uri.parse(importer.getWorkingPath() + image);
                    mAttachment = StorageHelper.createAttachmentFromUri(this, uri, true);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Error retrieving Springpad online image");
                }
                if (mAttachment != null) {
                    note.addAttachment(mAttachment);
                }
                mAttachment = null;
            }

            for (SpringpadAttachment springpadAttachment : springpadElement.getAttachments()) {
                if (image != null && image.equals(springpadAttachment.getUrl())) continue;

                if (TextUtils.isEmpty(springpadAttachment.getUrl())) {
                    continue;
                }

                try {
                    File file = StorageHelper.createNewAttachmentFileFromHttp(this, springpadAttachment.getUrl());
                    uri = Uri.fromFile(file);
                    String mimeType = StorageHelper.getMimeType(uri.getPath());
                    mAttachment = new Attachment(uri, mimeType);
                } catch (MalformedURLException e) {
                    uri = Uri.parse(importer.getWorkingPath() + springpadAttachment.getUrl());
                    mAttachment = StorageHelper.createAttachmentFromUri(this, uri, true);
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Error retrieving Springpad online image");
                }
                if (mAttachment != null) {
                    note.addAttachment(mAttachment);
                }
                mAttachment = null;
            }

            if (springpadElement.getNotebooks().size() > 0) {
                note.setCategory(categoriesWithUuid.get(springpadElement.getNotebooks().get(0)));
            } else {
                note.setCategory(defaulCategory);
            }

            DbHelper.getInstance().updateNote(note, false);
            ReminderHelper.addReminder(WizFlow.getAppContext(), note);

            importedSpringpadNotes++;
            updateImportNotification(importer);
        }

        try {
            importer.clean();
        } catch (IOException e) {
            Log.w(Constants.TAG, "Springpad import temp files not deleted");
        }

        String title = getString(developer.elkane.udacity.wizflow.R.string.data_import_completed);
        String text = getString(developer.elkane.udacity.wizflow.R.string.click_to_refresh_application);
        createNotification(intent, this, title, text, null);
    }


    private void updateImportNotification(Importer importer) {
        mNotificationsHelper.setMessage(
                importer.getNotebooksCount() + " " + getString(developer.elkane.udacity.wizflow.R.string.categories) + " ("
                        + importedSpringpadNotebooks + " " + getString(developer.elkane.udacity.wizflow.R.string.imported) + "), "
                        + +importer.getNotesCount() + " " + getString(developer.elkane.udacity.wizflow.R.string.notes) + " ("
                        + importedSpringpadNotes + " " + getString(developer.elkane.udacity.wizflow.R.string.imported) + ")").show();
    }


    synchronized private void deleteData(Intent intent) {

        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        StorageHelper.delete(this, backupDir.getAbsolutePath());

        String title = getString(developer.elkane.udacity.wizflow.R.string.data_deletion_completed);
        String text = backupName + " " + getString(developer.elkane.udacity.wizflow.R.string.deleted);
        createNotification(intent, this, title, text, backupDir);
    }


    private void createNotification(Intent intent, Context mContext, String title, String message, File backupDir) {

        Intent intentLaunch;
        if (DataBackupIntentService.ACTION_DATA_IMPORT.equals(intent.getAction())
                || DataBackupIntentService.ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
            intentLaunch = new Intent(mContext, MainActivity.class);
            intentLaunch.setAction(Constants.ACTION_RESTART_APP);
        } else {
            intentLaunch = new Intent();
        }
        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent notifyIntent = PendingIntent.getActivity(mContext, 0, intentLaunch,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationsHelper mNotificationsHelper = new NotificationsHelper(mContext);
        mNotificationsHelper.createNotification(developer.elkane.udacity.wizflow.R.drawable.ic_content_save_white_24dp, title, notifyIntent)
                .setMessage(message).setRingtone(prefs.getString("settings_notification_ringtone", null))
                .setLedActive();
        if (prefs.getBoolean("settings_notification_vibration", true))
            mNotificationsHelper.setVibration();
        mNotificationsHelper.show();
    }


    private boolean exportDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        return (StorageHelper.copyFile(database, new File(backupDir, Constants.DATABASE_NAME)));
    }

    private void exportNotes(File backupDir) {
        for (Note note : DbHelper.getInstance().getAllNotes(false)) {
            File noteFile = new File(backupDir, String.valueOf(note.get_id()));
            try {
                FileUtils.write(noteFile, note.toJSON());
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error backupping note: " + note.get_id());
            }
        }
    }


    private boolean exportAttachments(File backupDir) {
        File attachmentsDir = StorageHelper.getAttachmentDir(this);
        File destinationattachmentsDir = new File(backupDir, attachmentsDir.getName());

        DbHelper db = DbHelper.getInstance();
        ArrayList<Attachment> list = db.getAllAttachments();

        int exported = 0;
        for (Attachment attachment : list) {
            StorageHelper.copyToBackupDir(destinationattachmentsDir, new File(attachment.getUri().getPath()));
            mNotificationsHelper.setMessage(TextHelper.capitalize(getString(developer.elkane.udacity.wizflow.R.string.attachment)) + " " + exported++ + "/" + list.size())
                    .show();
        }
        return true;
    }


    private boolean exportSettings(File backupDir) {
        File preferences = StorageHelper.getSharedPreferencesFile(this);
        return (StorageHelper.copyFile(preferences, new File(backupDir, preferences.getName())));
    }


    private boolean importSettings(File backupDir) {
        File preferences = StorageHelper.getSharedPreferencesFile(this);
        File preferenceBackup = new File(backupDir, preferences.getName());
        return (StorageHelper.copyFile(preferenceBackup, preferences));
    }


    private void resetReminders() {
        Log.d(Constants.TAG, "Resettings reminders");
        for (Note note : DbHelper.getInstance().getNotesWithReminderNotFired()) {
            ReminderHelper.addReminder(WizFlow.getAppContext(), note);
        }
    }


    private boolean importDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        if (database.exists()) {
            database.delete();
        }
        return (StorageHelper.copyFile(new File(backupDir, Constants.DATABASE_NAME), database));
    }


    private void importNotes(File backupDir) {
        for (File file : FileUtils.listFiles(backupDir, new RegexFileFilter("\\d{13}"), TrueFileFilter.INSTANCE)) {
            try {
                Note note = new Note();
                note.buildFromJson(FileUtils.readFileToString(file));
                if (note.getCategory() != null) {
                    DbHelper.getInstance().updateCategory(note.getCategory());
                }
                for (Attachment attachment : note.getAttachmentsList()) {
                    DbHelper.getInstance().updateAttachment(attachment);
                }
                DbHelper.getInstance().updateNote(note, false);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error parsing note json");
            }
        }
    }

    private void importAttachments(File backupDir) {
        File attachmentsDir = StorageHelper.getAttachmentDir(this);
        File backupAttachmentsDir = new File(backupDir, attachmentsDir.getName());
        if (!backupAttachmentsDir.exists()) {
            return;
        }
        Collection list = FileUtils.listFiles(backupAttachmentsDir, FileFilterUtils.trueFileFilter(),
                TrueFileFilter.INSTANCE);
        Iterator i = list.iterator();
        int imported = 0;
        File file = null;
        while (i.hasNext()) {
            try {
                file = (File) i.next();
                FileUtils.copyFileToDirectory(file, attachmentsDir, true);
                mNotificationsHelper.setMessage(TextHelper.capitalize(getString(developer.elkane.udacity.wizflow.R.string.attachment)) + " " + imported++ + "/" + list.size())
                        .show();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error importing the attachment " + file.getName());
            }
        }
    }


    @Override
    public void onAttachingFileErrorOccurred(Attachment mAttachment) {
    }


    @Override
    public void onAttachingFileFinished(Attachment mAttachment) {
    }

}
