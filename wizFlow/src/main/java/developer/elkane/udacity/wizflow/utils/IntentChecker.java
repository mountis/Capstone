

package developer.elkane.udacity.wizflow.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;


public class IntentChecker {


    public static String resolveActivityPackage(Context ctx, Intent intent) {
        ComponentName activity = intent.resolveActivity(ctx.getPackageManager());
        return activity != null ? activity.getPackageName() : "";
    }


    public static boolean isAvailable(Context ctx, Intent intent, String[] features) {
        boolean res = getCompatiblePackages(ctx, intent).size() > 0;

        if (features != null) {
            for (String feature : features) {
                res = res && ctx.getPackageManager().hasSystemFeature(feature);
            }
        }
        return res;
    }


    public static boolean checkAction(Intent i, String action) {
        return action.equals(i.getAction());
    }


    public static boolean checkAction(Intent i, String... actions) {
        for (String action : actions) {
            if (checkAction(i, action)) return true;
        }
        return false;
    }

    private static List<ResolveInfo> getCompatiblePackages(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        return mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }
}
