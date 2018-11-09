

package developer.elkane.udacity.wizflow.utils;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;


public class SystemHelper {

    public static void restartApp(final Context mContext, Class activityClass) {
        System.exit(0);
    }


    public static void closeCloseable(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    Log.w(Constants.TAG, "Can't close " + closeable, e);
                }
            }
        }
    }
}
