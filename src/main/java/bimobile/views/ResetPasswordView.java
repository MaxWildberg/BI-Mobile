package bimobile.views;

import bimobile.model.PasswordResetToken;
import bimobile.service.PasswordResetService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route("reset-password")
@PageTitle("Passwort zurücksetzen | BI-Mobile")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordResetService passwordResetService;
    private String token;

    private final VerticalLayout formLayout = new VerticalLayout();
    private final VerticalLayout invalidLayout = new VerticalLayout();
    private final VerticalLayout successLayout = new VerticalLayout();

    private final PasswordField passwordField = new PasswordField("Neues Passwort");
    private final PasswordField confirmField = new PasswordField("Passwort bestätigen");
    private final Button submitButton = new Button("Passwort ändern");

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        setupFormLayout();
        setupInvalidLayout();
        setupSuccessLayout();

        formLayout.setVisible(false);
        invalidLayout.setVisible(false);
        successLayout.setVisible(false);

        add(formLayout, invalidLayout, successLayout);
    }

    private void setupFormLayout() {
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setWidth("350px");

        H1 title = new H1("Neues Passwort");
        title.getStyle().set("font-size", "1.5em");

        passwordField.setWidthFull();
        passwordField.setMinLength(8);
        passwordField.setHelperText("Mindestens 8 Zeichen");

        confirmField.setWidthFull();

        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.addClickListener(e -> handleSubmit());

        formLayout.add(title, passwordField, confirmField, submitButton);
    }

    private void setupInvalidLayout() {
        invalidLayout.setAlignItems(Alignment.CENTER);
        invalidLayout.setWidth("350px");

        H1 title = new H1("Ungültiger Link");
        title.getStyle().set("font-size", "1.5em");
        title.getStyle().set("color", "var(--lumo-error-color)");

        Paragraph message = new Paragraph("Der Link ist ungültig oder abgelaufen.");
        RouterLink newLink = new RouterLink("Neuen Link anfordern", ForgotPasswordView.class);
        RouterLink loginLink = new RouterLink("Zurück zum Login", LoginView.class);

        invalidLayout.add(title, message, newLink, loginLink);
    }

    private void setupSuccessLayout() {
        successLayout.setAlignItems(Alignment.CENTER);
        successLayout.setWidth("350px");

        H1 title = new H1("Passwort geändert");
        title.getStyle().set("font-size", "1.5em");
        title.getStyle().set("color", "var(--lumo-success-color)");

        Paragraph message = new Paragraph("Sie können sich jetzt mit Ihrem neuen Passwort anmelden.");

        Button loginButton = new Button("Zum Login", e -> getUI().ifPresent(ui -> ui.navigate("login")));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        successLayout.add(title, message, loginButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        List<String> tokenParams = params.get("token");

        if (tokenParams == null || tokenParams.isEmpty()) {
            invalidLayout.setVisible(true);
            return;
        }

        token = tokenParams.get(0);
        Optional<PasswordResetToken> resetToken = passwordResetService.validateToken(token);

        if (resetToken.isEmpty()) {
            invalidLayout.setVisible(true);
        } else {
            formLayout.setVisible(true);
        }
    }

    private void handleSubmit() {
        String password = passwordField.getValue();
        String confirm = confirmField.getValue();

        if (password.length() < 8) {
            passwordField.setInvalid(true);
            passwordField.setErrorMessage("Mindestens 8 Zeichen");
            return;
        }

        if (!password.equals(confirm)) {
            confirmField.setInvalid(true);
            confirmField.setErrorMessage("Passwörter stimmen nicht überein");
            return;
        }

        submitButton.setEnabled(false);

        if (passwordResetService.resetPassword(token, password)) {
            formLayout.setVisible(false);
            successLayout.setVisible(true);
        } else {
            formLayout.setVisible(false);
            invalidLayout.setVisible(true);
        }
    }
}