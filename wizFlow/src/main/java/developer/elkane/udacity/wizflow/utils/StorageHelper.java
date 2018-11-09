
package developer.elkane.udacity.wizflow.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import developer.elkane.udacity.wizflow.models.Attachment;


public class StorageHelper {

    public static boolean checkStorage() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        switch (state) {
            case Environment.MEDIA_MOUNTED:
                mExternalStorageAvailable = mExternalStorageWriteable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
                break;
            default:
                mExternalStorageAvailable = mExternalStorageWriteable = false;
                break;
        }
        return mExternalStorageAvailable && mExternalStorageWriteable;
    }


    public static String getStorageDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }


    public static File getAttachmentDir(Context mContext) {
        return mContext.getExternalFilesDir(null);
    }


    public static File getDbSyncDir(Context mContext) {
        File extFilesDir = mContext.getExternalFilesDir(null);
        File dbSyncDir = new File(extFilesDir, Constants.APP_STORAGE_DIRECTORY_SB_SYNC);
        dbSyncDir.mkdirs();
        if (dbSyncDir.exists() && dbSyncDir.isDirectory()) {
            return dbSyncDir;
        } else {
            return null;
        }
    }


    public static File createExternalStoragePrivateFile(Context mContext, Uri uri, String extension) {

        if (!checkStorage()) {
            Toast.makeText(mContext, mContext.getString(developer.elkane.udacity.wizflow.R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return null;
        }
        File file = createNewAttachmentFile(mContext, extension);

        InputStream contentResolverInputStream = null;
        OutputStream contentResolverOutputStream = null;
        try {
            contentResolverInputStream = mContext.getContentResolver().openInputStream(uri);
            contentResolverOutputStream = new FileOutputStream(file);
            copyFile(contentResolverInputStream, contentResolverOutputStream);
        } catch (IOException e) {
            try {
                FileUtils.copyFile(new File(FileHelper.getPath(mContext, uri)), file);
            } catch (NullPointerException e1) {
                try {
                    FileUtils.copyFile(new File(uri.getPath()), file);
                } catch (IOException e2) {
                    Log.e(Constants.TAG, "Error writing " + file, e2);
                    file = null;
                }
            } catch (IOException e2) {
                Log.e(Constants.TAG, "Error writing " + file, e2);
                file = null;
            }
        } finally {
            try {
                if (contentResolverInputStream != null) {
                    contentResolverInputStream.close();
                }
                if (contentResolverOutputStream != null) {
                    contentResolverOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error closing streams", e);
            }

        }
        return file;
    }

    public static boolean copyFile(File source, File destination) {
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(destination);
            return copyFile(is, os);
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Error copying file", e);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error closing streams", e);
            }
        }
    }


    public static boolean copyFile(InputStream is, OutputStream os) {
        boolean res = false;
        byte[] data = new byte[1024];
        int len;
        try {
            while ((len = is.read(data)) > 0) {
                os.write(data, 0, len);
            }
            is.close();
            os.close();
            res = true;
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error copying file", e);
        }
        return res;
    }


    public static boolean deleteExternalStoragePrivateFile(Context mContext, String name) {
        boolean res = false;

        if (!checkStorage()) {
            Toast.makeText(mContext, mContext.getString(developer.elkane.udacity.wizflow.R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(mContext.getExternalFilesDir(null), name);
        file.delete();

        return true;
    }


    public static boolean delete(Context mContext, String name) {
        boolean res = false;

        if (!checkStorage()) {
            Toast.makeText(mContext, mContext.getString(developer.elkane.udacity.wizflow.R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(name);
        if (file.isFile()) {
            res = file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                res = delete(mContext, file2.getAbsolutePath());
            }
            res = file.delete();
        }

        return res;
    }


    public static String getRealPathFromURI(Context mContext, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }


    public static File createNewAttachmentFile(Context mContext, String extension) {
        File f = null;
        if (checkStorage()) {
            f = new File(mContext.getExternalFilesDir(null), createNewAttachmentName(extension));
        }
        return f;
    }


    public static synchronized String createNewAttachmentName(String extension) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SORTABLE);
        String name = sdf.format(now.getTime());
        name += extension != null ? extension : "";
        return name;
    }


    public static File createNewAttachmentFile(Context mContext) {
        return createNewAttachmentFile(mContext, null);
    }


    public static File copyToBackupDir(File backupDir, File file) {
        if (!checkStorage()) {
            return null;
        }
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File destination = new File(backupDir, file.getName());
        copyFile(file, destination);
        return destination;
    }


    public static File getCacheDir(Context mContext) {
        File dir = mContext.getExternalCacheDir();
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }


    public static File getExternalStoragePublicDir() {
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_STORAGE_FOLDER + File
                .separator);
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }


    public static File getBackupDir(String backupName) {
        File backupDir = new File(getExternalStoragePublicDir(), backupName);
        if (!backupDir.exists())
            backupDir.mkdirs();
        return backupDir;
    }


    public static File getSharedPreferencesFile(Context mContext) {
        File appData = mContext.getFilesDir().getParentFile();
        String packageName = mContext.getApplicationContext().getPackageName();
        return new File(appData
                + System.getProperty("file.separator")
                + "shared_prefs"
                + System.getProperty("file.separator")
                + packageName
                + "_preferences.xml");
    }


    @SuppressWarnings("deprecation")
    public static long getSize(File directory) {
        StatFs statFs = new StatFs(directory.getAbsolutePath());
        long blockSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
            } else {
                blockSize = statFs.getBlockSize();
            }
            // Can't understand why on some devices this fails
        } catch (NoSuchMethodError e) {
            Log.e(Constants.TAG, "Mysterious error", e);
        }
        return getSize(directory, blockSize);
    }


    private static long getSize(File directory, long blockSize) {
        if (blockSize == 0) {
            throw new InvalidParameterException("Blocksize can't be 0");
        }
        File[] files = directory.listFiles();
        if (files != null) {

            long size = directory.length();

            for (File file : files) {
                if (file.isDirectory()) {
                    size += getSize(file, blockSize);
                } else {
                    size += (file.length() / blockSize + 1) * blockSize;
                }
            }
            return size;
        } else {
            return 0;
        }
    }


    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        boolean res = true;

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                res = res && copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
                        children[i]));
            }

        } else {
            res = copyFile(sourceLocation, targetLocation);
        }
        return res;
    }


    public static String getMimeType(Context mContext, Uri uri) {
        ContentResolver cR = mContext.getContentResolver();
        String mimeType = cR.getType(uri);
        if (mimeType == null) {
            mimeType = getMimeType(uri.toString());
        }
        return mimeType;
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public static String getMimeTypeInternal(Context mContext, Uri uri) {
        String mimeType = getMimeType(mContext, uri);
        mimeType = getMimeTypeInternal(mContext, mimeType);
        return mimeType;
    }


    public static String getMimeTypeInternal(Context mContext, String mimeType) {
        if (mimeType != null) {
            if (mimeType.contains("image/")) {
                mimeType = Constants.MIME_TYPE_IMAGE;
            } else if (mimeType.contains("audio/")) {
                mimeType = Constants.MIME_TYPE_AUDIO;
            } else if (mimeType.contains("video/")) {
                mimeType = Constants.MIME_TYPE_VIDEO;
            } else {
                mimeType = Constants.MIME_TYPE_FILES;
            }
        }
        return mimeType;
    }


    public static Attachment createAttachmentFromUri(Context mContext, Uri uri) {
        return createAttachmentFromUri(mContext, uri, false);
    }


    public static Attachment createAttachmentFromUri(Context mContext, Uri uri, boolean moveSource) {
        String name = FileHelper.getNameFromUri(mContext, uri);
        String extension = FileHelper.getFileExtension(FileHelper.getNameFromUri(mContext, uri)).toLowerCase(
                Locale.getDefault());
        File f;
        if (moveSource) {
            f = createNewAttachmentFile(mContext, extension);
            try {
                FileUtils.moveFile(new File(uri.getPath()), f);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Can't move file " + uri.getPath());
            }
        } else {
            f = StorageHelper.createExternalStoragePrivateFile(mContext, uri, extension);
        }
        Attachment mAttachment = null;
        if (f != null) {
            mAttachment = new Attachment(Uri.fromFile(f), StorageHelper.getMimeTypeInternal(mContext, uri));
            mAttachment.setName(name);
            mAttachment.setSize(f.length());
        }
        return mAttachment;
    }


    public static File createNewAttachmentFileFromHttp(Context mContext, String url)
            throws IOException {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return getFromHttp(url, createNewAttachmentFile(mContext, FileHelper.getFileExtension(url)));
    }


    public static File getFromHttp(String url, File file) throws IOException {
        URL imageUrl = new URL(url);

        FileUtils.copyURLToFile(imageUrl, file);
        return file;
    }


}
