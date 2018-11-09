

package developer.elkane.udacity.wizflow.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;


public class ListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplication(), intent);
    }
}
