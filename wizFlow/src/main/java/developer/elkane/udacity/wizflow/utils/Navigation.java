

package developer.elkane.udacity.wizflow.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.models.Category;


public class Navigation {

    public static final int NOTES = 0;
    public static final int ARCHIVE = 1;
    public static final int REMINDERS = 2;
    public static final int TRASH = 3;
    public static final int UNCATEGORIZED = 4;
    public static final int CATEGORY = 5;


    public static int getNavigation() {
        String[] navigationListCodes = WizFlow.getAppContext().getResources().getStringArray(developer.elkane.udacity.wizflow.R.array.navigation_list_codes);
        String navigation = getNavigationText();

        if (navigationListCodes[NOTES].equals(navigation)) {
            return NOTES;
        } else if (navigationListCodes[ARCHIVE].equals(navigation)) {
            return ARCHIVE;
        } else if (navigationListCodes[REMINDERS].equals(navigation)) {
            return REMINDERS;
        } else if (navigationListCodes[TRASH].equals(navigation)) {
            return TRASH;
        } else if (navigationListCodes[UNCATEGORIZED].equals(navigation)) {
            return UNCATEGORIZED;
        } else {
            return CATEGORY;
        }
    }


    public static String getNavigationText() {
        Context mContext = WizFlow.getAppContext();
        String[] navigationListCodes = mContext.getResources().getStringArray(developer.elkane.udacity.wizflow.R.array.navigation_list_codes);
        @SuppressWarnings("static-access")
        String navigation = mContext.getSharedPreferences(Constants.PREFS_NAME,
                mContext.MODE_MULTI_PROCESS).getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
        return navigation;
    }


    public static Long getCategory() {
        if (getNavigation() == CATEGORY) {
            return Long.valueOf(WizFlow.getAppContext().getSharedPreferences(Constants.PREFS_NAME, Context
                    .MODE_MULTI_PROCESS).getString(Constants.PREF_NAVIGATION, ""));
        } else {
            return null;
        }
    }


    public static boolean checkNavigation(int navigationToCheck) {
        return checkNavigation(new Integer[]{navigationToCheck});
    }


    public static boolean checkNavigation(Integer[] navigationsToCheck) {
        boolean res = false;
        int navigation = getNavigation();
        for (int navigationToCheck : new ArrayList<>(Arrays.asList(navigationsToCheck))) {
            if (navigation == navigationToCheck) {
                res = true;
                break;
            }
        }
        return res;
    }


    public static boolean checkNavigationCategory(Category categoryToCheck) {
        Context mContext = WizFlow.getAppContext();
        String[] navigationListCodes = mContext.getResources().getStringArray(developer.elkane.udacity.wizflow.R.array.navigation_list_codes);
        String navigation = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS).getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
        return (categoryToCheck != null && navigation.equals(String.valueOf(categoryToCheck.getId())));
    }

}
