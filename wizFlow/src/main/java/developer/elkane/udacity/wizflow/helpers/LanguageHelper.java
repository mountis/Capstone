
package developer.elkane.udacity.wizflow.helpers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Locale;

import developer.elkane.udacity.wizflow.utils.Constants;

import static android.content.Context.MODE_MULTI_PROCESS;


public class LanguageHelper {


    @SuppressLint("ApplySharedPref")
    public static Context updateLanguage(Context ctx, String lang) {
        SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
        String language = prefs.getString(Constants.PREF_LANG, "");

        Locale locale = null;
        if (TextUtils.isEmpty(language) && lang == null) {
            locale = Locale.getDefault();
            prefs.edit().putString(Constants.PREF_LANG, locale.toString()).commit();
        } else if (lang != null) {
            locale = getLocale(lang);
            prefs.edit().putString(Constants.PREF_LANG, lang).commit();
        } else if (!TextUtils.isEmpty(language)) {
            locale = getLocale(language);
        }

        return setLocale(ctx, locale);
    }

    private static Context setLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            context.createConfigurationContext(configuration);

        } else {
            configuration.locale = locale;
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }
        return context;
    }


    public static Locale getLocale(String lang) {
        if (lang.contains("_")) {
            return new Locale(lang.split("_")[0], lang.split("_")[1]);
        } else {
            return new Locale(lang);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @NonNull
    public static String getLocalizedString(Context context, String desiredLocale, int resourceId) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(getLocale(desiredLocale));
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources().getString(resourceId);
    }

}
