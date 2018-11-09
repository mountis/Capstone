

package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class NavigationUpdatedNavDrawerClosedEvent {

    public final Object navigationItem;


    public NavigationUpdatedNavDrawerClosedEvent(Object navigationItem) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.navigationItem = navigationItem;
    }
}
