

package developer.elkane.udacity.wizflow.helpers;


import org.apache.commons.io.FileUtils;

import java.io.File;

import developer.elkane.udacity.wizflow.models.Attachment;


public class AttachmentsHelper {


    public static String getSize(Attachment attachment) {
        long sizeInKb = attachment.getSize();
        if (attachment.getSize() == 0) {
            sizeInKb = new File(attachment.getUri().getPath()).length();
        }
        return FileUtils.byteCountToDisplaySize(sizeInKb);
    }
}
