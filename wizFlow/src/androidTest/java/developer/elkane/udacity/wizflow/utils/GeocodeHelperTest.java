
package developer.elkane.udacity.wizflow.utils;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.Suppress;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import developer.elkane.udacity.wizflow.WizFlow;


public class GeocodeHelperTest extends InstrumentationTestCase {

    @Test
    @Suppress
    public void testGetAddressFromCoordinates() throws IOException {
        if (ConnectionManager.internetAvailable(WizFlow.getAppContext())) {
            Double LAT = 43.799328;
            Double LON = 11.171552;
            String address = GeocodeHelper.getAddressFromCoordinates(WizFlow.getAppContext(), LAT, LON);
            Assert.assertTrue(address.length() > 0);
        }
    }
}
