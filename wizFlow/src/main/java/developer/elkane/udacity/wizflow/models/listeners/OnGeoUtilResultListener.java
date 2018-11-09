
package developer.elkane.udacity.wizflow.models.listeners;


import android.location.Location;


public interface OnGeoUtilResultListener {

    void onAddressResolved(String address);

    void onCoordinatesResolved(Location location, String address);

    void onLocationRetrieved(Location location);

    void onLocationUnavailable();
}
