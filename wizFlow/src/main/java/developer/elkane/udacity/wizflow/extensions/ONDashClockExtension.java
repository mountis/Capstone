

package developer.elkane.udacity.wizflow.extensions;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.TextHelper;
import developer.elkane.udacity.wizflow.utils.date.DateUtils;


public class ONDashClockExtension extends DashClockExtension {

    private DashClockUpdateReceiver mDashClockReceiver;

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        LocalBroadcastManager broadcastMgr = LocalBroadcastManager.getInstance(this);
        if (mDashClockReceiver != null) {
            broadcastMgr.unregisterReceiver(mDashClockReceiver);
        }
        mDashClockReceiver = new DashClockUpdateReceiver();
        broadcastMgr.registerReceiver(mDashClockReceiver, new IntentFilter(Constants.INTENT_UPDATE_DASHCLOCK));
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onUpdateData(int reason) {

        Map<Counters, List<Note>> notesCounters = getNotesCounters();
        int reminders = notesCounters.get(Counters.REMINDERS).size();

        StringBuilder expandedTitle = new StringBuilder();
        expandedTitle.append(notesCounters.get(Counters.ACTIVE).size()).append(" ").append(getString(developer.elkane.udacity.wizflow.R.string.notes).toLowerCase());
        if (reminders > 0) {
            expandedTitle.append(", ").append(reminders).append(" ").append(getString(developer.elkane.udacity.wizflow.R.string.reminders));
        }

        StringBuilder expandedBody = new StringBuilder();

        if (notesCounters.get(Counters.TODAY).size() > 0) {
            expandedBody.append(notesCounters.get(Counters.TODAY).size()).append(" ").append(getString(developer.elkane.udacity.wizflow.R.string.today)).append(":");
            for (Note todayReminder : notesCounters.get(Counters.TODAY)) {
                expandedBody.append(System.getProperty("line.separator")).append(("☆ ")).append(getNoteTitle(this,
                        todayReminder));
            }
            expandedBody.append("\n");
        }

        if (notesCounters.get(Counters.TOMORROW).size() > 0) {
            expandedBody.append(notesCounters.get(Counters.TOMORROW).size()).append(" ").append(getString(developer.elkane.udacity.wizflow.R.string.tomorrow)).append(":");
            for (Note tomorrowReminder : notesCounters.get(Counters.TOMORROW)) {
                expandedBody.append(System.getProperty("line.separator")).append(("☆ ")).append(getNoteTitle(this,
                        tomorrowReminder));
            }
        }

        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setAction(Intent.ACTION_MAIN);
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(developer.elkane.udacity.wizflow.R.drawable.ic_stat_literal_icon)
                .status(String.valueOf(notesCounters.get(Counters.ACTIVE).size()))
                .expandedTitle(expandedTitle.toString())
                .expandedBody(expandedBody.toString())
                .clickIntent(launchIntent));
    }

    private String getNoteTitle(Context context, Note note) {
        return TextHelper.getAlternativeTitle(context, note, TextHelper.parseTitleAndContent(context, note)[0]);
    }

    private Map<Counters, List<Note>> getNotesCounters() {
        Map noteCounters = new HashMap<>();
        List<Note> activeNotes = new ArrayList<>();
        List<Note> reminders = new ArrayList<>();
        List<Note> today = new ArrayList<>();
        List<Note> tomorrow = new ArrayList<>();
        for (Note note : DbHelper.getInstance().getNotesActive()) {
            activeNotes.add(note);
            if (note.getAlarm() != null && !note.isReminderFired()) {
                reminders.add(note);
                if (DateUtils.isSameDay(Long.valueOf(note.getAlarm()), Calendar.getInstance().getTimeInMillis())) {
                    today.add(note);
                } else if ((Long.valueOf(note.getAlarm()) - Calendar.getInstance().getTimeInMillis()) / (1000 * 60 *
                        60) < 24) {
                    tomorrow.add(note);
                }
            }
        }
        noteCounters.put(Counters.ACTIVE, activeNotes);
        noteCounters.put(Counters.REMINDERS, reminders);
        noteCounters.put(Counters.TODAY, today);
        noteCounters.put(Counters.TOMORROW, tomorrow);
        return noteCounters;
    }


    private enum Counters {ACTIVE, REMINDERS, TODAY, TOMORROW}

    public class DashClockUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(UPDATE_REASON_MANUAL);
        }

    }
}
