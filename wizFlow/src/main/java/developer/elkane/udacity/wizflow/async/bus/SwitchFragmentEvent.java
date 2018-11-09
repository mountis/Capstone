
package developer.elkane.udacity.wizflow.async.bus;

import android.util.Log;

import developer.elkane.udacity.wizflow.utils.Constants;


public class SwitchFragmentEvent {

    public Direction direction;


    public SwitchFragmentEvent(Direction direction) {
        Log.d(Constants.TAG, this.getClass().getName());
        this.direction = direction;
    }


    public enum Direction {
        CHILDREN, PARENT
    }
}
