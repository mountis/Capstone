

package developer.elkane.udacity.wizflow.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import developer.elkane.udacity.wizflow.BaseAndroidTestCase;
import developer.elkane.udacity.wizflow.R;
import developer.elkane.udacity.wizflow.utils.Constants;


public class LanguageHelperTest extends BaseAndroidTestCase {

    public void testShouldChangeSharedPrefrencesLanguage() {
        LanguageHelper.updateLanguage(testContext, Locale.ITALY.toString());
        SharedPreferences prefs = testContext.getSharedPreferences(Constants.PREFS_NAME, Context
                .MODE_MULTI_PROCESS);
        String language = prefs.getString(Constants.PREF_LANG, "");
        assertEquals(Locale.ITALY.toString(), language);
    }

    public void testShouldChangeAppLanguage() {
        LanguageHelper.updateLanguage(testContext, Locale.ITALY.toString());
        assertTranslationMatches(Locale.ITALY.toString(), R.string.add_note);
    }

    public void testSameStaticStringToEnsureTranslationsAreCorrect() {

        assertTranslationMatches("en_US", R.string.add_note, "Add Note");
        assertTranslationMatches("fr_FR", R.string.add_note, "Ajouter une note");
    }

    private void assertTranslationMatches(String locale, int resourceId) {
        assertTranslationMatches(locale, resourceId, testContext.getString(resourceId));
    }

    private void assertTranslationMatches(String locale, int resourceId, String string) {
        assertEquals(LanguageHelper.getLocalizedString(testContext, locale, resourceId), string);
    }
}
