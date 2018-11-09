
package developer.elkane.udacity.wizflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import developer.elkane.udacity.wizflow.utils.Fonts;
import it.feio.android.checklistview.utils.DensityUtil;

public class FontSizeListPreference extends ListPreference {

    private int clickedDialogEntryIndex;


    public FontSizeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summary = (TextView) view.findViewById(android.R.id.summary);
        Fonts.overrideTextSize(getContext(), getSharedPreferences(), summary);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(),
                R.layout.settings_font_size_dialog_item, getEntries()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) convertView;
                if (view == null) {
                    view = (CheckedTextView) View.inflate(getContext(),
                            R.layout.settings_font_size_dialog_item, null);
                }
                view.setText(getEntries()[position]);
                Context privateContext = getContext().getApplicationContext();
                float currentSize = DensityUtil.pxToDp(((TextView) View.inflate(getContext(),
                        R.layout.settings_font_size_dialog_item, null)).getTextSize(), privateContext);
                float offset = privateContext.getResources().getIntArray(
                        R.array.text_size_offset)[position];
                view.setTextSize(currentSize + offset);
                return view;
            }
        };
        clickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setSingleChoiceItems(adapter, clickedDialogEntryIndex,
                (dialog, which) -> {
                    clickedDialogEntryIndex = which;
                    FontSizeListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    dialog.dismiss();
                });
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && clickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String val = getEntryValues()[clickedDialogEntryIndex].toString();
            if (callChangeListener(val)) {
                setValue(val);
            }
        }
    }
}