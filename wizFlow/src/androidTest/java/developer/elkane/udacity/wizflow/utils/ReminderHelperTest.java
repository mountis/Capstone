
package developer.elkane.udacity.wizflow.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import developer.elkane.udacity.wizflow.models.Note;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ReminderHelperTest {

    @Test
    public void shouldGetRequestCode() {
        Long now = Calendar.getInstance().getTimeInMillis();
        Note note = new Note();
        note.setAlarm(now);
        int requestCode = ReminderHelper.getRequestCode(note);
        int requestCode2 = ReminderHelper.getRequestCode(note);
        assertEquals(requestCode, requestCode2);
        assertTrue(String.valueOf(now).startsWith(String.valueOf(requestCode)));
    }

    @Test
    public void shouldAddReminder() {
        Note note = buildNote();
        ReminderHelper.addReminder(InstrumentationRegistry.getTargetContext(), note);
        boolean reminderActive = ReminderHelper.checkReminder(InstrumentationRegistry.getTargetContext(), note);
        assertTrue(reminderActive);
    }

    @Test
    public void shouldNotAddReminderWithPassedTime() {
        Note note = buildNote();
        note.setAlarm(Calendar.getInstance().getTimeInMillis());
        ReminderHelper.addReminder(InstrumentationRegistry.getTargetContext(), note);
        boolean reminderActive = ReminderHelper.checkReminder(InstrumentationRegistry.getTargetContext(), note);
        assertFalse(reminderActive);
    }

    @Test
    public void shouldRemoveReminder() {
        Note note = buildNote();
        ReminderHelper.addReminder(InstrumentationRegistry.getTargetContext(), note);
        boolean reminderActive = ReminderHelper.checkReminder(InstrumentationRegistry.getTargetContext(), note);
        ReminderHelper.removeReminder(InstrumentationRegistry.getTargetContext(), note);
        boolean reminderRemoved = ReminderHelper.checkReminder(InstrumentationRegistry.getTargetContext(), note);
        assertTrue(reminderActive);
        assertFalse(reminderRemoved);
    }

    private Note buildNote() {
        Long now = Calendar.getInstance().getTimeInMillis();
        Note note = new Note();
        note.setCreation(now);
        note.setAlarm(now + 1000);
        return note;
    }
}
