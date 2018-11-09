
package developer.elkane.udacity.wizflow.async.notes;

import java.util.List;

import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.Note;


public class NoteProcessorCategorize extends NoteProcessor {

    Category category;


    public NoteProcessorCategorize(List<Note> notes, Category category) {
        super(notes);
        this.category = category;
    }


    @Override
    protected void processNote(Note note) {
        note.setCategory(category);
        DbHelper.getInstance().updateNote(note, false);
    }
}
