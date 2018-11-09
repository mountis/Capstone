
package developer.elkane.udacity.wizflow.utils.date;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment {

    public static final String DEFAULT_TIME = "default_time";

    TextView timer_label;
    private Activity mActivity;
    private OnTimeSetListener mListener;
    private Long defaultTime = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        if (getArguments().containsKey(DEFAULT_TIME)) {
            this.defaultTime = getArguments().getLong(DEFAULT_TIME);
        }

        try {
            mListener = (OnTimeSetListener) mActivity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTimeSetListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar cal = DateUtils.getCalendar(defaultTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        boolean is24HourMode = DateUtils.is24HourMode(mActivity);
        TimePickerDialog tpd = new TimePickerDialog(mActivity, developer.elkane.udacity.wizflow.R.style.Theme_AppCompat_Dialog_NoBackgroundOrDim, mListener, hour, minute, is24HourMode);
        tpd.setTitle("");
        return tpd;
    }

}
