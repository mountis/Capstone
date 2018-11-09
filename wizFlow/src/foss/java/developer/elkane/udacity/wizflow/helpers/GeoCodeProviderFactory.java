package developer.elkane.udacity.wizflow.helpers;

import android.content.Context;

import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;


public class GeoCodeProviderFactory {

    public static LocationProvider getProvider(Context context) {
        return new LocationManagerProvider();
    }
}