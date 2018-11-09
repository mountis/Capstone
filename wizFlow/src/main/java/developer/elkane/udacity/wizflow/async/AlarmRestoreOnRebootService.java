
package developer.elkane.udacity.wizflow.async;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import developer.elkane.udacity.wizflow.BaseActivity;
import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.ReminderHelper;


public class AlarmRestoreOnRebootService extends IntentService {

    public AlarmRestoreOnRebootService() {
        super("AlarmRestoreOnRebootService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(Constants.TAG, "System rebooted: service refreshing reminders");
        Context mContext = getApplicationContext();

        BaseActivity.notifyAppWidgets(mContext);

        List<Note> notes = DbHelper.getInstance().getNotesWithReminderNotFired();
        Log.d(Constants.TAG, "Found " + notes.size() + " reminders");
        for (Note note : notes) {
            ReminderHelper.addReminder(WizFlow.getAppContext(), note);
        }
    }

}
