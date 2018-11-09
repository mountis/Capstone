
package developer.elkane.udacity.wizflow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;

import org.apache.commons.io.FilenameUtils;

import developer.elkane.udacity.wizflow.models.Attachment;
import it.feio.android.simplegallery.util.BitmapUtils;


public class BitmapHelper {


    public static Bitmap getBitmapFromAttachment(Context mContext, Attachment mAttachment, int width, int height) {
        Bitmap bmp = null;
        String path;
        mAttachment.getUri().getPath();

        if (Constants.MIME_TYPE_VIDEO.equals(mAttachment.getMime_type())) {
            path = StorageHelper.getRealPathFromURI(mContext, mAttachment.getUri());
            if (path == null) {
                path = FileHelper.getPath(mContext, mAttachment.getUri());
            }
            bmp = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
            if (bmp == null) {
                return null;
            } else {
                bmp = BitmapUtils.createVideoThumbnail(mContext, bmp, width, height);
            }

        } else if (Constants.MIME_TYPE_IMAGE.equals(mAttachment.getMime_type())
                || Constants.MIME_TYPE_SKETCH.equals(mAttachment.getMime_type())) {
            try {
                bmp = BitmapUtils.getThumbnail(mContext, mAttachment.getUri(), width, height);
            } catch (NullPointerException e) {
                bmp = null;
            }

        } else if (Constants.MIME_TYPE_AUDIO.equals(mAttachment.getMime_type())) {
            bmp = ThumbnailUtils.extractThumbnail(
                    BitmapUtils.decodeSampledBitmapFromResourceMemOpt(mContext.getResources().openRawResource(developer.elkane.udacity.wizflow.R
                            .raw.play), width, height), width, height);

        } else if (Constants.MIME_TYPE_FILES.equals(mAttachment.getMime_type())) {

            if (Constants.MIME_TYPE_CONTACT_EXT.equals(FilenameUtils.getExtension(mAttachment.getName()))) {
                bmp = ThumbnailUtils.extractThumbnail(
                        BitmapUtils.decodeSampledBitmapFromResourceMemOpt(mContext.getResources().openRawResource(developer.elkane.udacity.wizflow.R
                                .raw.vcard), width, height), width, height);
            } else {
                bmp = ThumbnailUtils.extractThumbnail(
                        BitmapUtils.decodeSampledBitmapFromResourceMemOpt(mContext.getResources().openRawResource(developer.elkane.udacity.wizflow.R
                                .raw.files), width, height), width, height);
            }
        }

        return bmp;
    }


    public static Uri getThumbnailUri(Context mContext, Attachment mAttachment) {
        Uri uri = mAttachment.getUri();
        String mimeType = StorageHelper.getMimeType(uri.toString());
        if (!TextUtils.isEmpty(mimeType)) {
            String type = mimeType.split("/")[0];
            String subtype = mimeType.split("/")[1];
            switch (type) {
                case "image":
                case "video":
                    break;
                case "audio":
                    uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + developer.elkane.udacity.wizflow.R.raw.play);
                    break;
                default:
                    int drawable = "x-vcard".equals(subtype) ? developer.elkane.udacity.wizflow.R.raw.vcard : developer.elkane.udacity.wizflow.R.raw.files;
                    uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + drawable);
                    break;
            }
        } else {
            uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + developer.elkane.udacity.wizflow.R.raw.files);
        }
        return uri;
    }
}
