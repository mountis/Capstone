

package developer.elkane.udacity.wizflow.models.holders;

public class ImageAndTextItem {

    private int image;
    private String text;

    public ImageAndTextItem(int image, String text) {
        super();
        this.image = image;
        this.text = text;
    }

    public int getImage() {
        return image;
    }


    public void setImage(int image) {
        this.image = image;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }

}
