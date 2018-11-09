

package developer.elkane.udacity.wizflow.helpers;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import developer.elkane.udacity.wizflow.models.Note;
import developer.elkane.udacity.wizflow.utils.Constants;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class NotesHelperTest {

    @Test
    public void haveSameIdShouldFail() {
        Note note1 = getNote(1L, "test title", "test content");
        Note note2 = getNote(2L, "test title", "test content");
        assertFalse(NotesHelper.haveSameId(note1, note2));
    }

    @Test
    public void haveSameIdShouldSucceed() {
        Note note1 = getNote(3L, "test title", "test content");
        Note note2 = getNote(3L, "different test title", "different test content");
        assertTrue(NotesHelper.haveSameId(note1, note2));
    }

    @Test
    public void mergingNotesDoesntDuplicateFirstTitle() {
        final String FIRST_NOTE_TITLE = "test title 1";
        Note note1 = getNote(4L, FIRST_NOTE_TITLE, "");
        Note note2 = getNote(5L, "test title 2", "");
        Note mergedNote = NotesHelper.mergeNotes(Arrays.asList(note1, note2), false);
        assertFalse(mergedNote.getContent().contains(FIRST_NOTE_TITLE));
    }

    @Test
    public void mergeNotes() {
        int notesNumber = 3;
        List<Note> notes = new ArrayList<>();
        for (int i = 0; i < notesNumber; i++) {
            Note note = new Note();
            note.setTitle("Merged note " + i + " title");
            note.setContent("Merged note " + i + " content");
            notes.add(note);
        }
        Note mergeNote = NotesHelper.mergeNotes(notes, false);

        assertNotNull(mergeNote);
        Assert.assertTrue(mergeNote.getTitle().equals("Merged note 0 title"));
        Assert.assertTrue(mergeNote.getContent().contains("Merged note 0 content"));
        Assert.assertTrue(mergeNote.getContent().contains("Merged note 1 content"));
        Assert.assertTrue(mergeNote.getContent().contains("Merged note 2 content"));
        assertEquals(StringUtils.countMatches(mergeNote.getContent(), Constants.MERGED_NOTES_SEPARATOR), 2);
    }

    @Test
    public void getChars() {
        Note note = getNote(1L, "one two", "three four five\nAnother line");
        assertEquals(35, NotesHelper.getChars(note));
    }

    @Test
    public void getChecklistChars() {
        String content = it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM + "done\n" + it.feio.android
                .checklistview.interfaces.Constants.UNCHECKED_SYM + "undone yet";
        Note note = getNote(1L, "checklist", content);
        note.setChecklist(true);
        assertEquals(24, NotesHelper.getChars(note));
    }

    @Test
    public void getWords() {
        Note note = getNote(1L, "one two", "three four five");
        assertEquals(5, NotesHelper.getWords(note));
        note.setTitle("singleword");
        assertEquals(4, NotesHelper.getWords(note));
    }

    @Test
    public void getChecklistWords() {
        String content = it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM + "done\n" + it.feio.android
                .checklistview.interfaces.Constants.UNCHECKED_SYM + "undone yet";
        Note note = getNote(1L, "checklist", content);
        note.setChecklist(true);
        assertEquals(4, NotesHelper.getWords(note));
    }

    private Note getNote(Long id, String title, String content) {
        Note note = new Note();
        note.set_id(id);
        note.setTitle(title);
        note.setContent(content);
        return note;
    }
}
