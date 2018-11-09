
package developer.elkane.udacity.wizflow.extensions;

import android.util.Log;

import com.pushbullet.android.extension.MessagingExtension;

import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.async.bus.PushbulletReplyEvent;
import developer.elkane.udacity.wizflow.utils.Constants;


public class PushBulletExtension extends MessagingExtension {

    private static final String TAG = "PushBulletExtension";


    @Override
    protected void onMessageReceived(final String conversationIden, final String message) {
        Log.i(Constants.TAG, "Pushbullet MessagingExtension: onMessageReceived(" + conversationIden + ", " + message
                + ")");
        EventBus.getDefault().post(new PushbulletReplyEvent(message));
    }


    @Override
    protected void onConversationDismissed(final String conversationIden) {
        Log.i(Constants.TAG, "Pushbullet MessagingExtension: onConversationDismissed(" + conversationIden + ")");
    }
}
