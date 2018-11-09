
package developer.elkane.udacity.wizflow.utils;

import android.content.Context;
import android.net.ConnectivityManager;


public class ConnectionManager {


    public static boolean internetAvailable(Context ctx) {
        boolean result = false;
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null) {
            return conMgr.getActiveNetworkInfo().isConnected();
        }
        return result;
    }
}
