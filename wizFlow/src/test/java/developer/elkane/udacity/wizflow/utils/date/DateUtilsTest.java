

package developer.elkane.udacity.wizflow.utils.date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.Locale;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DateUtils.class})
public class DateUtilsTest {

    @Test
    public void prettyTime() {
        long now = Calendar.getInstance().getTimeInMillis();

        String prettyTime = DateUtils.prettyTime(now, Locale.ENGLISH);
        Assert.assertEquals(prettyTime.toLowerCase(), "moments ago");

        prettyTime = DateUtils.prettyTime(now + 10 * 60 * 1000, Locale.ENGLISH);
        Assert.assertEquals(prettyTime.toLowerCase(), "10 minutes from now");

        prettyTime = DateUtils.prettyTime(now + 24 * 60 * 60 * 1000, Locale.ITALIAN);
        Assert.assertEquals(prettyTime.toLowerCase(), "fra 24 ore");

        prettyTime = DateUtils.prettyTime(now + 25 * 60 * 60 * 1000, Locale.ITALIAN);
        Assert.assertEquals(prettyTime.toLowerCase(), "fra 1 giorno");

        prettyTime = DateUtils.prettyTime(null, Locale.JAPANESE);
        Assert.assertNotNull(prettyTime.toLowerCase());
        Assert.assertEquals(prettyTime.toLowerCase().length(), 0);
    }

    @Test
    public void getPresetReminder() {
        long mockedNextMinute = 1497315847L;
        PowerMockito.stub(PowerMockito.method(DateUtils.class, "getNextMinute")).toReturn(mockedNextMinute);
        assertTrue(mockedNextMinute == DateUtils.getPresetReminder(null));
    }

    @Test
    public void isFuture() {
        String nextMinute = String.valueOf(Calendar.getInstance().getTimeInMillis() + 60000);
        String previousMinute = String.valueOf(Calendar.getInstance().getTimeInMillis() - 60000);
        assertTrue(DateUtils.isFuture(nextMinute));
        assertFalse(DateUtils.isFuture(previousMinute));
    }

    @Test
    public void isSameDay() {
        long today = Calendar.getInstance().getTimeInMillis();
        long tomorrow = today + (1000 * 60 * 60 * 24);
        assertTrue(DateUtils.isSameDay(today, today));
        assertFalse(DateUtils.isSameDay(today, tomorrow));
    }


}