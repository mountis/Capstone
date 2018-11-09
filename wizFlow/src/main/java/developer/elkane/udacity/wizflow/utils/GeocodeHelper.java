
package developer.elkane.udacity.wizflow.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import developer.elkane.udacity.wizflow.BuildConfig;
import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.helpers.GeoCodeProviderFactory;
import developer.elkane.udacity.wizflow.models.listeners.OnGeoUtilResultListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.rx.ObservableFactory;
import rx.Observable;
import rx.Subscriber;


public class GeocodeHelper implements LocationListener {

    private static final String LOG_TAG = Constants.TAG;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static GeocodeHelper instance;
    private static LocationManager locationManager;


    private GeocodeHelper() {
        instance = this;
        locationManager = (LocationManager) WizFlow.getAppContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public static void getLocation(OnGeoUtilResultListener onGeoUtilResultListener) {
        SmartLocation.LocationControl bod = SmartLocation.with(WizFlow.getAppContext())
                .location(GeoCodeProviderFactory.getProvider(WizFlow.getAppContext()))
                .config(LocationParams.NAVIGATION).oneFix();

        Observable<Location> locations = ObservableFactory.from(bod).timeout(2, TimeUnit.SECONDS);
        locations.subscribe(new Subscriber<Location>() {
            @Override
            public void onNext(Location location) {
                onGeoUtilResultListener.onLocationRetrieved(location);
                unsubscribe();
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                onGeoUtilResultListener.onLocationUnavailable();
                unsubscribe();
            }
        });
    }

    public static void stop() {
        SmartLocation.with(WizFlow.getAppContext()).location().stop();
        if (Geocoder.isPresent()) {
            SmartLocation.with(WizFlow.getAppContext()).geocoding().stop();
        }
    }

    static String getAddressFromCoordinates(Context mContext, double latitude,
                                            double longitude) throws IOException {
        String addressString = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            if (address != null) {
                addressString = address.getThoroughfare() + ", " + address.getLocality();
            }
        }
        return addressString;
    }

    public static void getAddressFromCoordinates(Location location,
                                                 final OnGeoUtilResultListener onGeoUtilResultListener) {
        if (!Geocoder.isPresent()) {
            onGeoUtilResultListener.onAddressResolved("");
        } else {
            SmartLocation.with(WizFlow.getAppContext()).geocoding().reverse(location, (location1, list) -> {
                String address = list.size() > 0 ? list.get(0).getAddressLine(0) : null;
                onGeoUtilResultListener.onAddressResolved(address);
            });
        }
    }

    public static double[] getCoordinatesFromAddress(Context mContext, String address)
            throws IOException {
        double[] result = new double[2];
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocationName(address, 1);
        if (addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            result[0] = latitude;
            result[1] = longitude;
        }
        return result;
    }

    public static void getCoordinatesFromAddress(String address, final OnGeoUtilResultListener
            listener) {
        SmartLocation.with(WizFlow.getAppContext()).geocoding().direct(address, (name, results) -> {
            if (results.size() > 0) {
                listener.onCoordinatesResolved(results.get(0).getLocation(), address);
            }
        });
    }

    public static List<String> autocomplete(String input) {
        String MAPS_API_KEY = BuildConfig.MAPS_API_KEY;
        if (TextUtils.isEmpty(MAPS_API_KEY)) {
            return Collections.emptyList();
        }
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        InputStreamReader in = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key=" + MAPS_API_KEY + "&input=" +
                    URLEncoder.encode(input, "utf8"));
            conn = (HttpURLConnection) url.openConnection();
            in = new InputStreamReader(conn.getInputStream());
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(Constants.TAG, "Error processing Places API URL");
            return null;
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error connecting to Places API");
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Error closing address autocompletion InputStream");
                }
            }
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            resultList = new ArrayList<>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Cannot process JSON results", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            SystemHelper.closeCloseable(in);
        }
        return resultList;
    }

    public static boolean areCoordinates(String string) {
        Pattern p = Pattern.compile("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|" +
                "([1-9]?\\d))(\\.\\d+)?)$");
        Matcher m = p.matcher(string);
        return m.matches();
    }

    @Override
    public void onLocationChanged(Location newLocation) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
