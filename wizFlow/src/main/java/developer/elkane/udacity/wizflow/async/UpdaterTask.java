
package developer.elkane.udacity.wizflow.async;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

import developer.elkane.udacity.wizflow.BuildConfig;
import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.helpers.AppVersionHelper;
import developer.elkane.udacity.wizflow.models.misc.PlayStoreMetadataFetcherResult;
import developer.elkane.udacity.wizflow.utils.ConnectionManager;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.SystemHelper;
import it.feio.android.analitica.AnalyticsHelper;


public class UpdaterTask extends AsyncTask<String, Void, Void> {

    private static final String BETA = " Beta ";
    private final WeakReference<Activity> mActivityReference;
    private final Activity mActivity;
    private final SharedPreferences prefs;
    private boolean promptUpdate = false;
    private long now;


    public UpdaterTask(Activity mActivity) {
        this.mActivityReference = new WeakReference<>(mActivity);
        this.mActivity = mActivity;
        this.prefs = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }


    @Override
    protected void onPreExecute() {
        now = System.currentTimeMillis();
        if (WizFlow.isDebugBuild() || !ConnectionManager.internetAvailable(WizFlow.getAppContext()) || now < prefs.getLong(Constants
                .PREF_LAST_UPDATE_CHECK, 0) + Constants.UPDATE_MIN_FREQUENCY) {
            cancel(true);
        }
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(String... params) {
        if (!isCancelled()) {
            try {
                promptUpdate = isVersionUpdated(getAppData());
                if (promptUpdate) {
                    prefs.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK, now).apply();
                }
            } catch (Exception e) {
                Log.w(Constants.TAG, "Error fetching app metadata", e);
            }
        }
        return null;
    }


    private void promptUpdate() {
        new MaterialDialog.Builder(mActivityReference.get())
                .title(developer.elkane.udacity.wizflow.R.string.app_name)
                .content(developer.elkane.udacity.wizflow.R.string.new_update_available)
                .positiveText(developer.elkane.udacity.wizflow.R.string.update)
                .negativeText(developer.elkane.udacity.wizflow.R.string.not_now)
                .negativeColorRes(developer.elkane.udacity.wizflow.R.color.colorPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        if (isGooglePlayAvailable()) {
                            ((WizFlow) mActivity.getApplication()).getAnalyticsHelper().trackEvent(AnalyticsHelper.CATEGORIES.UPDATE, "Play Store");
                            mActivityReference.get().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                    ("market://details?id=" + mActivity.getPackageName())));
                        }
                    }
                }).build().show();
    }


    @Override
    protected void onPostExecute(Void result) {
        if (isAlive(mActivityReference)) {
            if (promptUpdate) {
                promptUpdate();
            } else {
                try {
                    boolean appVersionUpdated = AppVersionHelper.isAppUpdated(mActivity);
                    if (appVersionUpdated) {
                        restoreReminders();
                        AppVersionHelper.updateAppVersionInPreferences(mActivity);
                    }
                } catch (NameNotFoundException e) {
                    Log.e(Constants.TAG, "Error retrieving app version", e);
                }
            }
        }
    }

    private void restoreReminders() {
        Intent service = new Intent(mActivity, AlarmRestoreOnRebootService.class);
        mActivity.startService(service);
    }


    private boolean isAlive(WeakReference<Activity> weakActivityReference) {
        return !(weakActivityReference.get() == null || weakActivityReference.get().isFinishing());
    }


    private PlayStoreMetadataFetcherResult getAppData() throws IOException {
        InputStream is = null;
        InputStreamReader inputStreamReader = null;
        try {
            StringBuilder sb = new StringBuilder();
            URLConnection conn = new URL(BuildConfig.VERSION_CHECK_URL).openConnection();
            is = conn.getInputStream();
            inputStreamReader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(inputStreamReader);

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }

            return new Gson().fromJson(sb.toString(), PlayStoreMetadataFetcherResult.class);
        } finally {
            SystemHelper.closeCloseable(inputStreamReader, is);
        }
    }


    private boolean isVersionUpdated(PlayStoreMetadataFetcherResult playStoreMetadataFetcherResult)
            throws NameNotFoundException {

        String playStoreVersion = playStoreMetadataFetcherResult.getSoftwareVersion();

        PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(
                mActivity.getPackageName(), 0);
        String installedVersion = pInfo.versionName;

        String[] playStoreVersionArray = playStoreVersion.split(BETA)[0].split("\\.");
        String[] installedVersionArray = installedVersion.split(BETA)[0].split("\\.");

        String playStoreVersionString = playStoreVersionArray[0];
        String installedVersionString = installedVersionArray[0];
        for (int i = 1; i < playStoreVersionArray.length; i++) {
            playStoreVersionString += String.format("%02d", Integer.parseInt(playStoreVersionArray[i]));
            installedVersionString += String.format("%02d", Integer.parseInt(installedVersionArray[i]));
        }

        boolean playStoreHasMoreRecentVersion = Integer.parseInt(playStoreVersionString) > Integer.parseInt(installedVersionString);
        boolean outOfBeta = Integer.parseInt(playStoreVersionString) == Integer.parseInt(installedVersionString)
                && playStoreVersion.split("b").length == 1 && installedVersion.split("b").length == 2;

        return playStoreHasMoreRecentVersion || outOfBeta;
    }


    private boolean isGooglePlayAvailable() {
        boolean available = true;
        try {
            mActivity.getPackageManager().getPackageInfo("com.android.vending", 0);
        } catch (NameNotFoundException e) {
            Log.d(getClass().getName(), "Google Play app not available on device");
            available = false;
        }
        return available;
    }
}
