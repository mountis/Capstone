

package developer.elkane.udacity.wizflow.models.misc;


import android.util.Log;

import java.util.List;

import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.async.bus.DynamicNavigationReadyEvent;
import developer.elkane.udacity.wizflow.async.bus.NotesUpdatedEvent;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.Constants;


public class DynamicNavigationLookupTable {

    private static DynamicNavigationLookupTable instance;
    int archived;
    int trashed;
    int uncategorized;
    int reminders;


    private DynamicNavigationLookupTable() {
        EventBus.getDefault().register(this);
        update();
    }


    public static DynamicNavigationLookupTable getInstance() {
        if (instance == null) {
            instance = new DynamicNavigationLookupTable();
        }
        return instance;
    }


    public void update() {
        ((Runnable) () -> {
            archived = trashed = uncategorized = reminders = 0;
            List<Note> notes = DbHelper.getInstance().getAllNotes(false);
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).isTrashed()) trashed++;
                else if (notes.get(i).isArchived()) archived++;
                else if (notes.get(i).getAlarm() != null) reminders++;
                if (notes.get(i).getCategory() == null || notes.get(i).getCategory().getId().equals(0L)) {
                    uncategorized++;
                }
            }
            EventBus.getDefault().post(new DynamicNavigationReadyEvent());
            Log.d(Constants.TAG, "Dynamic menu finished counting items");
        }).run();
    }


    public void onEventAsync(NotesUpdatedEvent event) {
        update();
    }


    public int getArchived() {
        return archived;
    }


    public int getTrashed() {
        return trashed;
    }


    public int getReminders() {
        return reminders;
    }


    public int getUncategorized() {
        return uncategorized;
    }

}
