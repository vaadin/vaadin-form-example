package org.vaadin.examples.form.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.examples.form.data.UserDetails;
import org.vaadin.examples.form.data.UserDetailsService;
import org.vaadin.examples.form.data.UserDetailsService.ServiceException;
import org.vaadin.examples.form.ui.components.AvatarField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.Route;

/**
 * This is the default (and only) view in this example.
 * <p>
 * It demonstrates how to create a form using Vaadin and the Binder. The backend
 * service and data class are in the <code>.data</code> package.
 */
@Route("")
public class MainView extends VerticalLayout {

    private Checkbox allowMarketingBox;
    private PasswordField passwordField1;
    private PasswordField passwordField2;

    private UserDetailsService service;
    private BeanValidationBinder<UserDetails> binder;

    /**
     * We use Spring to inject the backend into our view.
     */
    public MainView(@Autowired UserDetailsService service) {

        this.service = service;

        /*
         * Create the components we'll need
         */
        TextField firstnameField = new TextField("First name");
        TextField lastnameField = new TextField("Last name");
        TextField handleField = new TextField("Wanted username");

        AvatarField avatarField = new AvatarField("Select Avatar image");

        allowMarketingBox = new Checkbox("Allow Marketing?");
        EmailField emailField = new EmailField("Email");

        passwordField1 = new PasswordField("Wanted password");
        passwordField2 = new PasswordField("Password again");

        Span errorMessage = new Span();

        Button submitButton = new Button("Join the community");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        /*
         * Build the visible layout
         */
        add(firstnameField, lastnameField, handleField, avatarField, allowMarketingBox, emailField, passwordField1, passwordField2, errorMessage, submitButton);

        /*
         * Set up form functionality
         */

        /*
         * Binder is a form utility class provided by Vaadin. Here, we use a specialized
         * version to gain access to automatic Bean Validation (JSR-303). We provide our
         * data class so that the Binder can read the validation definitions on that
         * class and create appropriate validators. The BeanValidationBinder can
         * automatically validate all JSR-303 definitions, meaning we can concentrate on
         * custom things such as the passwords in this class.
         */
        binder = new BeanValidationBinder<UserDetails>(UserDetails.class);

        // Basic name fields that are required to fill in
        binder.forField(firstnameField).asRequired().bind("firstname");
        binder.forField(lastnameField).asRequired().bind("lastname");

        // The handle has a custom validator, in addition to being required
        binder.forField(handleField).withValidator(this::validateHandle).asRequired().bind("handle");

        // Because the AvatarField is of type HasValue<AvatarImage>, the Binder can bind
        // it automatically. The avatar is not required and doesn't have a validator,
        // but could.
        binder.forField(avatarField).bind("avatar");

        binder.forField(allowMarketingBox).bind("allowsMarketing");
        binder.forField(emailField).bind("email");
        // Only ask for email address if the user wants marketing emails
        allowMarketingBox.addValueChangeListener(e -> emailField.setVisible(allowMarketingBox.getValue()));

        // Another custom validator, this time for passwords
        binder.forField(passwordField1).asRequired().withValidator(this::passwordValidator).bind("password");
        // We won't bind passwordField2 to the Binder, because it will have the same
        // value as the first field when correctly filled in. We just use it for
        // validation.

        // The second field is not connected to the Binder, but we want the binder to
        // re-check the password validator when the field value changes.
        passwordField2.addValueChangeListener(e -> binder.validate());

        // A label where bean-level error messages go
        binder.setStatusLabel(errorMessage);

        // And finally the submit button
        submitButton.addClickListener(e -> {
            try {

                // Create empty bean to store the details into
                UserDetails detailsBean = new UserDetails();

                // Run validators and write the values to the bean
                binder.writeBean(detailsBean);

                // Call backend to store the data
                service.store(detailsBean);

                // Show success message if everything went well
                showSuccess(detailsBean);

            } catch (ValidationException e1) {
                // validation errors are already visible for each field,
                // and bean-level errors are shown in the status label.

                // We could show additional messages here if we want, do logging, etc.

            } catch (ServiceException e2) {

                // For same reason, the save failed in the back end.

                // First, make sure we store the error in the server logs (preferably using a
                // logging framework)
                e2.printStackTrace();

                // Notify, and let the user try again.
                errorMessage.setText("Saving the data failed, please try again");
            }
        });

    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(UserDetails detailsBean) {
        Notification notification = Notification.show("Data saved, welcome " + detailsBean.getHandle());
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Here you'd typically redirect the user to another view, we'll just clear the
        // form
        binder.readBean(new UserDetails());
        passwordField2.clear(); // this field isn't connected to the Binder so we manually clear it.
    }

    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Passwords in both field match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */
        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        String pass2 = passwordField2.getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    /**
     * Method that demonstrates using an external validator. Here we ask the backend
     * if this handle is already in use.
     */
    private ValidationResult validateHandle(String handle, ValueContext ctx) {

        String errorMsg = service.validateHandle(handle);

        if (errorMsg == null) {
            return ValidationResult.ok();
        }

        return ValidationResult.error(errorMsg);
    }
}
