
package developer.elkane.udacity.wizflow.utils;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;


@RunWith(AndroidJUnit4.class)
public class SecurityTest {

    private final String PASS = "12345Passwords";
    private final String TEXT = "Today is a - good - day to test things!";
    private final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, " +
            "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    @Test
    public void testEncrypt() {
        String encryptedText = Security.encrypt(TEXT, PASS);
        assertFalse(TEXT.equals(encryptedText));
    }

    @Test
    public void testDecrypt() {
        String encryptedText = Security.encrypt(TEXT, PASS);
        assertEquals(TEXT, Security.decrypt(encryptedText, PASS));
        assertFalse(TEXT.equals(Security.decrypt(encryptedText, "zaza" + PASS)));
    }

    @Test
    public void testDecryptUnencrypted() {
        String result = Security.decrypt(LOREM, PASS);
        assertFalse(result.length() == 0);
    }
}
