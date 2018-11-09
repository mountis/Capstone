
package developer.elkane.udacity.wizflow.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.utils.Constants;


public abstract class WidgetProvider extends AppWidgetProvider {

    public static final String EXTRA_WORD = "developer.elkane.udacity.wizflow.widget.WORD";
    public static final String TOAST_ACTION = "developer.elkane.udacity.wizflow.widget.NOTE";
    public static final String EXTRA_ITEM = "developer.elkane.udacity.wizflow.widget.EXTRA_FIELD";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, getClass());
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int appWidgetId : allWidgetIds) {
            Log.d(Constants.TAG, "WidgetProvider onUpdate() widget " + appWidgetId);
            setLayout(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {
        Log.d(Constants.TAG, "Widget size changed");
        setLayout(context, appWidgetManager, appWidgetId);
    }


    private void setLayout(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        Intent intentDetail = new Intent(context, MainActivity.class);
        intentDetail.setAction(Constants.ACTION_WIDGET);
        intentDetail.putExtra(Constants.INTENT_WIDGET, widgetId);
        PendingIntent pendingIntentDetail = PendingIntent.getActivity(context, widgetId, intentDetail,
                Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intentList = new Intent(context, MainActivity.class);
        intentList.setAction(Constants.ACTION_WIDGET_SHOW_LIST);
        intentList.putExtra(Constants.INTENT_WIDGET, widgetId);
        PendingIntent pendingIntentList = PendingIntent.getActivity(context, widgetId, intentList,
                Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intentDetailPhoto = new Intent(context, MainActivity.class);
        intentDetailPhoto.setAction(Constants.ACTION_WIDGET_TAKE_PHOTO);
        intentDetailPhoto.putExtra(Constants.INTENT_WIDGET, widgetId);
        PendingIntent pendingIntentDetailPhoto = PendingIntent.getActivity(context, widgetId, intentDetailPhoto,
                Intent.FLAG_ACTIVITY_NEW_TASK);

        boolean isSmall = false;
        boolean isSingleLine = true;
        Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
        isSmall = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 110;
        isSingleLine = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) < 110;

        SparseArray<PendingIntent> map = new SparseArray<>();
        map.put(developer.elkane.udacity.wizflow.R.id.list, pendingIntentList);
        map.put(developer.elkane.udacity.wizflow.R.id.add, pendingIntentDetail);
        map.put(developer.elkane.udacity.wizflow.R.id.camera, pendingIntentDetailPhoto);

        RemoteViews views = getRemoteViews(context, widgetId, isSmall, isSingleLine, map);

        appWidgetManager.updateAppWidget(widgetId, views);
    }


    abstract protected RemoteViews getRemoteViews(Context context, int widgetId, boolean isSmall, boolean isSingleLine, SparseArray<PendingIntent> pendingIntentsMap);

}
