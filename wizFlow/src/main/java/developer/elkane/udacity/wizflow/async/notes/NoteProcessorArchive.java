

package developer.elkane.udacity.wizflow.async.notes;

import java.util.List;

import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Note;


public class NoteProcessorArchive extends NoteProcessor {

    boolean archive;


    public NoteProcessorArchive(List<Note> notes, boolean archive) {
        super(notes);
        this.archive = archive;
    }


    @Override
    protected void processNote(Note note) {
        DbHelper.getInstance().archiveNote(note, archive);
    }
}
