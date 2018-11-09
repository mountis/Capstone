

package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import java.util.ArrayList;

import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.Constants;


public class NotesLoadedEvent {

    public ArrayList<Note> notes;


    public NotesLoadedEvent(ArrayList<Note> notes) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.notes = notes;
    }
}
