
package developer.elkane.udacity.wizflow.models.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import developer.elkane.udacity.wizflow.helpers.date.DateHelper;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.views.ExpandableHeightGridView;
import developer.elkane.udacity.wizflow.models.views.SquareImageView;
import developer.elkane.udacity.wizflow.utils.BitmapHelper;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Fonts;
import developer.elkane.udacity.wizflow.utils.date.DateUtils;


public class AttachmentAdapter extends BaseAdapter {

    private Activity mActivity;
    private List<Attachment> attachmentsList;
    private LayoutInflater inflater;


    public AttachmentAdapter(Activity mActivity, List<Attachment> attachmentsList, ExpandableHeightGridView mGridView) {
        this.mActivity = mActivity;
        if (attachmentsList == null) {
            attachmentsList = Collections.emptyList();
        }
        this.attachmentsList = attachmentsList;
        this.inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public int getCount() {
        return attachmentsList.size();
    }


    public Attachment getItem(int position) {
        return attachmentsList.get(position);
    }


    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        Log.v(Constants.TAG, "GridView called for position " + position);

        Attachment mAttachment = attachmentsList.get(position);

        AttachmentHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(developer.elkane.udacity.wizflow.R.layout.gridview_item, parent, false);

            Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_MULTI_PROCESS), convertView);

            holder = new AttachmentHolder();
            holder.image = (SquareImageView) convertView.findViewById(developer.elkane.udacity.wizflow.R.id.gridview_item_picture);
            holder.text = (TextView) convertView.findViewById(developer.elkane.udacity.wizflow.R.id.gridview_item_text);
            convertView.setTag(holder);
        } else {
            holder = (AttachmentHolder) convertView.getTag();
        }

        if (mAttachment.getMime_type() != null && mAttachment.getMime_type().equals(Constants.MIME_TYPE_AUDIO)) {
            String text;

            if (mAttachment.getLength() > 0) {
                text = DateHelper.formatShortTime(mActivity, mAttachment.getLength());
            } else {
                text = DateUtils.getLocalizedDateTime(mActivity, mAttachment
                                .getUri().getLastPathSegment().split("\\.")[0],
                        Constants.DATE_FORMAT_SORTABLE);
            }

            if (text == null) {
                text = mActivity.getString(developer.elkane.udacity.wizflow.R.string.attachment);
            }
            holder.text.setText(text);
            holder.text.setVisibility(View.VISIBLE);
        } else {
            holder.text.setVisibility(View.GONE);
        }

        if (mAttachment.getMime_type() != null && mAttachment.getMime_type().equals(Constants.MIME_TYPE_FILES)) {
            holder.text.setText(mAttachment.getName());
            holder.text.setVisibility(View.VISIBLE);
        }

        Uri thumbnailUri = BitmapHelper.getThumbnailUri(mActivity, mAttachment);
        Glide.with(mActivity.getApplicationContext())
                .load(thumbnailUri)
                .centerCrop()
                .crossFade()
                .into(holder.image);

        return convertView;
    }


    public List<Attachment> getAttachmentsList() {
        return this.attachmentsList;
    }


    public class AttachmentHolder {

        TextView text;
        SquareImageView image;
    }


}
