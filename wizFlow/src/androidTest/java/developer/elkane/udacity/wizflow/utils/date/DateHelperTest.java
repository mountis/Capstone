

package developer.elkane.udacity.wizflow.utils.date;

import android.test.InstrumentationTestCase;

import org.junit.Assert;

import java.util.Calendar;

import developer.elkane.udacity.wizflow.helpers.date.DateHelper;


public class DateHelperTest extends InstrumentationTestCase {

    private int TEN_MINUTES = 10 * 60 * 1000;
    private int MILLISEC_TO_HOURS_RATIO = 60 * 60 * 1000;

    public void testNextReminderFromRecurrenceRule() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long reminder = Calendar.getInstance().getTimeInMillis() + TEN_MINUTES;

        // Daily test
        String rruleDaily = "FREQ=DAILY;COUNT=30;WKST=MO";
        long nextReminder = DateHelper.nextReminderFromRecurrenceRule(reminder, currentTime, rruleDaily);
        Assert.assertNotEquals(0, nextReminder);
        Assert.assertEquals(24 - 1, (nextReminder - reminder) / MILLISEC_TO_HOURS_RATIO);

        // 3-Daily test
        String rruleDaily2 = "FREQ=DAILY;COUNT=30;INTERVAL=3";
        long nextReminder2 = DateHelper.nextReminderFromRecurrenceRule(reminder, currentTime, rruleDaily2);
        Assert.assertNotEquals(0, nextReminder2);
        long delta = (nextReminder2 - reminder) / MILLISEC_TO_HOURS_RATIO;
        Assert.assertTrue(delta == 3 * 24 - 2 || delta == 3 * 24 - 1); // The results depends on the day of week


    }

}
