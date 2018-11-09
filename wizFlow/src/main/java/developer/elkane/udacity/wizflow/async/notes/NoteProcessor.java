

package developer.elkane.udacity.wizflow.async.notes;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.async.bus.NotesUpdatedEvent;
import developer.elkane.udacity.wizflow.models.Note;


public abstract class NoteProcessor {

    List<Note> notes;


    protected NoteProcessor(List<Note> notes) {
        this.notes = new ArrayList<>(notes);
    }


    public void process() {
        NotesProcessorTask task = new NotesProcessorTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, notes);
    }


    protected abstract void processNote(Note note);


    class NotesProcessorTask extends AsyncTask<List<Note>, Void, Void> {

        @Override
        protected Void doInBackground(List<Note>... params) {
            List<Note> notes = params[0];
            for (Note note : notes) {
                processNote(note);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            EventBus.getDefault().post(new NotesUpdatedEvent());
        }
    }
}
