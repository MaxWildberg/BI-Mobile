package bimobile.views;

import bimobile.model.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.security.AuthorizationUtils;
import bimobile.service.RentalService;
import bimobile.service.CustomerService;
import bimobile.service.VehicleService;
import bimobile.service.FacilityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Lasse
 * Description: Rentals overview with role and facility restrictions.
 */
@Route(value = "ausleihen", layout = MainLayout.class)
@PageTitle("Ausleihübersicht")
@PermitAll
public class RentalsOverviewView extends VerticalLayout {

    private final RentalService rentalService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final FacilityService facilityService;

    private final Grid<Rental> grid = new Grid<>(Rental.class, false);
    private final Grid<ChangeLogEntry> changeLogGrid = new Grid<>(ChangeLogEntry.class, false);
    private final List<ChangeLogEntry> changeLogEntries = new ArrayList<>();

    private final boolean isManagement = AuthorizationUtils.isManagement();
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
    private final boolean isEmployee = AuthorizationUtils.isEmployee();

    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    public RentalsOverviewView(
            RentalService rentalService,
            CustomerService customerService,
            VehicleService vehicleService,
            FacilityService facilityService) {

        this.rentalService = rentalService;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;

        setPadding(true);
        setSizeFull();

        H2 title = new H2("Ausleihübersicht");

        Button neu = new Button("Neue Ausleihe anlegen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Jeder darf anlegen: Employee / BranchManager / Management
        neu.setEnabled(true);
        neu.addClickListener(e -> openCreateDialog());

        HorizontalLayout header = new HorizontalLayout(title, neu);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        configureGrid();
        updateGrid();

        add(header, grid, buildChangeLogSection());
        setFlexGrow(1, grid);
    }

    private void configureGrid() {

        grid.addColumn(Rental::getId).setHeader("ID");
        grid.addColumn(r -> r.getCustomer().getFullName()).setHeader("Kunde");
        grid.addColumn(r -> r.getVehicle().getLicensePlate()).setHeader("Fahrzeug");
        grid.addColumn(Rental::getStartDate).setHeader("Startdatum");
        grid.addColumn(Rental::getEndDate).setHeader("Enddatum");
        grid.addColumn(Rental::getTotalPrice).setHeader("Preis (€)");
        grid.addColumn(r -> r.getStatus().name()).setHeader("Status");

        grid.addComponentColumn(rental -> {

            // EMPLOYEE darf nur zurückgeben
            if (isEmployee) {
                return createReturnButton(rental);
            }

            // Manager darf nur eigene Rentals bearbeiten oder löschen
            boolean sameFacility = rental.getFacility() != null &&
                    currentFacility != null &&
                    rental.getFacility().getId().equals(currentFacility.getId());

            Button edit = new Button(new Icon(VaadinIcon.EDIT));
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            edit.setEnabled(isManagement || (isBranchManager && sameFacility));
            edit.addClickListener(e -> openEditDialog(rental));

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delete.setEnabled(isManagement || (isBranchManager && sameFacility));
            delete.addClickListener(e -> openDeleteDialog(rental));

            Button ret = createReturnButton(rental);
            ret.setEnabled(true);

            return new HorizontalLayout(edit, delete, ret);
        }).setHeader("Aktionen");
    }

    private Button createReturnButton(Rental rental) {
        Button zurück = new Button(new Icon(VaadinIcon.CHECK));
        zurück.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        zurück.addClickListener(e -> {
            try {
                rentalService.returnRental(rental);
                Notification.show("Ausleihe zurückgegeben.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
            } catch (Exception ex) {
                Notification.show("Fehler beim Zurückgeben.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return zurück;
    }

    private void updateGrid() {
        List<Rental> rentals = rentalService.findAllWithCustomerVehicleFacility();

        // Branch Manager + Employee sehen nur eigene Filiale
        if (!isManagement && currentFacility != null) {
            rentals = rentals.stream()
                    .filter(r -> r.getFacility() != null &&
                            r.getFacility().getId().equals(currentFacility.getId()))
                    .collect(Collectors.toList());
        }

        grid.setItems(rentals);
    }

    private void openCreateDialog() {

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        H3 title = new H3("Neue Ausleihe");

        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(customerService.findAllCustomers());
        customerBox.setItemLabelGenerator(Customer::getFullName);

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        vehicleBox.setItems(vehicleService.findAllVehicles());
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");

        // MANAGEMENT: alle Standorte
        if (isManagement) {
            facilityBox.setItems(facilityService.getAllFacilities());
        } else {
            // BranchManager + Employee: nur eigener Standort
            facilityBox.setItems(currentFacility);
            facilityBox.setValue(currentFacility);
            facilityBox.setEnabled(false);
        }

        facilityBox.setItemLabelGenerator(Facility::getAddress);

        DatePicker start = new DatePicker("Startdatum");
        DatePicker end = new DatePicker("Enddatum");

        enforceDateOrder(start, end);

        Button save = new Button("Speichern", e -> {
            if (customerBox.isEmpty() || vehicleBox.isEmpty() ||
                    start.isEmpty() || end.isEmpty()) {
                Notification.show("Bitte alle Pflichtfelder ausfüllen.");
                return;
            }

            try {
                rentalService.createRental(
                        customerBox.getValue(),
                        vehicleBox.getValue(),
                        facilityBox.getValue(),
                        start.getValue(),
                        end.getValue()
                );

                Notification.show("Ausleihe erstellt.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                updateGrid();
                dialog.close();

            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancel = new Button("Abbrechen", e -> dialog.close());

        dialog.add(new VerticalLayout(
                title,
                new FormLayout(customerBox, vehicleBox, facilityBox, start, end),
                new HorizontalLayout(save, cancel)
        ));

        dialog.open();
    }

    private void openEditDialog(Rental rental) {

        if (isEmployee) {
            Notification.show("Mitarbeiter dürfen keine Ausleihen bearbeiten.");
            return;
        }

        if (isBranchManager &&
                rental.getFacility() != null &&
                !rental.getFacility().getId().equals(currentFacility.getId())) {
            Notification.show("Sie dürfen nur Ausleihen Ihres Standorts bearbeiten.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        H3 title = new H3("Ausleihe bearbeiten #" + rental.getId());

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItemLabelGenerator(Facility::getAddress);

        if (isManagement) {
            facilityBox.setItems(facilityService.getAllFacilities());
        } else {
            facilityBox.setItems(currentFacility);
            facilityBox.setEnabled(false);
        }

        facilityBox.setValue(rental.getFacility());

        DatePicker start = new DatePicker("Startdatum");
        start.setValue(rental.getStartDate());

        DatePicker end = new DatePicker("Enddatum");
        end.setValue(rental.getEndDate());

        enforceDateOrder(start, end);

        Button save = new Button("Speichern", e -> {
            try {
                rentalService.updateRental(
                        rental,
                        facilityBox.getValue(),
                        start.getValue(),
                        end.getValue()
                );
                Notification.show("Ausleihe aktualisiert.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                updateGrid();
                dialog.close();

            } catch (Exception ex) {
                Notification.show(ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancel = new Button("Abbrechen", e -> dialog.close());

        dialog.add(new VerticalLayout(
                title,
                new FormLayout(facilityBox, start, end),
                new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }

    private void openDeleteDialog(Rental rental) {

        if (isEmployee) {
            Notification.show("Mitarbeiter dürfen keine Ausleihen löschen.");
            return;
        }

        if (isBranchManager &&
                rental.getFacility() != null &&
                !rental.getFacility().getId().equals(currentFacility.getId())) {
            Notification.show("Sie dürfen nur Ausleihen Ihres Standorts löschen.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        dialog.add(new VerticalLayout(
                new H3("Ausleihe löschen?"),
                new Button("Löschen", click -> {
                    rentalService.deleteRental(rental);
                    Notification.show("Ausleihe gelöscht.");
                    updateGrid();
                    dialog.close();
                }),
                new Button("Abbrechen", e -> dialog.close())
        ));

        dialog.open();
    }

    private void enforceDateOrder(DatePicker start, DatePicker end) {
        end.setMin(start.getValue());

        start.addValueChangeListener(event -> {
            LocalDate s = event.getValue();
            end.setMin(s);
            if (end.getValue() != null && end.getValue().isBefore(s)) {
                end.clear();
            }
        });
    }

    private VerticalLayout buildChangeLogSection() {

        changeLogGrid.addColumn(entry ->
                        entry.timestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .setHeader("Zeit");

        changeLogGrid.addColumn(ChangeLogEntry::user).setHeader("Benutzer");
        changeLogGrid.addColumn(ChangeLogEntry::action).setHeader("Aktion");
        changeLogGrid.addColumn(ChangeLogEntry::details).setHeader("Details");
        changeLogGrid.addColumn(ChangeLogEntry::status).setHeader("Status");

        changeLogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        changeLogGrid.setItems(changeLogEntries);

        VerticalLayout wrap = new VerticalLayout(
                new H3("Änderungsprotokoll"),
                changeLogGrid
        );

        wrap.getStyle().set("background", "white");
        wrap.getStyle().set("padding", "1rem");
        wrap.getStyle().set("border-radius", "8px");

        return wrap;
    }

    private record ChangeLogEntry(LocalDateTime timestamp,
                                  String user,
                                  String action,
                                  String details,
                                  String status) { }
}
