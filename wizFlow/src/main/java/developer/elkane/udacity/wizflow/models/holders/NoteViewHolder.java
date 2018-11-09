

package developer.elkane.udacity.wizflow.models.holders;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.elkane.udacity.wizflow.models.views.SquareImageView;


public class NoteViewHolder {

    @BindView(developer.elkane.udacity.wizflow.R.id.root)
    public View root;
    @BindView(developer.elkane.udacity.wizflow.R.id.card_layout)
    public View cardLayout;
    @BindView(developer.elkane.udacity.wizflow.R.id.category_marker)
    public View categoryMarker;
    @BindView(developer.elkane.udacity.wizflow.R.id.note_title)
    public TextView title;
    @BindView(developer.elkane.udacity.wizflow.R.id.note_content)
    public TextView content;
    @BindView(developer.elkane.udacity.wizflow.R.id.note_date)
    public TextView date;
    @BindView(developer.elkane.udacity.wizflow.R.id.archivedIcon)
    public ImageView archiveIcon;
    @BindView(developer.elkane.udacity.wizflow.R.id.locationIcon)
    public ImageView locationIcon;
    @BindView(developer.elkane.udacity.wizflow.R.id.alarmIcon)
    public ImageView alarmIcon;
    @BindView(developer.elkane.udacity.wizflow.R.id.lockedIcon)
    public ImageView lockedIcon;
    @Nullable
    @BindView(developer.elkane.udacity.wizflow.R.id.attachmentIcon)
    public ImageView attachmentIcon;
    @Nullable
    @BindView(developer.elkane.udacity.wizflow.R.id.attachmentThumbnail)
    public SquareImageView attachmentThumbnail;

    public NoteViewHolder(View view) {
        ButterKnife.bind(this, view);
    }
}
