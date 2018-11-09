
package developer.elkane.udacity.wizflow.models.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.models.NavigationItem;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Fonts;

public class NavDrawerAdapter extends BaseAdapter {

    private Activity mActivity;
    private List<NavigationItem> items = new ArrayList<>();
    private LayoutInflater inflater;


    public NavDrawerAdapter(Activity mActivity, List<NavigationItem> items) {
        this.mActivity = mActivity;
        this.items = items;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return items.size();
    }


    @Override
    public Object getItem(int position) {
        return items.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        NoteDrawerAdapterViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(developer.elkane.udacity.wizflow.R.layout.drawer_list_item, parent, false);

            Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_MULTI_PROCESS), convertView);

            holder = new NoteDrawerAdapterViewHolder();

            holder.imgIcon = (ImageView) convertView.findViewById(developer.elkane.udacity.wizflow.R.id.icon);
            holder.txtTitle = (TextView) convertView.findViewById(developer.elkane.udacity.wizflow.R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (NoteDrawerAdapterViewHolder) convertView.getTag();
        }

        holder.txtTitle.setText(items.get(position).getText());

        if (isSelected(position)) {
            holder.imgIcon.setImageResource(items.get(position).getIconSelected());
            holder.txtTitle.setTypeface(null, Typeface.BOLD);
            int color = mActivity.getResources().getColor(developer.elkane.udacity.wizflow.R.color.colorPrimaryDark);
            holder.txtTitle.setTextColor(color);
            holder.imgIcon.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.imgIcon.setImageResource(items.get(position).getIcon());
            holder.txtTitle.setTypeface(null, Typeface.NORMAL);
            holder.txtTitle.setTextColor(mActivity.getResources().getColor(developer.elkane.udacity.wizflow.R.color.drawer_text));
        }

        return convertView;
    }


    private boolean isSelected(int position) {

        String[] navigationListCodes = mActivity.getResources().getStringArray(developer.elkane.udacity.wizflow.R.array.navigation_list_codes);

        String navigationTmp = MainActivity.class.isAssignableFrom(mActivity.getClass()) ? ((MainActivity) mActivity)
                .getNavigationTmp() : null;

        String navigation = navigationTmp != null ? navigationTmp
                : mActivity.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_MULTI_PROCESS)
                .getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);

        int index = Arrays.asList(navigationListCodes).indexOf(navigation);

        if (index == -1)
            return false;

        String navigationLocalized = mActivity.getResources().getStringArray(developer.elkane.udacity.wizflow.R.array.navigation_list)[index];
        return navigationLocalized.equals(items.get(position).getText());
    }

}


class NoteDrawerAdapterViewHolder {

    ImageView imgIcon;
    TextView txtTitle;
}
