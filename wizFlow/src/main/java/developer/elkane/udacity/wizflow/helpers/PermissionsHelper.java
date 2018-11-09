

package developer.elkane.udacity.wizflow.helpers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.tbruyelle.rxpermissions.RxPermissions;

import developer.elkane.udacity.wizflow.models.listeners.OnPermissionRequestedListener;


public class PermissionsHelper {


    public static void requestPermission(Activity activity, String permission, int rationaleDescription, View
            messageView, OnPermissionRequestedListener onPermissionRequestedListener) {

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Snackbar.make(messageView, rationaleDescription, Snackbar.LENGTH_INDEFINITE)
                        .setAction(developer.elkane.udacity.wizflow.R.string.ok, view -> {
                            requestPermissionExecute(activity, permission, onPermissionRequestedListener, messageView);
                        })
                        .show();
            } else {
                requestPermissionExecute(activity, permission, onPermissionRequestedListener, messageView);
            }
        } else {
            onPermissionRequestedListener.onPermissionGranted();
        }
    }


    private static void requestPermissionExecute(Activity activity, String permission, OnPermissionRequestedListener
            onPermissionRequestedListener, View messageView) {
        RxPermissions.getInstance(activity)
                .request(permission)
                .subscribe(granted -> {
                    if (granted) {
                        onPermissionRequestedListener.onPermissionGranted();
                    } else {
                        String msg = activity.getString(developer.elkane.udacity.wizflow.R.string.permission_not_granted) + ": " + permission;
                        Snackbar.make(messageView, msg, Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
