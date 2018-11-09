

package developer.elkane.udacity.wizflow.models.listeners;

import developer.elkane.udacity.wizflow.models.Attachment;


public interface OnAttachingFileListener {

    void onAttachingFileErrorOccurred(Attachment mAttachment);

    void onAttachingFileFinished(Attachment mAttachment);
}
