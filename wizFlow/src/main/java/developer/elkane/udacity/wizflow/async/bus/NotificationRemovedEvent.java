

package developer.elkane.udacity.wizflow.async.bus;

import android.service.notification.StatusBarNotification;
import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class NotificationRemovedEvent {

    public StatusBarNotification statusBarNotification;


    public NotificationRemovedEvent(StatusBarNotification statusBarNotification) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.statusBarNotification = statusBarNotification;
    }
}
