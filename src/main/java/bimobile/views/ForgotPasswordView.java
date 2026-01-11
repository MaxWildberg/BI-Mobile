package bimobile.views;

import bimobile.service.PasswordResetService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * "Passwort vergessen"-Seite.
 * Hier gibt der Nutzer seine E-Mail ein und bekommt einen Reset-Link.
 *
 * @author Jannick Braun
 */


@Route("forgot-password")
@PageTitle("Passwort vergessen | BI-Mobile")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    private final PasswordResetService passwordResetService;
    private final EmailField emailField = new EmailField("E-Mail-Adresse");
    private final Button submitButton = new Button("Link senden");
    private final VerticalLayout formLayout = new VerticalLayout();
    private final VerticalLayout successLayout = new VerticalLayout();

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        addClassName("forgot-password-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        setupFormLayout();
        setupSuccessLayout();
        successLayout.setVisible(false);

        add(formLayout, successLayout);
    }

    private void setupFormLayout() {
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setWidth("350px");
        formLayout.setPadding(true);

        H1 title = new H1("Passwort vergessen");
        title.getStyle().set("font-size", "1.5em");

        Paragraph description = new Paragraph(
                "Geben Sie Ihre E-Mail-Adresse ein. Falls ein Konto existiert, erhalten Sie einen Link zum Zurücksetzen."
        );
        description.getStyle().set("text-align", "center");

        emailField.setWidthFull();
        emailField.setPlaceholder("beispiel@firma.de");
        emailField.setRequired(true);

        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.addClickListener(event -> handleSubmit());

        RouterLink backToLogin = new RouterLink("Zurück zum Login", LoginView.class);

        formLayout.add(title, description, emailField, submitButton, backToLogin);
    }

    private void setupSuccessLayout() {
        successLayout.setAlignItems(Alignment.CENTER);
        successLayout.setWidth("350px");
        successLayout.setPadding(true);

        H1 title = new H1("E-Mail gesendet");
        title.getStyle().set("font-size", "1.5em");

        Paragraph message = new Paragraph(
                "Falls ein Konto mit dieser E-Mail existiert, haben wir Ihnen einen Link gesendet. Der Link ist 24 Stunden gültig."
        );
        message.getStyle().set("text-align", "center");

        RouterLink backToLogin = new RouterLink("Zurück zum Login", LoginView.class);

        successLayout.add(title, message, backToLogin);
    }

    private void handleSubmit() {
        String email = emailField.getValue();

        if (email == null || email.isBlank()) {
            emailField.setInvalid(true);
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Wird gesendet...");

        try {
            String baseUrl = getBaseUrl();
            passwordResetService.initiatePasswordReset(email, baseUrl);

            formLayout.setVisible(false);
            successLayout.setVisible(true);
        } catch (Exception e) {
            Notification.show("Ein Fehler ist aufgetreten.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            submitButton.setEnabled(true);
            submitButton.setText("Link senden");
        }
    }

    private String getBaseUrl() {
        VaadinRequest request = VaadinRequest.getCurrent();
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) scheme = "http";
        String host = request.getHeader("Host");
        if (host == null) host = "localhost:8080";
        return scheme + "://" + host;
    }
}