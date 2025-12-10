package bimobile.views;

import bimobile.model.Vehicle;
import bimobile.model.VehicleStatus;
import bimobile.security.AuthorizationUtils;
import bimobile.service.VehicleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;



/**
 * Author: Lasse
 * Description: Vehicle management with role restrictions.
 */
@PermitAll
@Route(value = "vehicles", layout = MainLayout.class)
@PageTitle("Fahrzeugverwaltung")
public class VehicleView extends VerticalLayout {

    private final VehicleService vehicleService;

    private final Grid<Vehicle> grid = new Grid<>(Vehicle.class, false);

    private final TextField licensePlateField = new TextField("Kennzeichen");
    private final TextField brandField        = new TextField("Marke");
    private final TextField modelField        = new TextField("Modell");
    private final TextField priceClassField   = new TextField("Preisklasse");
    private final TextField mileageField      = new TextField("Kilometerstand");

    private Vehicle selectedVehicle;

    // New: MANAGEMENT + BRANCH_MANAGER may edit
    private final boolean canEdit =
            AuthorizationUtils.isManagement() || AuthorizationUtils.isBranchManager();

    public VehicleView(VehicleService vehicleService) {
        this.vehicleService = vehicleService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Fahrzeugverwaltung"));

        if (!canEdit) {
            Span info = new Span("Sie haben keine Berechtigung, Fahrzeugdaten zu ändern. Anzeige ist schreibgeschützt.");
            info.getStyle().set("color", "var(--lumo-secondary-text-color)");
            add(info);
        }

        configureGrid();
        configureForm();

        VerticalLayout formWithButtons = new VerticalLayout(createFormLayout());
        formWithButtons.setPadding(false);
        formWithButtons.setSpacing(true);
        formWithButtons.setWidthFull();

        add(grid, formWithButtons);
        expand(grid);

        refreshGrid();
        initValidationListeners();
    }

