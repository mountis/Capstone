
package developer.elkane.udacity.wizflow.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;

import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.adapters.NavDrawerCategoryAdapter;
import developer.elkane.udacity.wizflow.utils.Constants;


public class WidgetConfigurationActivity extends Activity {

    private Spinner categorySpinner;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String sqlCondition;
    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(developer.elkane.udacity.wizflow.R.layout.activity_widget_configuration);

        mRadioGroup = (RadioGroup) findViewById(developer.elkane.udacity.wizflow.R.id.widget_config_radiogroup);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case developer.elkane.udacity.wizflow.R.id.widget_config_notes:
                    categorySpinner.setEnabled(false);
                    break;

                case developer.elkane.udacity.wizflow.R.id.widget_config_categories:
                    categorySpinner.setEnabled(true);
                    break;

                default:
                    Log.e(Constants.TAG, "Wrong element choosen: " + checkedId);
            }
        });

        categorySpinner = (Spinner) findViewById(developer.elkane.udacity.wizflow.R.id.widget_config_spinner);
        categorySpinner.setEnabled(false);
        DbHelper db = DbHelper.getInstance();
        ArrayList<Category> categories = db.getCategories();
        categorySpinner.setAdapter(new NavDrawerCategoryAdapter(this, categories));

        Button configOkButton = (Button) findViewById(developer.elkane.udacity.wizflow.R.id.widget_config_confirm);
        configOkButton.setOnClickListener(v -> {

            if (mRadioGroup.getCheckedRadioButtonId() == developer.elkane.udacity.wizflow.R.id.widget_config_notes) {
                sqlCondition = " WHERE " + DbHelper.KEY_ARCHIVED + " IS NOT 1 AND " + DbHelper.KEY_TRASHED + " IS" +
                        " NOT 1 ";

            } else {
                Category tag = (Category) categorySpinner.getSelectedItem();
                sqlCondition = " WHERE " + DbHelper.TABLE_NOTES + "."
                        + DbHelper.KEY_CATEGORY + " = " + tag.getId()
                        + " AND " + DbHelper.KEY_ARCHIVED + " IS NOT 1"
                        + " AND " + DbHelper.KEY_TRASHED + " IS NOT 1";
            }

            CheckBox showThumbnailsCheckBox = (CheckBox) findViewById(developer.elkane.udacity.wizflow.R.id.show_thumbnails);
            CheckBox showTimestampsCheckBox = (CheckBox) findViewById(developer.elkane.udacity.wizflow.R.id.show_timestamps);


            ListRemoteViewsFactory.updateConfiguration(getApplicationContext(), mAppWidgetId,
                    sqlCondition, showThumbnailsCheckBox.isChecked(), showTimestampsCheckBox.isChecked());

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            finish();
        });

        if (categories.size() == 0) {
            mRadioGroup.setVisibility(View.GONE);
            categorySpinner.setVisibility(View.GONE);
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

}
