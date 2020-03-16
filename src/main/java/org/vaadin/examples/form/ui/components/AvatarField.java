package org.vaadin.examples.form.ui.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.vaadin.examples.form.data.AvatarImage;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;

/**
 * A custom Vaadin component that allows users to upload an avatar image.
 * <p>
 * Can be used with the {@link Binder}. Note the type below; this Component can
 * only modify {@link AvatarImage} data.
 */
public class AvatarField extends CustomField<AvatarImage> {

    /**
     * We store any value here.
     */
    private AvatarImage value;

    /**
     * This is where any upload content will be written to
     */
    private ByteArrayOutputStream outputStream;

    private Image currentAvatar;
    private Upload upload;

    public AvatarField(String caption) {
        this();
        setLabel(caption);
    }

    public AvatarField() {

        // shows the current avatar
        currentAvatar = new Image();
        currentAvatar.setAlt("avatar image");
        currentAvatar.setMaxHeight("100px");
        currentAvatar.getStyle().set("margin-right", "15px");
        currentAvatar.setVisible(false); // see updateImage()

        // Create the upload component and delegate actions to the receiveUpload method
        upload = new Upload(this::receiveUpload);
        upload.getStyle().set("flex-grow", "1");

        // listen to state changes
        upload.addSucceededListener(e -> uploadSuccess(e));

        upload.addFailedListener(e -> setFailed(e.getReason().getMessage()));
        upload.addFileRejectedListener(e -> setFailed(e.getErrorMessage()));

        // Only allow images to be uploaded
        upload.setAcceptedFileTypes("image/*");

        // only allow single file at a time
        upload.setMaxFiles(1);

        // set max file size to 1 MB
        upload.setMaxFileSize(1 * 1024 * 1024);

        Div wrapper = new Div();
        wrapper.add(currentAvatar, upload);
        wrapper.getStyle().set("display", "flex");
        add(wrapper);
    }

    private void setFailed(String message) {
        setInvalid(true);
        setErrorMessage(message);
    }

    private void uploadSuccess(SucceededEvent e) {

        value.setImage(outputStream.toByteArray());

        // fire value changes
        setModelValue(value, true);

        updateImage();

        // clear the upload component 'finished files' list for a cleaner appearance
        // there is yet no API for it on the server side, see
        // https://github.com/vaadin/vaadin-upload-flow/issues/96
        upload.getElement().executeJs("this.files=[]");
    }

    /**
     * Update avatar content or hide if empty
     */
    private void updateImage() {
        if (value != null && value.getImage() != null) {
            currentAvatar.setSrc(new StreamResource("avatar", () -> new ByteArrayInputStream(value.getImage())));
            currentAvatar.setVisible(true);
        } else {
            currentAvatar.setSrc("");
            currentAvatar.setVisible(false);
        }
    }

    @Override
    protected AvatarImage generateModelValue() {
        return value;
    }

    @Override
    protected void setPresentationValue(AvatarImage newPresentationValue) {

        // Called when something external calls setValue() on this component. Store
        // value and refresh.
        value = newPresentationValue;
        updateImage();
    }

    /**
     * Called when a user initializes an upload
     */
    private OutputStream receiveUpload(String fileName, String mimeType) {

        // clear errors
        setInvalid(false);

        // create new value bean
        value = new AvatarImage();
        value.setName(fileName);
        value.setMime(mimeType);

        // set up receiving Stream
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

}
