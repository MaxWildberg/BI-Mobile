package bimobile.views;

import bimobile.controller.EmployeeController;
import bimobile.model.Employee;
import bimobile.model.EmployeeChangeLog;
import bimobile.model.Facility;
import bimobile.model.RoleType;
import bimobile.security.AuthorizationUtils;
import bimobile.service.EmployeeChangeLogService;
import bimobile.service.FacilityService;
import bimobile.views.customer.DeleteDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hauptview fuer die Mitarbeiterverwaltung.
 * Zeigt Tabelle aller Mitarbeiter und darunter die Historie an.
 *
 * @author Jan Lasse Stegmann
 */
@Route(value = "employees", layout = MainLayout.class)
@PageTitle("Mitarbeiterverwaltung")
@PermitAll
public class EmployeeView extends VerticalLayout {

    private final EmployeeController controller;
    private final FacilityService facilityService;
    private final EmployeeChangeLogService changeLogService;

    private final Grid<Employee> grid = new Grid<>(Employee.class, false);
    private final Grid<EmployeeChangeLog> logGrid = new Grid<>(EmployeeChangeLog.class, false);

    private final boolean isManagement = AuthorizationUtils.isManagement();
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    public EmployeeView(EmployeeController controller,
                        FacilityService facilityService,
                        EmployeeChangeLogService changeLogService) {
        this.controller = controller;
        this.facilityService = facilityService;
        this.changeLogService = changeLogService;

        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");

        // Zugriffsschutz
        if (!isManagement && !isBranchManager) {
            removeAll();
            add(new Span("Sie haben keine Berechtigung für diesen Bereich."));
            return;
        }

        // Manager ohne Standort abfangen
        if (isBranchManager && currentFacility == null) {
            removeAll();
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.setAlignItems(Alignment.CENTER);
            errorLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            errorLayout.setSizeFull();
            Icon errorIcon = VaadinIcon.WARNING.create();
            errorIcon.setColor("var(--lumo-error-color)");
            errorIcon.setSize("48px");
            H2 errorTitle = new H2("Kein Standort zugewiesen");
            Span errorText = new Span("Bitte an GF wenden.");
            errorLayout.add(errorIcon, errorTitle, errorText);
            add(errorLayout);
            return;
        }

        HorizontalLayout header = createHeader();
        configureGrid();
        configureLogGrid();

        add(header, grid, buildLogSection());
        setFlexGrow(1, grid);

        refreshGrid();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Mitarbeiter");
        Button addEmployeeButton = new Button("Neuer Mitarbeiter", new Icon(VaadinIcon.PLUS));
        addEmployeeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addEmployeeButton.addClickListener(e -> openEmployeeDialog(null));

        HorizontalLayout header = new HorizontalLayout(title, addEmployeeButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private void configureGrid() {
        grid.addColumn(Employee::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Employee::getName).setHeader("Vorname").setAutoWidth(true);
        grid.addColumn(Employee::getLastname).setHeader("Nachname").setAutoWidth(true);
        grid.addColumn(Employee::getEmail).setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn(e -> e.getRole() != null ? formatRole(e.getRole().name()) : "-").setHeader("Rolle").setAutoWidth(true);
        grid.addColumn(e -> e.getFacility() != null ? e.getFacility().getAddress() : "-").setHeader("Standort").setAutoWidth(true);

        grid.addComponentColumn(e -> {
            Icon icon = e.isActive() ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
            icon.setColor(e.isActive() ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
            return icon;
        }).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(this::createGridActions).setHeader("Aktionen").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
    }

    private void configureLogGrid() {
        logGrid.addColumn(log -> log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .setHeader("Datum/Uhrzeit").setAutoWidth(true).setSortable(true);

        // Anzeige-Logik: Lebende Referenz > Gespeicherter Name > Fallback ID
        logGrid.addColumn(log -> {
            if (log.getEmployee() != null) {
                return log.getEmployee().getLastname() + ", " + log.getEmployee().getName();
            }
            if (log.getEmployeeNameSnapshot() != null) {
                return log.getEmployeeNameSnapshot() + " (Gelöscht)";
            }
            return "ID: " + log.getEmployeeIdSnapshot() + " (Gelöscht)";
        }).setHeader("Mitarbeiter").setAutoWidth(true);

        logGrid.addColumn(EmployeeChangeLog::getUserIdentifier).setHeader("Benutzer").setAutoWidth(true);
        logGrid.addColumn(EmployeeChangeLog::getAction).setHeader("Aktion").setAutoWidth(true);
        logGrid.addColumn(EmployeeChangeLog::getDetails).setHeader("Details").setAutoWidth(true);

        logGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        logGrid.setHeight("300px");
    }

    private VerticalLayout buildLogSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setWidthFull();
        layout.getStyle().set("background-color", "#ffffff");
        layout.getStyle().set("border-radius", "8px");
        layout.getStyle().set("box-shadow", "0 1px 4px rgba(0,0,0,0.1)");
        layout.getStyle().set("margin-top", "20px");

        H3 title = new H3("Änderungsprotokoll");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "10px");

        layout.add(title, logGrid);
        return layout;
    }

    private HorizontalLayout createGridActions(Employee employee) {
        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editBtn.addClickListener(e -> {
            if (isBranchManager && (currentFacility == null || employee.getFacility() == null || !employee.getFacility().getId().equals(currentFacility.getId()))) {
                Notification.show("Bearbeiten nur für eigenen Standort erlaubt.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                openEmployeeDialog(employee);
            }
        });

        Icon powerIcon = VaadinIcon.POWER_OFF.create();
        Button toggleBtn = new Button(powerIcon);
        toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        if (!employee.isActive()) toggleBtn.getStyle().set("color", "var(--lumo-success-color)");
        toggleBtn.addClickListener(e -> toggleEmployeeStatus(employee));

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        boolean canModify = true;
        if (isBranchManager) {
            if (currentFacility == null || employee.getFacility() == null || !employee.getFacility().getId().equals(currentFacility.getId())) {
                canModify = false;
            }
        }
        toggleBtn.setEnabled(canModify);
        deleteBtn.setEnabled(canModify);

        deleteBtn.addClickListener(e -> confirmDelete(employee));

        Button infoBtn = new Button(new Icon(VaadinIcon.INFO));
        infoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        infoBtn.addClickListener(e -> openDetailsDialog(employee));

        return new HorizontalLayout(editBtn, toggleBtn, deleteBtn, infoBtn);
    }

    private void toggleEmployeeStatus(Employee employee) {
        // Check: Manager duerfen keine Vorgesetzten nutzen
        if (isBranchManager) {
            if (employee.getRole() == RoleType.GENERAL_MANAGER || employee.getRole() == RoleType.MANAGING_DIRECTOR) {
                Notification.show("Fehler: Sie dürfen keine Vorgesetzten oder Standortleiter bearbeiten.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        boolean success = controller.toggleEmployeeStatus(employee.getId());
        if (success) {
            refreshGrid();
        } else {
            Notification.show("Fehler: Aktion nicht erlaubt.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
        layout.add(createDetailRow("Name:", employee.getName() + " " + employee.getLastname()));
        layout.add(createDetailRow("Rolle:", formatRole(employee.getRole().name())));
        layout.add(createDetailRow("Standort:", employee.getFacility() != null ? employee.getFacility().getAddress() : "-"));

        Button closeBtn = new Button("Schließen", e -> dialog.close());
        dialog.getFooter().add(closeBtn);
        dialog.add(layout);
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        Span lbl = new Span(label);
        lbl.getStyle().set("font-weight", "bold");
        lbl.setWidth("120px");
        return new HorizontalLayout(lbl, new Span(value));
    }

    private void openEmployeeDialog(Employee employee) {
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                employee, controller, facilityService, isBranchManager, currentFacility, this::refreshGrid
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
        logGrid.setItems(changeLogService.getAllEntries());
    }

    private String formatRole(String role) {
        return switch (role) {
            case "MANAGING_DIRECTOR" -> "Geschäftsführer";
            case "GENERAL_MANAGER" -> "Standortleiter";
            case "EMPLOYEE" -> "Mitarbeiter";
            default -> role;
        };
    }
}