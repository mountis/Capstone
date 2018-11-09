

package developer.elkane.udacity.wizflow.async.upgrade;

import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.ReminderHelper;
import developer.elkane.udacity.wizflow.utils.StorageHelper;


public class UpgradeProcessor {

    private final static String METHODS_PREFIX = "onUpgradeTo";

    private static UpgradeProcessor instance;


    private UpgradeProcessor() {
    }


    private static UpgradeProcessor getInstance() {
        if (instance == null) {
            instance = new UpgradeProcessor();
        }
        return instance;
    }


    public static void process(int dbOldVersion, int dbNewVersion) {
        try {
            List<Method> methodsToLaunch = getInstance().getMethodsToLaunch(dbOldVersion, dbNewVersion);
            for (Method methodToLaunch : methodsToLaunch) {
                methodToLaunch.invoke(getInstance());
            }
        } catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
            Log.d(Constants.TAG, "Explosion processing upgrade!", e);
        }
    }


    private List<Method> getMethodsToLaunch(int dbOldVersion, int dbNewVersion) {
        List<Method> methodsToLaunch = new ArrayList<>();
        Method[] declaredMethods = getInstance().getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().contains(METHODS_PREFIX)) {
                int methodVersionPostfix = Integer.parseInt(declaredMethod.getName().replace(METHODS_PREFIX, ""));
                if (dbOldVersion <= methodVersionPostfix && methodVersionPostfix <= dbNewVersion) {
                    methodsToLaunch.add(declaredMethod);
                }
            }
        }
        return methodsToLaunch;
    }


    private void onUpgradeTo476() {
        final DbHelper dbHelper = DbHelper.getInstance();
        for (Attachment attachment : dbHelper.getAllAttachments()) {
            if (attachment.getMime_type() == null) {
                String mimeType = StorageHelper.getMimeType(attachment.getUri().toString());
                if (!TextUtils.isEmpty(mimeType)) {
                    String type = mimeType.replaceFirst("/.*", "");
                    switch (type) {
                        case "image":
                            attachment.setMime_type(Constants.MIME_TYPE_IMAGE);
                            break;
                        case "video":
                            attachment.setMime_type(Constants.MIME_TYPE_VIDEO);
                            break;
                        case "audio":
                            attachment.setMime_type(Constants.MIME_TYPE_AUDIO);
                            break;
                        default:
                            attachment.setMime_type(Constants.MIME_TYPE_FILES);
                            break;
                    }
                    dbHelper.updateAttachment(attachment);
                } else {
                    attachment.setMime_type(Constants.MIME_TYPE_FILES);
                }
            }
        }
    }


    private void onUpgradeTo480() {
        final DbHelper dbHelper = DbHelper.getInstance();
        for (Attachment attachment : dbHelper.getAllAttachments()) {
            if ("audio/3gp".equals(attachment.getMime_type()) || "audio/3gpp".equals(attachment.getMime_type
                    ())) {

                File from = new File(attachment.getUriPath());
                FilenameUtils.getExtension(from.getName());
                File to = new File(from.getParent(), from.getName().replace(FilenameUtils.getExtension(from
                        .getName()), Constants.MIME_TYPE_AUDIO_EXT));
                from.renameTo(to);

                attachment.setUri(Uri.fromFile(to));
                attachment.setMime_type(Constants.MIME_TYPE_AUDIO);
                dbHelper.updateAttachment(attachment);
            }
        }
    }


    private void onUpgradeTo482() {
        for (Note note : DbHelper.getInstance().getNotesWithReminderNotFired()) {
            ReminderHelper.addReminder(WizFlow.getAppContext(), note);
        }
    }


    private void onUpgradeTo501() {
        List<Long> creations = new ArrayList<>();
        for (Note note : DbHelper.getInstance().getAllNotes(false)) {
            if (creations.contains(note.getCreation())) {

                ContentValues values = new ContentValues();
                values.put(DbHelper.KEY_CREATION, note.getCreation() + (long) (Math.random() * 999));
                DbHelper.getInstance().getDatabase().update(DbHelper.TABLE_NOTES, values, DbHelper.KEY_TITLE +
                        " = ? AND " + DbHelper.KEY_CREATION + " = ? AND " + DbHelper.KEY_CONTENT + " = ?", new String[]{note
                        .getTitle(), String.valueOf(note.getCreation()), note.getContent()});
            }
            creations.add(note.getCreation());
        }
    }

}
