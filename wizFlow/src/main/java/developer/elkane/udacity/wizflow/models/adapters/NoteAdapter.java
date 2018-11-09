
package developer.elkane.udacity.wizflow.models.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.nhaarman.listviewanimations.util.Insertable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import developer.elkane.udacity.wizflow.async.TextWorkerTask;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.holders.NoteViewHolder;
import developer.elkane.udacity.wizflow.utils.BitmapHelper;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Fonts;
import developer.elkane.udacity.wizflow.utils.Navigation;
import developer.elkane.udacity.wizflow.utils.TextHelper;


public class NoteAdapter extends ArrayAdapter<Note> implements Insertable {

    private final Activity mActivity;
    private final int navigation;
    private List<Note> notes = new ArrayList<>();
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean expandedView;
    private int layout;
    private LayoutInflater inflater;
    private long closestNoteReminder = Long.parseLong(Constants.TIMESTAMP_UNIX_EPOCH_FAR);
    private int closestNotePosition;


    public NoteAdapter(Activity activity, int layout, List<Note> notes) {
        super(activity, developer.elkane.udacity.wizflow.R.layout.note_layout_expanded, notes);
        this.mActivity = activity;
        this.notes = notes;
        this.layout = layout;

        expandedView = layout == developer.elkane.udacity.wizflow.R.layout.note_layout_expanded;
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        navigation = Navigation.getNavigation();
        manageCloserNote(notes, navigation);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = notes.get(position);
        NoteViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
            holder = buildHolder(convertView, parent);
            convertView.setTag(holder);
        } else {
            holder = (NoteViewHolder) convertView.getTag();
        }
        initText(note, holder);
        initIcons(note, holder);
        initDates(note, holder);
        initThumbnail(note, holder);
        manageSelectionColor(position, note, holder);
        return convertView;
    }


    private void manageSelectionColor(int position, Note note, NoteViewHolder holder) {
        if (selectedItems.get(position)) {
            holder.cardLayout.setBackgroundColor(mActivity.getResources().getColor(
                    developer.elkane.udacity.wizflow.R.color.list_bg_selected));
        } else {
            restoreDrawable(note, holder.cardLayout, holder);
        }
    }


    private void initThumbnail(Note note, NoteViewHolder holder) {
        if (expandedView) {
            if ((note.isLocked() && !mActivity.getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_MULTI_PROCESS).getBoolean("settings_password_access", false))
                    || note.getAttachmentsList().size() == 0) {
                holder.attachmentThumbnail.setVisibility(View.GONE);
            } else {
                holder.attachmentThumbnail.setVisibility(View.VISIBLE);
                Attachment mAttachment = note.getAttachmentsList().get(0);
                Uri thumbnailUri = BitmapHelper.getThumbnailUri(mActivity, mAttachment);
                Glide.with(mActivity)
                        .load(thumbnailUri)
                        .centerCrop()
                        .crossFade()
                        .into(holder.attachmentThumbnail);
            }
        }
    }


    public List<Note> getNotes() {
        return notes;
    }


    private void initDates(Note note, NoteViewHolder holder) {
        String dateText = TextHelper.getDateText(mActivity, note, navigation);
        holder.date.setText(dateText);
    }


    private void initIcons(Note note, NoteViewHolder holder) {
        holder.archiveIcon.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
        holder.locationIcon.setVisibility(note.getLongitude() != null && note.getLongitude() != 0 ? View.VISIBLE :
                View.GONE);

        holder.alarmIcon.setVisibility(note.getAlarm() != null ? View.VISIBLE : View.GONE);
        holder.lockedIcon.setVisibility(note.isLocked() ? View.VISIBLE : View.GONE);
        if (!expandedView) {
            holder.attachmentIcon.setVisibility(note.getAttachmentsList().size() > 0 ? View.VISIBLE : View.GONE);
        }
    }


    private void initText(Note note, NoteViewHolder holder) {
        try {
            if (note.isChecklist()) {
                TextWorkerTask task = new TextWorkerTask(mActivity, holder.title, holder.content, expandedView);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
            } else {
                Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mActivity, note);
                holder.title.setText(titleAndContent[0]);
                holder.content.setText(titleAndContent[1]);
                holder.title.setText(titleAndContent[0]);
                if (titleAndContent[1].length() > 0) {
                    holder.content.setText(titleAndContent[1]);
                    holder.content.setVisibility(View.VISIBLE);
                } else {
                    holder.content.setVisibility(View.INVISIBLE);
                }
            }
        } catch (RejectedExecutionException e) {
            Log.w(Constants.TAG, "Oversized tasks pool to load texts!", e);
        }
    }


    private void manageCloserNote(List<Note> notes, int navigation) {
        if (navigation == Navigation.REMINDERS) {
            for (int i = 0; i < notes.size(); i++) {
                long now = Calendar.getInstance().getTimeInMillis();
                long reminder = Long.parseLong(notes.get(i).getAlarm());
                if (now < reminder && reminder < closestNoteReminder) {
                    closestNotePosition = i;
                    closestNoteReminder = reminder;
                }
            }
        }

    }


    public int getClosestNotePosition() {
        return closestNotePosition;
    }


    public SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }


    public void addSelectedItem(Integer selectedItem) {
        this.selectedItems.put(selectedItem, true);
    }


    public void removeSelectedItem(Integer selectedItem) {
        this.selectedItems.delete(selectedItem);
    }


    public void clearSelectedItems() {
        this.selectedItems.clear();
    }


    public void restoreDrawable(Note note, View v) {
        restoreDrawable(note, v, null);
    }


    public void restoreDrawable(Note note, View v, NoteViewHolder holder) {
        final int paddingBottom = v.getPaddingBottom(), paddingLeft = v.getPaddingLeft();
        final int paddingRight = v.getPaddingRight(), paddingTop = v.getPaddingTop();
        v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        colorNote(note, v, holder);
    }


    @SuppressWarnings("unused")
    private void colorNote(Note note, View v) {
        colorNote(note, v, null);
    }


    private void colorNote(Note note, View v, NoteViewHolder holder) {

        String colorsPref = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS)
                .getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);

        if (!colorsPref.equals("disabled")) {

            v.setBackgroundColor(Color.parseColor("#00000000"));

            if (note.getCategory() != null && note.getCategory().getColor() != null) {
                if (colorsPref.equals("complete") || colorsPref.equals("list")) {
                    v.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
                } else {
                    if (holder != null) {
                        holder.categoryMarker.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
                    } else {
                        v.findViewById(developer.elkane.udacity.wizflow.R.id.category_marker).setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
                    }
                }
            } else {
                v.findViewById(developer.elkane.udacity.wizflow.R.id.category_marker).setBackgroundColor(0);
            }
        }
    }


    public void replace(Note note, int index) {
        if (notes.indexOf(note) != -1) {
            notes.remove(index);
        } else {
            index = notes.size();
        }
        notes.add(index, note);
    }


    @Override
    public void add(int i, @NonNull Object o) {
        insert((Note) o, i);
    }


    public void remove(List<Note> notes) {
        for (Note note : notes) {
            remove(note);
        }
    }


    private NoteViewHolder buildHolder(View convertView, ViewGroup parent) {
        Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_MULTI_PROCESS), convertView);
        return new NoteViewHolder(convertView);
    }

}



