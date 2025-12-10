package bimobile.views;

import bimobile.dao.UserRepository;
import bimobile.model.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.PasswordField;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;

/**
 * Hauptlayout der BI-Mobile Verwaltungsoberfläche.

 * Dieses Layout definiert die globale Struktur der Anwendung, bestehend aus:
 * - einer Top-Bar mit Titel
 * - einer linken Navigationsleiste mit allen wichtigen Views.

 * Das Layout sorgt für ein einheitliches Styling und ein konsistentes Benutzererlebnis innerhalb der gesamten Anwendung.

 * Wird automatisch durch Vaadin verwendet, sobald Views das Layout in der @Route-Annotation angeben.
 *
 * @author Ben Berlin
 */
@PermitAll
public class MainLayout extends AppLayout {

    /**
     * Initialisiert das Hauptlayout der Anwendung.
     * Der Konstruktor:
     * - baut Top-Bar und die Linke Navigationsleiste auf.
     * - legt ein einheitliches visuelles Styling fest.
     */
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private User currentUser;

    public MainLayout(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        loadCurrentUser();
        // TopBar
        H3 brand = new H3("BI-Mobile · Verwaltung");
        brand.getStyle().set("margin", "0");

        // User Menu Button (oben rechts)
        Button userMenuButton = createUserMenuButton();

        HorizontalLayout top = new HorizontalLayout(brand, userMenuButton);

        top.setWidthFull();
        top.setPadding(true);
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.setAlignItems(FlexComponent.Alignment.CENTER);
        addToNavbar(top);

        // Linke Navigationsleiste
        VerticalLayout nav = new VerticalLayout();
        nav.setWidth("240px");
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.getStyle().set("background", "#f9fafb");
        nav.getStyle().set("border-right", "1px solid #e5e7eb");
        nav.getStyle().set("box-shadow", "2px 0 6px rgba(0,0,0,0.05)");

        // Navigationseinträge
        RouterLink dashboard = new RouterLink("Dashboard", DashboardView.class);
        RouterLink facilities = new RouterLink("Standorte", LocationsOverviewView.class);
        RouterLink vehicles = new RouterLink("Fahrzeuge", VehicleView.class);
        RouterLink rentals = new RouterLink("Ausleihen", RentalsOverviewView.class);
        RouterLink employees = new RouterLink("Mitarbeiter", EmployeeView.class);
        RouterLink customers = new RouterLink("Kunden", CustomerOverview.class);

        styleLinks(dashboard, facilities, vehicles, rentals, employees, customers);

        nav.add(new H3("Navigation"), dashboard, facilities, vehicles, rentals, employees, customers);
        addToDrawer(nav);

    }

    private void loadCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        currentUser = userOpt.orElse(null);
    }

    private Button createUserMenuButton() {
        Icon icon = VaadinIcon.COG.create();
        Button button = new Button(icon);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.getStyle().set("cursor", "pointer");

        button.addClickListener(e -> openUserMenu());

        return button;
    }

    private void openUserMenu() {
        Dialog dialog = new Dialog();
        dialog.setWidth("320px");
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Header
        H3 header = new H3("Mein Konto");
        header.getStyle().set("margin", "0 0 10px 0");

        // User Info
        Div userInfo = new Div();
        userInfo.getStyle()
                .set("background", "#f3f4f6")
                .set("padding", "12px")
                .set("border-radius", "8px")
                .set("margin-bottom", "10px");

        String userName = currentUser != null ? currentUser.getFullName() : "Unbekannt";
        String userEmail = currentUser != null ? currentUser.getEmail() : "-";
        String userRole = currentUser != null ? formatRole(currentUser.getRole().name()) : "-";

        Span nameSpan = new Span(userName);
        nameSpan.getStyle().set("font-weight", "bold").set("display", "block");

        Span emailSpan = new Span(userEmail);
        emailSpan.getStyle().set("color", "#6b7280").set("font-size", "0.9em").set("display", "block");

        Span roleSpan = new Span(userRole);
        roleSpan.getStyle().set("color", "#3b82f6").set("font-size", "0.85em").set("display", "block").set("margin-top", "4px");

        userInfo.add(nameSpan, emailSpan, roleSpan);

        // Passwort ändern Button
        Button changePasswordBtn = new Button("Passwort ändern", VaadinIcon.KEY.create());
        changePasswordBtn.setWidthFull();
        changePasswordBtn.addClickListener(e -> {
            dialog.close();
            openChangePasswordDialog();
        });

        // Logout Button
        Button logoutBtn = new Button("Abmelden", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logoutBtn.setWidthFull();
        logoutBtn.addClickListener(e -> {
            dialog.close();
            UI.getCurrent().getPage().setLocation("/logout");
        });

        content.add(header, userInfo, changePasswordBtn, logoutBtn);
        dialog.add(content);
        dialog.open();
    }

    private void openChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("350px");
        dialog.setHeaderTitle("Passwort ändern");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        PasswordField currentPassword = new PasswordField("Aktuelles Passwort");
        currentPassword.setWidthFull();
        currentPassword.setRequired(true);

        PasswordField newPassword = new PasswordField("Neues Passwort");
        newPassword.setWidthFull();
        newPassword.setRequired(true);
        newPassword.setMinLength(8);
        newPassword.setHelperText("Mindestens 8 Zeichen");

        PasswordField confirmPassword = new PasswordField("Passwort bestätigen");
        confirmPassword.setWidthFull();
        confirmPassword.setRequired(true);

        content.add(currentPassword, newPassword, confirmPassword);

        Button saveBtn = new Button("Speichern", e -> {
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Notification.show("Bitte alle Felder ausfüllen", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (newPassword.getValue().length() < 8) {
                Notification.show("Passwort muss mindestens 8 Zeichen haben", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (!newPassword.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwörter stimmen nicht überein", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (currentUser == null || !passwordEncoder.matches(currentPassword.getValue(), currentUser.getPassword())) {
                Notification.show("Aktuelles Passwort ist falsch", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            currentUser.setPassword(passwordEncoder.encode(newPassword.getValue()));
            userRepository.save(currentUser);

            Notification.show("Passwort erfolgreich geändert", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Abbrechen", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.add(content);
        dialog.open();
    }

    private String formatRole(String role) {
        return switch (role) {
            case "MANAGING_DIRECTOR" -> "Geschäftsführer";
            case "GENERAL_MANAGER" -> "Standortleiter";
            case "EMPLOYEE" -> "Mitarbeiter";
            default -> role;
        };
    }

    // Gemeinsames Styling der Links
    private void styleLinks(RouterLink... links) {
        for (RouterLink link : links) {
            link.getElement().getStyle().set("padding", "10px 16px");
            link.getElement().getStyle().set("border-radius", "8px");
            link.getElement().getStyle().set("margin", "4px 8px");
        }
    }
}