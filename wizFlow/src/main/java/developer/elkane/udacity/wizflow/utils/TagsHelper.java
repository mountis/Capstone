

package developer.elkane.udacity.wizflow.utils;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.Tag;
import it.feio.android.pixlui.links.RegexPatternsConstants;


public class TagsHelper {


    public static List<Tag> getAllTags() {
        return DbHelper.getInstance().getTags();
    }


    public static HashMap<String, Integer> retrieveTags(Note note) {
        HashMap<String, Integer> tagsMap = new HashMap<>();
        for (String token : (note.getTitle() + " " + note.getContent()).replaceAll("\n", " ").trim().split(" ")) {
            if (RegexPatternsConstants.HASH_TAG.matcher(token).matches()) {
                int count = tagsMap.get(token) == null ? 0 : tagsMap.get(token);
                tagsMap.put(token, ++count);
            }
        }
        return tagsMap;
    }


    public static Pair<String, List<Tag>> addTagToNote(List<Tag> tags, Integer[] selectedTags, Note note) {
        StringBuilder sbTags = new StringBuilder();
        List<Tag> tagsToRemove = new ArrayList<>();
        HashMap<String, Integer> tagsMap = retrieveTags(note);

        List<Integer> selectedTagsList = Arrays.asList(selectedTags);
        for (int i = 0; i < tags.size(); i++) {
            if (mapContainsTag(tagsMap, tags.get(i))) {
                if (!selectedTagsList.contains(i)) {
                    tagsToRemove.add(tags.get(i));
                }
            } else {
                if (selectedTagsList.contains(i)) {
                    if (sbTags.length() > 0) {
                        sbTags.append(" ");
                    }
                    sbTags.append(tags.get(i));
                }
            }
        }
        return Pair.create(sbTags.toString(), tagsToRemove);
    }


    private static boolean mapContainsTag(HashMap<String, Integer> tagsMap, Tag tag) {
        for (String tagsMapItem : tagsMap.keySet()) {
            if (tagsMapItem.equals(tag.getText())) {
                return true;
            }
        }
        return false;
    }


    public static Pair<String, String> removeTag(String noteTitle, String noteContent, List<Tag> tagsToRemove) {
        String title = noteTitle, content = noteContent;
        for (Tag tagToRemove : tagsToRemove) {
            title = title.replaceAll(tagToRemove.getText(), "");
            content = content.replaceAll(tagToRemove.getText(), "");
        }
        return new Pair<>(title, content);
    }


    public static String[] getTagsArray(List<Tag> tags) {
        String[] tagsArray = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            tagsArray[i] = tags.get(i).getText().substring(1) + " (" + tags.get(i).getCount() + ")";
        }
        return tagsArray;
    }


    public static Integer[] getPreselectedTagsArray(Note note, List<Tag> tags) {
        List<Note> notes = new ArrayList<>();
        notes.add(note);
        return getPreselectedTagsArray(notes, tags);
    }


    public static Integer[] getPreselectedTagsArray(List<Note> notes, List<Tag> tags) {
        final Integer[] preSelectedTags;
        if (notes.size() == 1) {
            List<Integer> t = new ArrayList<>();
            for (String noteTag : TagsHelper.retrieveTags(notes.get(0)).keySet()) {
                for (Tag tag : tags) {
                    if (tag.getText().equals(noteTag)) {
                        t.add(tags.indexOf(tag));
                        break;
                    }
                }
            }
            preSelectedTags = t.toArray(new Integer[t.size()]);
        } else {
            preSelectedTags = new Integer[]{};
        }
        return preSelectedTags;
    }
}
