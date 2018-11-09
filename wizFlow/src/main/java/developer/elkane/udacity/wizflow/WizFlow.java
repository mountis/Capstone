
package developer.elkane.udacity.wizflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import developer.elkane.udacity.wizflow.helpers.LanguageHelper;
import developer.elkane.udacity.wizflow.utils.Constants;
import it.feio.android.analitica.AnalyticsHelper;
import it.feio.android.analitica.AnalyticsHelperFactory;
import it.feio.android.analitica.MockAnalyticsHelper;
import it.feio.android.analitica.exceptions.AnalyticsInstantiationException;
import it.feio.android.analitica.exceptions.InvalidIdentifierException;


@ReportsCrashes(httpMethod = Method.POST, reportType = Type.FORM, formUri = BuildConfig.CRASH_REPORTING_URL, mode =
        ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.crash_toast)
public class WizFlow extends MultiDexApplication {

    static SharedPreferences prefs;
    private static Context mContext;
    private static RefWatcher refWatcher;
    private AnalyticsHelper analyticsHelper;

    @NonNull
    public static boolean isDebugBuild() {
        return BuildConfig.BUILD_TYPE.equals("debug");
    }

    public static Context getAppContext() {
        return WizFlow.mContext;
    }

    public static RefWatcher getRefWatcher() {
        return WizFlow.refWatcher;
    }

    public static SharedPreferences getSharedPreferences() {
        return getAppContext().getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        if (isDebugBuild()) {
            StrictMode.enableDefaults();
        }

        initLeakCanary();
    }

    private void initLeakCanary() {
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            refWatcher = LeakCanary.install(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String language = prefs.getString(Constants.PREF_LANG, "");
        LanguageHelper.updateLanguage(this, language);
    }

    public AnalyticsHelper getAnalyticsHelper() {
        if (analyticsHelper == null) {
            boolean enableAnalytics = prefs.getBoolean(Constants.PREF_SEND_ANALYTICS, true);
            try {
                String[] analyticsParams = BuildConfig.ANALYTICS_PARAMS.split(Constants.PROPERTIES_PARAMS_SEPARATOR);
                analyticsHelper = new AnalyticsHelperFactory().getAnalyticsHelper(this, enableAnalytics,
                        analyticsParams);
            } catch (AnalyticsInstantiationException | InvalidIdentifierException e) {
                analyticsHelper = new MockAnalyticsHelper();
            }
        }
        return analyticsHelper;
    }
}
