
package developer.elkane.udacity.wizflow.utils;

import android.support.v4.util.Pair;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.models.Tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TagsHelperTest {

    private static String TAG1 = "#mixed";
    private static String TAG2 = "#123numbered";
    private static String TAG3 = "#tags";

    private Note note;


    @Before
    public void setup() {
        note = new Note();
        note.setContent("Random content with " + TAG1 + " " + TAG2 + " " + TAG3);
    }


    @Test
    public void retrievesTagsFromNote() {
        HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);
        assertEquals(tags.size(), 3);
        assertTrue(tags.containsKey(TAG1));
        assertTrue(tags.containsKey(TAG2));
        assertTrue(tags.containsKey(TAG3));
        assertFalse(tags.containsKey("#nonExistingTag"));
    }


    @Test
    public void retrievesTagsFromNoteMultilanguage() {
        note.setContent("#привет");
        HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);
        assertTrue(tags.containsKey("#привет"));

        note.setContent("#中华人民共和国");
        tags = TagsHelper.retrieveTags(note);
        assertTrue(tags.containsKey("#中华人民共和国"));
    }


    @Test
    public void removesTagsFromNote() {
        Pair<String, String> pair = TagsHelper.removeTag(note.getTitle(), note.getContent(), java.util.Collections
                .singletonList(new Tag(TAG2, 4)));
        note.setTitle(pair.first);
        note.setContent(pair.second);
        HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);
        assertTrue(tags.containsKey(TAG1));
        assertFalse(tags.containsKey(TAG2));
        assertTrue(tags.containsKey(TAG3));
    }


    @Test
    public void addsTagsToNote() {
        String newTag = "#addedTag";
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag(newTag, 1));
        tags.add(new Tag(TAG3, 1));
        Pair<String, List<Tag>> newTags = TagsHelper.addTagToNote(tags, new Integer[]{0, 1}, note);
        assertTrue(newTags.first.contains(newTag));
        assertFalse(newTags.first.contains(TAG3));
    }
}
