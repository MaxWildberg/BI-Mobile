package bimobile.views;

import bimobile.model.Vehicle;
import bimobile.model.VehicleStatus;
import bimobile.service.VehicleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * - Zeigt eine Tabelle mit allen Fahrzeugen
 * - Formular zum Erfassen/Ändern von Basisdaten
 * - Buttons für "Speichern" und einfache Statusänderungen
 */
@Route(value = "vehicles", layout = MainLayout.class)
@PageTitle("Fahrzeugverwaltung")
@PermitAll


public class VehicleView extends VerticalLayout {

    private final VehicleService vehicleService;

    private final Grid<Vehicle> grid = new Grid<>(Vehicle.class, false);

    // Formularfelder
    private final TextField licensePlate = new TextField("Kennzeichen");
    private final TextField brand = new TextField("Marke");
    private final TextField model = new TextField("Modell");
    private final TextField priceClass = new TextField("Preisklasse");
    private final TextField mileage = new TextField("Kilometerstand");

    private Vehicle selectedVehicle;

    public VehicleView(VehicleService vehicleService) {
        this.vehicleService = vehicleService;

        // Überschrift
        add("Fahrzeugverwaltung");

        configureGrid();
        configureForm();

        // Layout: Tabelle oben, Formular unten
        add(grid, createFormLayout());

        refreshGrid();
    }

    /**
     * Konfiguration der Fahrzeug-Tabelle.
     */
    private void configureGrid() {
        grid.addColumn(Vehicle::getLicensePlate).setHeader("Kennzeichen");
        grid.addColumn(Vehicle::getBrand).setHeader("Marke");
        grid.addColumn(Vehicle::getModel).setHeader("Modell");
        grid.addColumn(Vehicle::getPriceClass).setHeader("Preisklasse");
        grid.addColumn(Vehicle::getMileage).setHeader("Kilometerstand");
        grid.addColumn(v ->
                v.getStatus() != null ? v.getStatus().getDisplayName() : ""
        ).setHeader("Status");

        // Wenn eine Zeile angeklickt wird, werden die Daten ins Formular geladen
        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedVehicle = event.getValue();
            populateForm(selectedVehicle);
        });
    }

    /**
     * Formular mit Buttons für Speichern und einfache Statuswechsel.
     */
    private HorizontalLayout createFormLayout() {
        Button saveButton = new Button("Speichern", event -> saveVehicle());
        Button setAvailable = new Button("Verfügbar", e -> changeStatus(VehicleStatus.AVAILABLE));
        Button setRented = new Button("Verliehen", e -> changeStatus(VehicleStatus.RENTED));
        Button setInMaintenance = new Button("In Wartung", e -> changeStatus(VehicleStatus.IN_MAINTENANCE));

        HorizontalLayout buttons = new HorizontalLayout(saveButton, setAvailable, setRented, setInMaintenance);

        FormLayout formLayout = new FormLayout();
        formLayout.add(licensePlate, brand, model, priceClass, mileage);

        return new HorizontalLayout(formLayout, buttons);
    }

    /**
     * Setzt Basis-Eigenschaften des Formulars (z.B. Pflichtfelder).
     */
    private void configureForm() {
        licensePlate.setRequiredIndicatorVisible(true);
        brand.setRequiredIndicatorVisible(true);
        model.setRequiredIndicatorVisible(true);
    }

    /**
     * Lädt die aktuelle Fahrzeugliste aus der DB in die Tabelle.
     */
    private void refreshGrid() {
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        grid.setItems(vehicles);
    }

    /**
     * Befüllt das Formular mit den Daten des ausgewählten Fahrzeugs.
     */
    private void populateForm(Vehicle vehicle) {
        if (vehicle == null) {
            licensePlate.clear();
            brand.clear();
            model.clear();
            priceClass.clear();
            mileage.clear();
            return;
        }

        licensePlate.setValue(vehicle.getLicensePlate());
        brand.setValue(vehicle.getBrand() != null ? vehicle.getBrand() : "");
        model.setValue(vehicle.getModel() != null ? vehicle.getModel() : "");
        priceClass.setValue(vehicle.getPriceClass() != null ? vehicle.getPriceClass() : "");
        mileage.setValue(String.valueOf(vehicle.getMileage()));
    }

    /**
     * Speichert entweder ein neues Fahrzeug oder aktualisiert das ausgewählte.
     */
    private void saveVehicle() {
        try {
            if (selectedVehicle == null) {
                // Neues Fahrzeug anlegen
                Vehicle vehicle = new Vehicle(
                        licensePlate.getValue(),
                        brand.getValue(),
                        model.getValue(),
                        priceClass.getValue()
                );
                vehicle.setMileage(parseMileage());

                // Standard-Status: verfügbar
                vehicle.setStatus(VehicleStatus.AVAILABLE);

                vehicleService.createVehicle(vehicle);
                Notification.show("Fahrzeug erstellt");
            } else {
                // Bestehendes Fahrzeug aktualisieren
                selectedVehicle.setLicensePlate(licensePlate.getValue());
                selectedVehicle.setBrand(brand.getValue());
                selectedVehicle.setModel(model.getValue());
                selectedVehicle.setPriceClass(priceClass.getValue());
                selectedVehicle.setMileage(parseMileage());

                vehicleService.updateVehicle(selectedVehicle);
                Notification.show("Fahrzeug aktualisiert");
            }

            selectedVehicle = null;
            populateForm(null);
            refreshGrid();

        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Führt einen Statuswechsel über den Service durch.
     * Die Fachregeln (z.B. HU fällig) werden im Service geprüft.
     */
    private void changeStatus(VehicleStatus newStatus) {
        if (selectedVehicle == null) {
            Notification.show("Bitte wähle ein Fahrzeug aus.");
            return;
        }

        try {
            vehicleService.changeStatus(selectedVehicle.getId(), newStatus);
            Notification.show("Status wurde geändert zu: " + newStatus);
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    /**
     * Hilfsmethode: konvertiert den Kilometerstand aus dem Textfeld.
     */
    private int parseMileage() {
        try {
            return Integer.parseInt(mileage.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
