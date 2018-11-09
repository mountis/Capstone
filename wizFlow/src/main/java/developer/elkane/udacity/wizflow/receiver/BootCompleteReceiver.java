
package developer.elkane.udacity.wizflow.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import developer.elkane.udacity.wizflow.async.AlarmRestoreOnRebootService;
import developer.elkane.udacity.wizflow.utils.Constants;


public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.i(Constants.TAG, "System rebooted: refreshing reminders");

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent service = new Intent(ctx, AlarmRestoreOnRebootService.class);
            ctx.startService(service);
        }

    }


}
