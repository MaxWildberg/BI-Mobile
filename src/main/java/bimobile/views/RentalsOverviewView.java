package bimobile.views;

import bimobile.model.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.service.RentalService;
import bimobile.service.CustomerService;
import bimobile.service.VehicleService;
import bimobile.service.FacilityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
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
import java.util.List;

/**
 * Übersicht aller Ausleihen im BI-Mobile System.
 *
 * Diese View stellt ein Grid zur Anzeige aller Ausleihen bereit und ermöglicht deren Verwaltung:
 *  - Anlegen neuer Ausleihen
 *  - Bearbeiten bestehender Ausleihen
 *  - Löschen von Ausleihen
 *
 * Alle Aktionen werden in Dialogen ausgeführt, angelehnt an die Standortverwaltung.
 *
 * @author Ben Berlin
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

    /**
     * Erstellt die Ausleihübersicht und initialisiert Layout, Grid und Aktionen.
     *
     * @param rentalService    Service für Ausleihoperationen
     * @param customerService  Service zum Laden von Kunden
     * @param vehicleService   Service zum Laden von Fahrzeugen
     * @param facilityService  Service zum Laden von Standorten
     */
    public RentalsOverviewView(RentalService rentalService,
                               CustomerService customerService,
                               VehicleService vehicleService,
                               FacilityService facilityService) {

        this.rentalService = rentalService;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;

        // Layout-Grundstruktur
        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        H2 title = new H2("Ausleihübersicht");

        Button neu = new Button("Neue Ausleihe anlegen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neu.addClickListener(e -> openCreateDialog());

        HorizontalLayout header = new HorizontalLayout(title, neu);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Grid-Konfiguration
        grid.addColumn(Rental::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(r -> r.getCustomer().getFullName()).setHeader("Kunde").setAutoWidth(true);
        grid.addColumn(r -> r.getVehicle().getLicensePlate()).setHeader("Fahrzeug").setAutoWidth(true);
        grid.addColumn(Rental::getStartDate).setHeader("Startdatum").setAutoWidth(true);
        grid.addColumn(Rental::getEndDate).setHeader("Enddatum").setAutoWidth(true);
        grid.addColumn(Rental::getTotalPrice).setHeader("Gesamtpreis (€)").setAutoWidth(true);
        grid.addColumn(r -> r.getStatus().name()).setHeader("Status").setAutoWidth(true);

        // Aktionsspalte (Bearbeiten / Löschen)
        grid.addComponentColumn(rental -> {
            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> openEditDialog(rental));

            Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
            loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            loeschen.addClickListener(e -> openDeleteDialog(rental));

            return new HorizontalLayout(bearbeiten, loeschen);
        }).setHeader("Aktionen");

        updateGrid();

        add(header, grid);
        setFlexGrow(1, grid);
    }

    /**
     * Lädt alle Ausleihen aus dem {@link RentalService} und
     * aktualisiert das Grid.
     *
     * Wird nach jeder Änderungsoperation aufgerufen.
     */
    private void updateGrid() {
        List<Rental> rentals = rentalService.findAll();
        grid.setItems(rentals);
    }

    /**
     * Öffnet einen Dialog zum Anlegen einer neuen Ausleihe.
     * Die Eingabe erfolgt über Comboboxen (Kunde, Fahrzeug, Standort)
     * und Datumsauswahl (Start, Ende).
     *
     * Validierungen:
     *  - Pflichtfelder müssen befüllt sein
     *  - Enddatum muss nach Startdatum liegen
     *  - Fahrzeug darf nicht belegt sein (Prüfung im Service)
     */
    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3("Neue Ausleihe anlegen");

        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(customerService.findAll());
        customerBox.setItemLabelGenerator(Customer::getFullName);

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        vehicleBox.setItems(VehicleService.findAll());
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItems(facilityService.findAll());
        // Falls Facility keine "Name"-Methode hat, Adresse verwenden:
        facilityBox.setItemLabelGenerator(Facility::getAddress);

        DatePicker startDate = new DatePicker("Startdatum");
        DatePicker endDate = new DatePicker("Enddatum");

        Button save = new Button("Speichern", e -> {
            try {
                if (customerBox.isEmpty() || vehicleBox.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                    Notification.show("Bitte mindestens Kunde, Fahrzeug, Start- und Enddatum angeben.")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                Rental rental = rentalService.createRental(
                        customerBox.getValue(),
                        vehicleBox.getValue(),
                        facilityBox.getValue(),
                        startDate.getValue(),
                        endDate.getValue()
                );

                Notification.show("Ausleihe #" + rental.getId() + " erfolgreich erstellt.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
                dialog.close();

            } catch (IllegalArgumentException ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Unerwarteter Fehler bei der Erstellung.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(customerBox, vehicleBox, facilityBox, startDate, endDate);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
        dialog.add(layout);
        dialog.open();
    }

    /**
     * Öffnet einen Dialog zur Bearbeitung einer bestehenden Ausleihe.
     *
     * Im Prototyp können Standort sowie Start- und Enddatum angepasst werden.
     * Der Gesamtpreis wird nach der Änderung neu berechnet.
     *
     * @param rental zu bearbeitende Ausleihe
     */
    private void openEditDialog(Rental rental) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3("Ausleihe bearbeiten (#" + rental.getId() + ")");

        // Kunde & Fahrzeug nur lesbar anzeigen
        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(customerService.findAll());
        customerBox.setItemLabelGenerator(Customer::getFullName);
        customerBox.setValue(rental.getCustomer());
        customerBox.setReadOnly(true);

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        vehicleBox.setItems(vehicleService.findAll());
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());
        vehicleBox.setValue(rental.getVehicle());
        vehicleBox.setReadOnly(true);

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItems(facilityService.findAll());
        facilityBox.setItemLabelGenerator(Facility::getAddress);
        facilityBox.setValue(rental.getFacility());

        DatePicker startDate = new DatePicker("Startdatum");
        startDate.setValue(rental.getStartDate());

        DatePicker endDate = new DatePicker("Enddatum");
        endDate.setValue(rental.getEndDate());

        Button save = new Button("Speichern", e -> {
            try {
                Rental updated = rentalService.updateRental(
                        rental,
                        facilityBox.getValue(),
                        startDate.getValue(),
                        endDate.getValue()
                );

                Notification.show("Ausleihe #" + updated.getId() + " erfolgreich aktualisiert.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
                dialog.close();

            } catch (IllegalArgumentException ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Unerwarteter Fehler bei der Aktualisierung.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(customerBox, vehicleBox, facilityBox, startDate, endDate);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
        dialog.add(layout);
        dialog.open();
    }

    /**
     * Öffnet einen Bestätigungsdialog zum Löschen einer Ausleihe.
     *
     * Die Ausleihe wird nach Bestätigung endgültig aus der Datenbank entfernt.
     *
     * @param rental zu löschende Ausleihe
     */
    private void openDeleteDialog(Rental rental) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Ausleihe löschen?");

        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie die Ausleihe wirklich löschen?");
        content.add("Kunde: " + rental.getCustomer().getFullName());
        content.add("Fahrzeug: " + rental.getVehicle().getLicensePlate());
        content.add("Zeitraum: " + rental.getStartDate() + " bis " + rental.getEndDate());

        Button confirmButton = new Button("Löschen", e -> {
            try {
                rentalService.deleteRental(rental);
                Notification.show("Ausleihe erfolgreich gelöscht.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Fehler beim Löschen der Ausleihe.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(confirmButton, cancelButton);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, content, actions);
        dialog.add(dialogLayout);
        dialog.open();
    }
}

