

package developer.elkane.udacity.wizflow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.helpers.date.DateHelper;
import developer.elkane.udacity.wizflow.models.Note;


public class TextHelper {


    public static Spanned[] parseTitleAndContent(Context mContext, Note note) {

        final int CONTENT_SUBSTRING_LENGTH = 300;

        String titleText = note.getTitle();
        String contentText = limit(note.getContent().trim(), CONTENT_SUBSTRING_LENGTH, false, true);

        if (note.isLocked()
                && !mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS).getBoolean(
                "settings_password_access", false)) {
            if (!note.getTitle().equals(titleText) && titleText.length() > 3) {
                titleText = limit(titleText, 4, false, false);
            }
            contentText = "";
        }

        Spanned contentSpanned;
        if (note.isChecklist() && !TextUtils.isEmpty(contentText)) {
            contentSpanned = Html.fromHtml(contentText
                    .replace(it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM,
                            it.feio.android.checklistview.interfaces.Constants.CHECKED_ENTITY)
                    .replace(it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM,
                            it.feio.android.checklistview.interfaces.Constants.UNCHECKED_ENTITY)
                    .replace(System.getProperty("line.separator"), "<br/>"));
        } else {
            contentSpanned = new SpannedString(contentText);
        }

        return new Spanned[]{new SpannedString(titleText), contentSpanned};
    }


    private static String limit(String value, int length, boolean singleLine, boolean elipsize) {
        StringBuilder buf = new StringBuilder(value);
        int indexNewLine = buf.indexOf(System.getProperty("line.separator"));
        int endIndex = singleLine && indexNewLine < length ? indexNewLine : length < buf.length() ? length : -1;
        if (endIndex != -1) {
            buf.setLength(endIndex);
            if (elipsize) {
                buf.append("...");
            }
        }
        return buf.toString();
    }


    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase(Locale.getDefault()) + string.substring(1,
                string.length()).toLowerCase(Locale.getDefault());
    }


    public static String checkIntentCategory(String sqlCondition) {
        String pattern = DbHelper.KEY_CATEGORY + "\\s*=\\s*([\\d]+)";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(sqlCondition);
        if (matcher.find() && matcher.group(1) != null) {
            return matcher.group(1).trim();
        }
        return null;
    }


    public static String getDateText(Context mContext, Note note, int navigation) {
        String dateText;
        String sort_column;
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);

        if (Navigation.REMINDERS == navigation) {
            sort_column = DbHelper.KEY_REMINDER;
        } else {
            sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, "");
        }

        switch (sort_column) {
            case DbHelper.KEY_CREATION:
                dateText = mContext.getString(developer.elkane.udacity.wizflow.R.string.creation) + " " + DateHelper.getFormattedDate(note.getCreation
                        (), prefs.getBoolean(Constants.PREF_PRETTIFIED_DATES, true));
                break;
            case DbHelper.KEY_REMINDER:
                String noteReminder = note.getAlarm();
                if (TextUtils.isEmpty(noteReminder)) {
                    dateText = mContext.getString(developer.elkane.udacity.wizflow.R.string.no_reminder_set);
                } else {
                    dateText = mContext.getString(developer.elkane.udacity.wizflow.R.string.alarm_set_on) + " " + DateHelper.getDateTimeShort(mContext,
                            Long.parseLong(noteReminder));
                }
                break;
            default:
                dateText = mContext.getString(developer.elkane.udacity.wizflow.R.string.last_update) + " " + DateHelper.getFormattedDate(note
                        .getLastModification(), prefs.getBoolean(Constants.PREF_PRETTIFIED_DATES, true));
                break;
        }
        return dateText;
    }


    public static String getAlternativeTitle(Context context, Note note, Spanned spanned) {
        if (spanned.length() > 0) {
            return spanned.toString();
        }
        return context.getString(developer.elkane.udacity.wizflow.R.string.note) + " " + context.getString(developer.elkane.udacity.wizflow.R.string.creation) + " " + DateHelper
                .getDateTimeShort(context, note.getCreation());
    }

}