    private void configureGrid() {
        grid.setWidthFull();
        grid.setHeight("400px");

        grid.addColumn(Vehicle::getLicensePlate).setHeader("Kennzeichen");
        grid.addColumn(Vehicle::getBrand).setHeader("Marke");
        grid.addColumn(Vehicle::getModel).setHeader("Modell");
        grid.addColumn(Vehicle::getPriceClass).setHeader("Preisklasse");
        grid.addColumn(Vehicle::getMileage).setHeader("Kilometerstand");
        grid.addColumn(v -> v.getStatus() != null ? v.getStatus().getDisplayName() : "")
                .setHeader("Status");

        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedVehicle = event.getValue();
            populateForm(selectedVehicle);
        });
    }

    private void refreshGrid() {
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        grid.setItems(vehicles);
    }

    private void configureForm() {
        licensePlateField.setRequiredIndicatorVisible(true);
        brandField.setRequiredIndicatorVisible(true);
        modelField.setRequiredIndicatorVisible(true);

        licensePlateField.setWidthFull();
        brandField.setWidthFull();
        modelField.setWidthFull();
        priceClassField.setWidthFull();
        mileageField.setWidthFull();

        clearValidation();
    }

    private void clearValidation() {
        licensePlateField.setInvalid(false);
        brandField.setInvalid(false);
        modelField.setInvalid(false);
        priceClassField.setInvalid(false);
        mileageField.setInvalid(false);
    }

    private void clearFormFields() {
        licensePlateField.clear();
        brandField.clear();
        modelField.clear();
        priceClassField.clear();
        mileageField.clear();
    }

    private HorizontalLayout createFormLayout() {

        Button saveButton = new Button("Speichern", event -> {
            if (canEdit) saveVehicle();
            else Notification.show("Keine Berechtigung zum Speichern.");
        });

        Button setAvailableButton    = new Button("Verfügbar",     e -> { if (canEdit) changeStatus(VehicleStatus.AVAILABLE); });
        Button setRentedButton       = new Button("Verliehen",     e -> { if (canEdit) changeStatus(VehicleStatus.RENTED); });
        Button setMaintenanceButton  = new Button("In Wartung",    e -> { if (canEdit) changeStatus(VehicleStatus.IN_MAINTENANCE); });

        if (!canEdit) {
            saveButton.setEnabled(false);
            setAvailableButton.setEnabled(false);
            setRentedButton.setEnabled(false);
            setMaintenanceButton.setEnabled(false);
        }

        FormLayout formLayout = new FormLayout(
                licensePlateField,
                brandField,
                modelField,
                priceClassField,
                mileageField
        );

        HorizontalLayout buttonBar = new HorizontalLayout(
                saveButton, setAvailableButton, setRentedButton, setMaintenanceButton
        );

        HorizontalLayout wrapper = new HorizontalLayout(formLayout, buttonBar);
        wrapper.setWidthFull();
        wrapper.setAlignItems(Alignment.START);

        return wrapper;
    }

    private void populateForm(Vehicle vehicle) {
        if (vehicle == null) {
            clearFormFields();
            clearValidation();
            return;
        }

        licensePlateField.setValue(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "");
        brandField.setValue(vehicle.getBrand() != null ? vehicle.getBrand() : "");
        modelField.setValue(vehicle.getModel() != null ? vehicle.getModel() : "");
        priceClassField.setValue(vehicle.getPriceClass() != null ? vehicle.getPriceClass() : "");
        mileageField.setValue(String.valueOf(vehicle.getMileage()));

        clearValidation();
    }

    private void saveVehicle() {
        try {
            clearValidation();
            boolean valid = true;

            if (licensePlateField.isEmpty()) { licensePlateField.setInvalid(true); valid = false; }
            if (brandField.isEmpty())        { brandField.setInvalid(true);        valid = false; }
            if (modelField.isEmpty())        { modelField.setInvalid(true);        valid = false; }

            if (!valid) {
                Notification.show("Bitte Pflichtfelder ausfüllen.");
                return;
            }

            int mileage = parseMileage();

            if (selectedVehicle == null) {
                Vehicle vehicle = new Vehicle(
                        licensePlateField.getValue(),
                        brandField.getValue(),
                        modelField.getValue(),
                        priceClassField.getValue()
                );
                vehicle.setMileage(mileage);
                vehicleService.createVehicle(vehicle);
                Notification.show("Fahrzeug angelegt.");
            } else {
                selectedVehicle.setLicensePlate(licensePlateField.getValue());
                selectedVehicle.setBrand(brandField.getValue());
                selectedVehicle.setModel(modelField.getValue());
                selectedVehicle.setPriceClass(priceClassField.getValue());
                selectedVehicle.setMileage(mileage);

                vehicleService.updateVehicle(selectedVehicle);
                Notification.show("Fahrzeug aktualisiert.");
            }

            selectedVehicle = null;
            grid.deselectAll();
            clearFormFields();
            refreshGrid();

        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage());
        }
    }

    private void changeStatus(VehicleStatus newStatus) {
        if (selectedVehicle == null) {
            Notification.show("Bitte zuerst ein Fahrzeug auswählen.");
            return;
        }

        try {
            vehicleService.changeStatus(selectedVehicle.getId(), newStatus, "");
            Notification.show("Status geändert zu: " + newStatus.getDisplayName());
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage());
        }
    }

    private int parseMileage() {
        if (mileageField.isEmpty()) return 0;
        try {
            return Integer.parseInt(mileageField.getValue());
        } catch (NumberFormatException e) {
            Notification.show("Kilometerstand ungültig – es wird 0 gespeichert.");
            return 0;
        }
    }

    private void initValidationListeners() {
        licensePlateField.addValueChangeListener(e -> licensePlateField.setInvalid(false));
        brandField.addValueChangeListener(e -> brandField.setInvalid(false));
        modelField.addValueChangeListener(e -> modelField.setInvalid(false));
        priceClassField.addValueChangeListener(e -> priceClassField.setInvalid(false));
        mileageField.addValueChangeListener(e -> mileageField.setInvalid(false));
    }
}
