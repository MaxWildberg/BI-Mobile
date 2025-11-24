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
    private final TextField licensePlate = new TextField("License plate");
    private final TextField brand = new TextField("Brand");
    private final TextField model = new TextField("Model");
    private final TextField priceClass = new TextField("Price class");
    private final TextField mileage = new TextField("Mileage");

    private Vehicle selectedVehicle;

    public VehicleView(VehicleService vehicleService) {
        this.vehicleService = vehicleService;

        // Überschrift
        add("Vehicle Management");

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
        grid.addColumn(Vehicle::getLicensePlate).setHeader("License plate");
        grid.addColumn(Vehicle::getBrand).setHeader("Brand");
        grid.addColumn(Vehicle::getModel).setHeader("Model");
        grid.addColumn(Vehicle::getPriceClass).setHeader("Price class");
        grid.addColumn(Vehicle::getMileage).setHeader("Mileage");
        grid.addColumn(v -> v.getStatus().name()).setHeader("Status");

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
        Button saveButton = new Button("Save", event -> saveVehicle());
        Button setAvailable = new Button("Set AVAILABLE", e -> changeStatus(VehicleStatus.AVAILABLE));
        Button setRented = new Button("Set RENTED", e -> changeStatus(VehicleStatus.RENTED));
        Button setInMaintenance = new Button("Set IN_MAINTENANCE", e -> changeStatus(VehicleStatus.IN_MAINTENANCE));

        HorizontalLayout buttons = new HorizontalLayout(saveButton, setAvailable, setRented, setInMaintenance);

        FormLayout formLayout = new FormLayout();
        formLayout.add(licensePlate, brand, model, priceClass, mileage);

        return new HorizontalLayout(formLayout, buttons);
    }

    /**
     * Setzt Basis-Eigenschaften des Formulars (z.B. Pflichtfelder).
     */
    private void configureForm() {
        licensePlate.setRequired(true);
        brand.setRequired(true);
        model.setRequired(true);
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
                vehicleService.createVehicle(vehicle);
                Notification.show("Vehicle created");
            } else {
                // Bestehendes Fahrzeug aktualisieren
                selectedVehicle.setLicensePlate(licensePlate.getValue());
                selectedVehicle.setBrand(brand.getValue());
                selectedVehicle.setModel(model.getValue());
                selectedVehicle.setPriceClass(priceClass.getValue());
                selectedVehicle.setMileage(parseMileage());

                vehicleService.updateVehicle(selectedVehicle);
                Notification.show("Vehicle updated");
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
            Notification.show("Please select a vehicle first.");
            return;
        }

        try {
            vehicleService.changeStatus(selectedVehicle.getId(), newStatus);
            Notification.show("Status changed to " + newStatus);
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
