
package developer.elkane.udacity.wizflow;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;


public class ResourceAwareTest {

    private static final String MODULE_NAME = "wizflow";
    private static final String BASE_PATH = resolveBasePath(MODULE_NAME);


    private static String resolveBasePath(String moduleName) {
        final String path = "./" + moduleName + "/src/test/res/";
        if (Arrays.asList(new File("./").list()).contains(moduleName)) {
            return path;
        }
        return "../" + path;
    }


    public static FileInputStream getResourceAsStream(@NonNull final String path) throws IOException {
        return new FileInputStream(BASE_PATH + path);
    }


    public static File getResourceAsFile(@NonNull final String path) {
        return new File(BASE_PATH + path);
    }


    public static String readFile(@NonNull final String path) throws IOException {
        final StringBuilder sb = new StringBuilder();
        String strLine;
        try (final BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(BASE_PATH + path), "UTF-8"))) {
            while ((strLine = reader.readLine()) != null) {
                sb.append(strLine);
            }
        }
        return sb.toString();
    }
}
