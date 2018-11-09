

package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class NavigationUpdatedEvent {

    public final Object navigationItem;


    public NavigationUpdatedEvent(Object navigationItem) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.navigationItem = navigationItem;
    }
}
