

package developer.elkane.udacity.wizflow.async.notes;

import java.util.List;

import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.ReminderHelper;
import developer.elkane.udacity.wizflow.utils.ShortcutHelper;


public class NoteProcessorTrash extends NoteProcessor {

    boolean trash;


    public NoteProcessorTrash(List<Note> notes, boolean trash) {
        super(notes);
        this.trash = trash;
    }


    @Override
    protected void processNote(Note note) {
        if (trash) {
            ShortcutHelper.removeshortCut(WizFlow.getAppContext(), note);
            ReminderHelper.removeReminder(WizFlow.getAppContext(), note);
        } else {
            ReminderHelper.addReminder(WizFlow.getAppContext(), note);
        }
        DbHelper.getInstance().trashNote(note, trash);
    }
}
