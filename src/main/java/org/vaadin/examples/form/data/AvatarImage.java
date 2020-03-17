package org.vaadin.examples.form.data;

/**
 * Class for holding data for an attachment file in this case an Image.
 */
public class AvatarImage {

    private byte[] image;
    private String name;
    private String mime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }
}
