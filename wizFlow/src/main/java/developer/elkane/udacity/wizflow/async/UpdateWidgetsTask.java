

package developer.elkane.udacity.wizflow.async;

import android.content.Context;
import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.BaseActivity;
import developer.elkane.udacity.wizflow.async.bus.NotesUpdatedEvent;

public class UpdateWidgetsTask extends AsyncTask<Void, Void, Void> {

    private Context context;

    public UpdateWidgetsTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        WidgetUpdateSubscriber widgetUpdateSubscriber = new WidgetUpdateSubscriber();
        return null;
    }

    class WidgetUpdateSubscriber {

        WidgetUpdateSubscriber() {
            EventBus.getDefault().register(this);
        }

        public void onEvent(NotesUpdatedEvent event) {
            BaseActivity.notifyAppWidgets(context);
            EventBus.getDefault().unregister(this);
        }
    }
}
