package bimobile.views;

import bimobile.dao.UserRepository;
import bimobile.model.User;
import bimobile.security.AuthorizationUtils;
import bimobile.views.customer.CustomerOverview;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Optional;

/**
 * Zentrales Vaadin-AppLayout der Anwendung: stellt Topbar und Navigationsmenü bereit.
 * Implementiert nun auch client-seitige Schutzmaßnahmen für URLs.
 *
 * @author Jannick Braun, Ben Berlin, Jan Lasse Stegmann
 */
@PermitAll
public class MainLayout extends AppLayout {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private User currentUser;

    public MainLayout(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        loadCurrentUser();

        // 1. URL-Schutz implementieren (Verhindert manuelle Eingabe der URL)
        registerNavigationGuard();

        // TopBar
        H3 brand = new H3("BI-Mobile · Verwaltung");
        brand.getStyle().set("margin", "0");

        Button userMenuButton = createUserMenuButton();

        HorizontalLayout top = new HorizontalLayout(brand, userMenuButton);
        top.setWidthFull();
        top.setPadding(true);
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.setAlignItems(FlexComponent.Alignment.CENTER);
        addToNavbar(top);

        VerticalLayout nav = new VerticalLayout();
        nav.setWidth("240px");
        nav.setPadding(true);
        nav.setSpacing(false);
        nav.setAlignItems(FlexComponent.Alignment.STRETCH);
        nav.getStyle().set("background", "#f9fafb");
        nav.getStyle().set("border-right", "1px solid #e5e7eb");
        nav.getStyle().set("box-shadow", "2px 0 6px rgba(0,0,0,0.05)");

        H3 navHeader = new H3("Navigation");
        navHeader.getStyle().set("margin", "0 0 12px 0");

        // Navigationseinträge erstellen
        RouterLink dashboard = createNavLink("Dashboard", VaadinIcon.HOME, DashboardView.class);

        // Berechtigungsprüfung für Standorte
        boolean canViewLocations = AuthorizationUtils.canAccessLocations();
        RouterLink facilities = createNavLink("Standorte", VaadinIcon.OFFICE, LocationsOverviewView.class);
        facilities.setEnabled(canViewLocations); // Deaktiviert Link (klickbar & visuell)
        styleDisabledLink(facilities, canViewLocations);

        RouterLink vehicles = createNavLink("Fahrzeuge", VaadinIcon.CAR, VehicleView.class);
        RouterLink rentals = createNavLink("Ausleihen", VaadinIcon.CLIPBOARD_TEXT, RentalsOverviewView.class);

        // Berechtigungsprüfung für Mitarbeiter
        boolean canViewEmployees = AuthorizationUtils.canAccessEmployees();
        RouterLink employees = createNavLink("Mitarbeiter", VaadinIcon.GROUP, EmployeeView.class);
        employees.setEnabled(canViewEmployees);
        styleDisabledLink(employees, canViewEmployees);

        RouterLink customers = createNavLink("Kunden", VaadinIcon.USER_CARD, CustomerOverview.class);

        nav.add(navHeader, dashboard, facilities, vehicles, rentals, employees, customers);
        addToDrawer(nav);
    }

    private void registerNavigationGuard() {
        addAttachListener(attachEvent -> {
            UI.getCurrent().addBeforeEnterListener(event -> {
                Class<?> target = event.getNavigationTarget();

                // Schutz für Standorte
                if (LocationsOverviewView.class.equals(target) && !AuthorizationUtils.canAccessLocations()) {
                    event.rerouteTo(DashboardView.class);
                    Notification.show("Zugriff verweigert: Keine Berechtigung für Standorte.", 3000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                // Schutz für Mitarbeiter
                if (EmployeeView.class.equals(target) && !AuthorizationUtils.canAccessEmployees()) {
                    event.rerouteTo(DashboardView.class);
                    Notification.show("Zugriff verweigert: Keine Berechtigung für Mitarbeiterverwaltung.", 3000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
        });
    }

    private void styleDisabledLink(RouterLink link, boolean enabled) {
        if (!enabled) {
            link.getStyle().set("opacity", "0.5");
            link.getStyle().set("pointer-events", "none"); // Sicherstellen, dass kein Klick möglich ist
        }
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
            logout();
        });


        content.add(header, userInfo, changePasswordBtn, logoutBtn);
        dialog.add(content);
        dialog.open();
    }

    private void logout() {
        UI.getCurrent().getPage().setLocation("/login");
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(),
                null,
                null
        );
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
            case "MANAGING_DIRECTOR" -> "Geschäftsleitung";
            case "GENERAL_MANAGER" -> "Geschäftsführer";
            case "EMPLOYEE" -> "Mitarbeiter";
            default -> role;
        };
    }

    private RouterLink createNavLink(String label, VaadinIcon iconType, Class<? extends Component> navigationTarget) {
        RouterLink link = new RouterLink("", navigationTarget);
        link.getStyle().set("width", "100%");
        link.getElement().getStyle().set("display", "flex");
        link.getElement().getStyle().set("align-items", "center");
        link.getElement().getStyle().set("gap", "10px");
        link.getElement().getStyle().set("padding", "10px 14px");
        link.getElement().getStyle().set("margin", "4px 0");
        link.getElement().getStyle().set("border-radius", "10px");
        link.getElement().getStyle().set("color", "#1f2937");
        link.getElement().getStyle().set("text-decoration", "none");
        link.getElement().getStyle().set("font-weight", "500");
        link.getElement().getStyle().set("transition", "background-color 120ms ease, color 120ms ease");

        Icon icon = iconType.create();
        icon.setColor("var(--lumo-primary-color)");
        icon.getStyle().set("width", "18px").set("height", "18px");

        Span text = new Span(label);

        link.add(icon, text);

        link.getElement().addEventListener("mouseenter", e -> {
        }).addEventData("event");
        link.getElement().addEventListener("mouseleave", e -> {
        }).addEventData("event");

        link.getElement().getStyle().set("cursor", "pointer");
        link.getElement().getStyle().set("background-color", "transparent");
        link.getElement().setAttribute("onmouseenter", "this.style.backgroundColor='#eef2ff'; this.style.color='#111827';");
        link.getElement().setAttribute("onmouseleave", "this.style.backgroundColor='transparent'; this.style.color='#1f2937';");

        return link;
    }
}