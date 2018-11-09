
package developer.elkane.udacity.wizflow.utils.date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment {

    public static final String DEFAULT_DATE = "default_date";

    private OnDateSetListener mListener;
    private Long defaultDate;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments().containsKey(DEFAULT_DATE)) {
            this.defaultDate = getArguments().getLong(DEFAULT_DATE);
        }

        try {
            mListener = (OnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDateSetListener");
        }
    }


    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar cal = DateUtils.getCalendar(defaultDate);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);


        final DatePickerDialog picker = new DatePickerDialog(getActivity(), DatePickerDialog.THEME_HOLO_LIGHT,
                mListener, y, m, d);
        picker.setTitle("");

        picker.setButton(DialogInterface.BUTTON_POSITIVE,
                getActivity().getString(android.R.string.ok),
                (dialog, which) -> {
                    DatePicker dp = picker.getDatePicker();
                    mListener.onDateSet(dp,
                            dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                });
        picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                getActivity().getString(android.R.string.cancel),
                (dialog, which) -> {
                });

        return picker;
    }

}
