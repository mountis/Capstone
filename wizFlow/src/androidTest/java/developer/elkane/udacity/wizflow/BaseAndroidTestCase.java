
package developer.elkane.udacity.wizflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.utils.Constants;


public class BaseAndroidTestCase extends AndroidTestCase {

    private final static String DB_PATH_REGEX = ".*developer\\.elkane\\.udacity\\.android.*\\/databases\\/test_wizflow.*";
    private final static String DB_PREFIX = "test_";

    protected DbHelper dbHelper;
    protected Context testContext;
    protected SharedPreferences prefs;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), DB_PREFIX);
        dbHelper = DbHelper.getInstance(testContext);
        prefs = testContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        assertTrue("Database used for tests MUST not be the default one but prefixed by '" + DB_PREFIX + "'", dbHelper.getDatabase().getPath().matches(DB_PATH_REGEX));
        assertFalse("Database MUST be writable", dbHelper.getDatabase().isReadOnly());
//        cleanDatabase();
    }


    @Override
    protected void tearDown() throws Exception {
        testContext.deleteDatabase(DbHelper.getInstance().getDatabaseName());
        super.tearDown();
    }

    protected void cleanDatabase() {
        dbHelper.getDatabase(true).delete(DbHelper.TABLE_NOTES, null, null);
        dbHelper.getDatabase(true).delete(DbHelper.TABLE_CATEGORY, null, null);
        dbHelper.getDatabase(true).delete(DbHelper.TABLE_ATTACHMENTS, null, null);
    }

}
