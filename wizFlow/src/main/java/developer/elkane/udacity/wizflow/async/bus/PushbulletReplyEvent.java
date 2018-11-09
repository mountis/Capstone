

package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class PushbulletReplyEvent {

    public String message;

    public PushbulletReplyEvent(String message) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.message = message;
    }
}
