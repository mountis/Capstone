

package developer.elkane.udacity.wizflow.widget;


import android.app.PendingIntent;
import android.content.Context;
import android.util.SparseArray;
import android.widget.RemoteViews;


public class SimpleWidgetProvider extends WidgetProvider {


    @Override
    protected RemoteViews getRemoteViews(Context mContext, int widgetId, boolean isSmall, boolean isSingleLine,
                                         SparseArray<PendingIntent> pendingIntentsMap) {
        RemoteViews views;
        if (isSmall) {
            views = new RemoteViews(mContext.getPackageName(), developer.elkane.udacity.wizflow.R.layout.widget_layout_small);
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.list, pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.list));
        } else {
            views = new RemoteViews(mContext.getPackageName(), developer.elkane.udacity.wizflow.R.layout.widget_layout);
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.add, pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.add));
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.list, pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.list));
            views.setOnClickPendingIntent(developer.elkane.udacity.wizflow.R.id.camera, pendingIntentsMap.get(developer.elkane.udacity.wizflow.R.id.camera));
        }
        return views;
    }
}
