
package developer.elkane.udacity.wizflow.utils;

import android.content.Context;


public class ResourcesUtils {

    public static int getXmlId(Context context, ResourceIdentifiers resourceIdentifier, String resourceName) {
        return context.getResources().getIdentifier(resourceName, resourceIdentifier.name(), context.getPackageName());
    }


    public enum ResourceIdentifiers {xml, id, array}
}
