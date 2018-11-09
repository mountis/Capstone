

package developer.elkane.udacity.wizflow.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.RemoteViews;

import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.utils.Constants;


public class ListWidgetProvider extends WidgetProvider {

    @Override
    protected RemoteViews getRemoteViews(Context mContext, int widgetId,
                                         boolean isSmall, boolean isSingleLine,
                                         SparseArray<PendingIntent> pendingIntentsMap) {
        RemoteViews views;
        if (isSmall) {
            views = new RemoteViews(mContext.getPackageName(),
                    developer.elkane.udacity.wizflow.R.layout.widget_layout_small);
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.list,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.list));
        } else if (isSingleLine) {
            views = new RemoteViews(mContext.getPackageName(),
                    developer.elkane.udacity.wizflow.R.layout.widget_layout);
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.add,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.add));
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.list,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.list));
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.camera,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.camera));
        } else {
            views = new RemoteViews(mContext.getPackageName(),
                    developer.elkane.udacity.wizflow.R.layout.widget_layout_list);
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.add,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.add));
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.list,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.list));
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.camera,
                    pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.camera));

            Intent intent = new Intent(mContext, ListWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            views.setRemoteAdapter(developer.elkane.udacity.wizflow.R.id.widget_list, intent);

            Intent clickIntent = new Intent(mContext, MainActivity.class);
            clickIntent.setAction(Constants.ACTION_WIDGET);
            PendingIntent clickPI = PendingIntent.getActivity(mContext, 0,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(developer.elkane.udacity.wizflow.R.id.widget_list, clickPI);
        }
        return views;
    }

}
