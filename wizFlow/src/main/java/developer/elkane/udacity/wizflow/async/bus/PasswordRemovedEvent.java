

package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class PasswordRemovedEvent {

    public PasswordRemovedEvent() {
        Log.d(Constants.TAG, this.getClass().getName());
    }
}
