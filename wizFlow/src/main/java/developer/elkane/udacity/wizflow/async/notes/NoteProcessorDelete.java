

package developer.elkane.udacity.wizflow.async.notes;

import java.util.List;

import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.StorageHelper;


public class NoteProcessorDelete extends NoteProcessor {


    private final boolean keepAttachments;


    public NoteProcessorDelete(List<Note> notes) {
        this(notes, false);
    }


    public NoteProcessorDelete(List<Note> notes, boolean keepAttachments) {
        super(notes);
        this.keepAttachments = keepAttachments;
    }


    @Override
    protected void processNote(Note note) {
        DbHelper db = DbHelper.getInstance();
        if (db.deleteNote(note) && !keepAttachments) {
            for (Attachment mAttachment : note.getAttachmentsList()) {
                StorageHelper.deleteExternalStoragePrivateFile(WizFlow.getAppContext(), mAttachment.getUri()
                        .getLastPathSegment());
            }
        }
    }
}
