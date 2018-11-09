
package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class NotesMergeEvent {

    public final boolean keepMergedNotes;


    public NotesMergeEvent(boolean keepMergedNotes) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.keepMergedNotes = keepMergedNotes;
    }
}
