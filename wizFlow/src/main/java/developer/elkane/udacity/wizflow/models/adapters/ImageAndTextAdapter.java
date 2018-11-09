
package developer.elkane.udacity.wizflow.models.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

import java.util.ArrayList;

import developer.elkane.udacity.wizflow.models.holders.ImageAndTextItem;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Fonts;


public class ImageAndTextAdapter extends BaseAdapter {

    ArrayList<ImageAndTextItem> items;
    private Activity mActivity;
    private LayoutInflater inflater;


    public ImageAndTextAdapter(Activity mActivity,
                               ArrayList<ImageAndTextItem> items) {
        this.mActivity = mActivity;
        this.items = items;
        inflater = (LayoutInflater) mActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        ImageAndTextViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(developer.elkane.udacity.wizflow.R.layout.image_and_text_item,
                    parent, false);

            Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS),
                    convertView);

            holder = new ImageAndTextViewHolder();

            holder.image = (ImageView) convertView.findViewById(developer.elkane.udacity.wizflow.R.id.image);
            holder.text = (TextView) convertView.findViewById(developer.elkane.udacity.wizflow.R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ImageAndTextViewHolder) convertView.getTag();
        }

        holder.text.setText(items.get(position).getText());

        if (items.get(position).getImage() != 0) {
            holder.image.setImageResource(items.get(position).getImage());
        }

        return convertView;
    }

}


class ImageAndTextViewHolder {

    ImageView image;
    TextView text;
}
