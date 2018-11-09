
package developer.elkane.udacity.wizflow.utils;

import android.content.Context;
import android.content.Intent;

import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.helpers.date.DateHelper;
import developer.elkane.udacity.wizflow.models.Note;


public class ShortcutHelper {


    public static void addShortcut(Context context, Note note) {
        Intent shortcutIntent = new Intent(context, MainActivity.class);
        shortcutIntent.putExtra(Constants.INTENT_KEY, note.get_id());
        shortcutIntent.setAction(Constants.ACTION_SHORTCUT);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        String shortcutTitle = note.getTitle().length() > 0 ? note.getTitle() : DateHelper.getFormattedDate(note
                .getCreation(), WizFlow.getSharedPreferences().getBoolean(Constants
                .PREF_PRETTIFIED_DATES, true));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, developer.elkane.udacity.wizflow.R.drawable.ic_shortcut));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(addIntent);
    }


    public static void removeshortCut(Context context, Note note) {
        Intent shortcutIntent = new Intent(context, MainActivity.class);
        shortcutIntent.putExtra(Constants.INTENT_KEY, note.get_id());
        shortcutIntent.setAction(Constants.ACTION_SHORTCUT);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        String shortcutTitle = note.getTitle().length() > 0 ? note.getTitle() : DateHelper.getFormattedDate(note
                .getCreation(), WizFlow.getSharedPreferences().getBoolean(Constants
                .PREF_PRETTIFIED_DATES, true));

        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);

        addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }
}
