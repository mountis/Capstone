

package developer.elkane.udacity.wizflow.db;


import developer.elkane.udacity.wizflow.BaseAndroidTestCase;
import developer.elkane.udacity.wizflow.models.Note;

public class DbHelperTest extends BaseAndroidTestCase {


    public void testGetNotesByTag() {
        Note note = new Note();
        note.setTitle("title with #tag inside");
        note.setContent("useless content");
        dbHelper.updateNote(note, true);
        Note note1 = new Note();
        note1.setTitle("simple title");
        note1.setContent("content with #tag");
        dbHelper.updateNote(note1, true);
        Note note2 = new Note();
        note2.setTitle("title without tags in it");
        note2.setContent("some \n #tagged content");
        dbHelper.updateNote(note2, true);
        assertEquals(2, dbHelper.getNotesByTag("#tag").size());
        assertEquals(1, dbHelper.getNotesByTag("#tagged").size());
    }
}
