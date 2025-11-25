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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * Einfache Fahrzeugverwaltungs-View.
 * - Tabelle mit allen Fahrzeugen
 * - Formular zum Anlegen/Bearbeiten
 * - Buttons zum Statuswechsel
 */
@Route(value = "vehicles", layout = MainLayout.class)
@PageTitle("Fahrzeugverwaltung")
@PermitAll
public class VehicleView extends VerticalLayout {

    private final VehicleService vehicleService;

    private final Grid<Vehicle> grid = new Grid<>(Vehicle.class, false);

    // Formularfelder
    private final TextField licensePlate = new TextField("Kennzeichen");
    private final TextField brand        = new TextField("Marke");
    private final TextField model        = new TextField("Modell");
    private final TextField priceClass   = new TextField("Preisklasse");
    private final NumberField mileage    = new NumberField("Kilometerstand");

    private Vehicle selectedVehicle;

    public VehicleView(VehicleService vehicleService) {
        this.vehicleService = vehicleService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add("Fahrzeugverwaltung");

        configureGrid();
        configureForm();

        HorizontalLayout content = new HorizontalLayout(grid, createFormLayout());
        content.setSizeFull();
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, createFormLayout());

        add(content);

        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Vehicle::getLicensePlate).setHeader("Kennzeichen").setAutoWidth(true);
        grid.addColumn(Vehicle::getBrand).setHeader("Marke").setAutoWidth(true);
        grid.addColumn(Vehicle::getModel).setHeader("Modell").setAutoWidth(true);
        grid.addColumn(Vehicle::getPriceClass).setHeader("Preisklasse").setAutoWidth(true);
        grid.addColumn(v -> v.getMileage() + " km").setHeader("Kilometerstand").setAutoWidth(true);
        grid.addColumn(v -> v.getStatus() != null ? v.getStatus().getDisplayName() : "")
                .setHeader("Status")
                .setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");

        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedVehicle = event.getValue();
            populateForm(selectedVehicle);
        });
    }

    private void configureForm() {
        licensePlate.setRequiredIndicatorVisible(true);
        brand.setRequiredIndicatorVisible(true);
        model.setRequiredIndicatorVisible(true);
    }

    private VerticalLayout createFormLayout() {
        FormLayout form = new FormLayout();
        mileage.setStep(1000);

        form.add(licensePlate, brand, model, priceClass, mileage);

        Button saveButton       = new Button("Speichern", e -> saveVehicle());
        Button availableButton  = new Button("Verfügbar", e -> changeStatus(VehicleStatus.AVAILABLE));
        Button rentedButton     = new Button("Verliehen", e -> changeStatus(VehicleStatus.RENTED));
        Button maintenanceButton= new Button("In Wartung", e -> changeStatus(VehicleStatus.IN_MAINTENANCE));

        HorizontalLayout buttons = new HorizontalLayout(saveButton, availableButton, rentedButton, maintenanceButton);

        VerticalLayout layout = new VerticalLayout(form, buttons);
        layout.setWidth("400px");
        return layout;
    }

    private void refreshGrid() {
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        grid.setItems(vehicles);
    }

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
        brand.setValue(vehicle.getBrand());
        model.setValue(vehicle.getModel());
        priceClass.setValue(vehicle.getPriceClass() != null ? vehicle.getPriceClass() : "");
        mileage.setValue((double) vehicle.getMileage());
    }

    private void saveVehicle() {
        try {
            if (licensePlate.isEmpty() || brand.isEmpty() || model.isEmpty()) {
                Notification.show("Bitte füllen Sie alle Pflichtfelder aus (Kennzeichen, Marke, Modell).");
                return;
            }

            int mileageValue = mileage.isEmpty() ? 0 : mileage.getValue().intValue();

            if (selectedVehicle == null) {
                Vehicle vehicle = new Vehicle(
                        licensePlate.getValue(),
                        brand.getValue(),
                        model.getValue(),
                        priceClass.getValue()
                );
                vehicle.setMileage(mileageValue);
                vehicleService.createVehicle(vehicle);
                Notification.show("Fahrzeug wurde angelegt.");
            } else {
                selectedVehicle.setLicensePlate(licensePlate.getValue());
                selectedVehicle.setBrand(brand.getValue());
                selectedVehicle.setModel(model.getValue());
                selectedVehicle.setPriceClass(priceClass.getValue());
                selectedVehicle.setMileage(mileageValue);
                vehicleService.updateVehicle(selectedVehicle);
                Notification.show("Fahrzeug wurde aktualisiert.");
            }

            selectedVehicle = null;
            populateForm(null);
            refreshGrid();

        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void changeStatus(VehicleStatus newStatus) {
        if (selectedVehicle == null) {
            Notification.show("Bitte wählen Sie zuerst ein Fahrzeug aus.");
            return;
        }

        try {
            vehicleService.changeStatus(selectedVehicle.getId(), newStatus, null);
            Notification.show("Status wurde geändert zu: " + newStatus.getDisplayName());
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Fehler beim Statuswechsel: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}