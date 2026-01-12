package bimobile.views;

import bimobile.controller.EmployeeController;
import bimobile.model.Employee;
import bimobile.model.Facility;
import bimobile.security.AuthorizationUtils;
import bimobile.service.FacilityService;
import bimobile.views.customer.DeleteDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "employees", layout = MainLayout.class)
@PageTitle("Mitarbeiterverwaltung")
@PermitAll
public class EmployeeView extends VerticalLayout {

    private final EmployeeController controller;
    private final FacilityService facilityService;

    private final Grid<Employee> grid = new Grid<>(Employee.class, false);

    private final boolean isManagement = AuthorizationUtils.isManagement();
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    public EmployeeView(EmployeeController controller, FacilityService facilityService) {
        this.controller = controller;
        this.facilityService = facilityService;

        setPadding(true);
        setSizeFull();

        // 1. Grundlegende Berechtigung (darf ich hier überhaupt sein?)
        if (!isManagement && !isBranchManager) {
            removeAll();
            add(new Span("Sie haben keine Berechtigung für diesen Bereich."));
            return;
        }

        // 2. [NEU] Anti-Crash-Check: Manager ohne zugewiesene Filiale abfangen
        if (isBranchManager && currentFacility == null) {
            removeAll();

            // Schöne Fehlermeldung bauen
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.setAlignItems(Alignment.CENTER);
            errorLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            errorLayout.setSizeFull();

            Icon errorIcon = VaadinIcon.WARNING.create();
            errorIcon.setColor("var(--lumo-error-color)");
            errorIcon.setSize("48px");

            H2 errorTitle = new H2("Kein Standort zugewiesen");
            Span errorText = new Span("Ihrem Benutzerkonto ist keine Filiale zugeordnet. Bitte wenden Sie sich an die Geschäftsführung.");
            errorText.getStyle().set("color", "var(--lumo-secondary-text-color)");

            errorLayout.add(errorIcon, errorTitle, errorText);
            add(errorLayout);

            // WICHTIG: Hier returnen, damit der restliche Code (Grid laden etc.) nicht ausgeführt wird!
            return;
        }

        // Ab hier ist sichergestellt, dass currentFacility existiert (wenn man Manager ist)
        HorizontalLayout header = createHeader();
        configureGrid();

        add(header, grid);
        setFlexGrow(1, grid);

        refreshGrid();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Mitarbeiter");

        Button addEmployeeButton = new Button("Neuer Mitarbeiter", new Icon(VaadinIcon.PLUS));
        addEmployeeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        addEmployeeButton.addClickListener(e -> {
            openEmployeeDialog(null);
        });

        HorizontalLayout header = new HorizontalLayout(title, addEmployeeButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private void configureGrid() {
        // ID Spalte mit fester Breite
        grid.addColumn(Employee::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Employee::getName).setHeader("Vorname").setAutoWidth(true);
        grid.addColumn(Employee::getLastname).setHeader("Nachname").setAutoWidth(true);
        grid.addColumn(Employee::getEmail).setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn(e -> e.getRole() != null ? formatRole(e.getRole().name()) : "-").setHeader("Rolle").setAutoWidth(true);
        grid.addColumn(e -> e.getFacility() != null ? e.getFacility().getAddress() : "-").setHeader("Standort").setAutoWidth(true);

        grid.addComponentColumn(e -> {
            Icon icon;
            if (e.isActive()) {
                icon = VaadinIcon.CHECK_CIRCLE.create();
                icon.setColor("var(--lumo-success-color)");
            } else {
                icon = VaadinIcon.CLOSE_CIRCLE.create();
                icon.setColor("var(--lumo-error-color)");
            }
            return icon;
        }).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createGridActions).setHeader("Aktionen").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setWidthFull();
        grid.setHeightFull();
    }

    private HorizontalLayout createGridActions(Employee employee) {
        // 1. Bearbeiten Button
        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editBtn.addClickListener(e -> {
            // Null-Safe Check
            if (isBranchManager && (currentFacility == null || employee.getFacility() == null || !employee.getFacility().getId().equals(currentFacility.getId()))) {
                Notification.show("Bearbeiten nur für eigenen Standort erlaubt.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                openEmployeeDialog(employee);
            }
        });

        // 2. Power Button (Aktivieren / Deaktivieren)
        Icon powerIcon = VaadinIcon.POWER_OFF.create();
        Button toggleBtn = new Button(powerIcon);
        toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        if (employee.isActive()) {
            toggleBtn.getElement().setAttribute("title", "Deaktivieren");
        } else {
            toggleBtn.getElement().setAttribute("title", "Aktivieren");
            toggleBtn.getStyle().set("color", "var(--lumo-success-color)");
        }

        toggleBtn.addClickListener(e -> toggleEmployeeStatus(employee));

        // 3. Löschen Button (Mülleimer)
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.getElement().setAttribute("title", "Endgültig löschen");

        // Berechtigungsprüfung
        boolean canModify = true;
        if (isBranchManager) {
            // Null-Safe Check
            if (currentFacility == null || employee.getFacility() == null || !employee.getFacility().getId().equals(currentFacility.getId())) {
                canModify = false;
            }
        }
        toggleBtn.setEnabled(canModify);
        deleteBtn.setEnabled(canModify);

        deleteBtn.addClickListener(e -> confirmDelete(employee));

        // 4. Info Button
        Button infoBtn = new Button(new Icon(VaadinIcon.INFO));
        infoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        infoBtn.addClickListener(e -> openDetailsDialog(employee));

        return new HorizontalLayout(editBtn, toggleBtn, deleteBtn, infoBtn);
    }

    private void toggleEmployeeStatus(Employee employee) {
        try {
            boolean success = controller.toggleEmployeeStatus(employee.getId());
            if (success) {
                String msg = employee.isActive() ? "deaktiviert" : "aktiviert";
                Notification.show("Mitarbeiter wurde " + msg + ".").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } else {
                Notification.show("Status konnte nicht geändert werden.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDelete(Employee employee) {
        DeleteDialog<Employee> deleteDialog = new DeleteDialog<>(
                employee,
                e -> e.getName() + " " + e.getLastname(),
                e -> controller.deleteEmployee(e.getId()),
                this::refreshGrid
        );
        deleteDialog.open();
    }

    private void openDetailsDialog(Employee employee) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Mitarbeiter-Details");
        dialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        layout.add(createDetailRow("ID:", String.valueOf(employee.getId())));
        layout.add(createDetailRow("Name:", employee.getName() + " " + employee.getLastname()));
        layout.add(createDetailRow("Geburtsdatum:", employee.getBirthday()));
        layout.add(createDetailRow("E-Mail:", employee.getEmail()));
        layout.add(createDetailRow("Telefon:", employee.getPhoneNumber()));
        layout.add(createDetailRow("Rolle:", formatRole(employee.getRole().name())));
        layout.add(createDetailRow("Standort:", employee.getFacility() != null ? employee.getFacility().getAddress() : "-"));

        String status = employee.isActive() ? "Aktiv" : "Inaktiv";
        layout.add(createDetailRow("Status:", status));

        Button closeBtn = new Button("Schließen", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);

        dialog.add(layout);
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        Span lbl = new Span(label);
        lbl.getStyle().set("font-weight", "bold");
        lbl.setWidth("120px");

        Span val = new Span(value);

        HorizontalLayout row = new HorizontalLayout(lbl, val);
        row.setAlignItems(FlexComponent.Alignment.BASELINE);
        return row;
    }

    private void openEmployeeDialog(Employee employee) {
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                employee,
                controller,
                facilityService,
                isBranchManager,
                currentFacility,
                this::refreshGrid
        );
        dialog.open();
    }

    private void refreshGrid() {
        List<Employee> employees;
        if (isBranchManager && currentFacility != null) {
            employees = controller.getEmployeesByFacility(currentFacility.getId());
        } else {
            employees = controller.getAllEmployees();
        }
        grid.setItems(employees);
    }

    private String formatRole(String role) {
        return switch (role) {
            case "MANAGING_DIRECTOR" -> "Standortleiter";
            case "GENERAL_MANAGER" -> "Geschäftsführer";
            case "EMPLOYEE" -> "Mitarbeiter";
            default -> role;
        };
    }
}