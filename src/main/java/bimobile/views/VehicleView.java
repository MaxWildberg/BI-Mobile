package bimobile.views;

import bimobile.model.PriceCategory;
import bimobile.model.Vehicle;
import bimobile.model.VehicleStatus;
import bimobile.service.VehicleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * Fahrzeugverwaltung:
 * - Zeigt alle Fahrzeuge in einer Tabelle
 * - Ermöglicht Anlegen und Bearbeiten von Fahrzeugen
 * - Bietet Buttons zum Ändern des Fahrzeugstatus
 */
@Route(value = "vehicles", layout = MainLayout.class)
@PageTitle("Fahrzeugverwaltung")
@PermitAll
public class VehicleView extends VerticalLayout {

    private final VehicleService vehicleService;

    // Tabelle
    private final Grid<Vehicle> grid = new Grid<>(Vehicle.class, false);

    // Formularfelder
    private final TextField licensePlateField = new TextField("Kennzeichen");
    private final TextField brandField        = new TextField("Marke");
    private final TextField modelField        = new TextField("Modell");
    private final ComboBox<PriceCategory> priceCategoryBox = new ComboBox<>("Preisklasse");
    private final TextField mileageField      = new TextField("Kilometerstand");

    private Vehicle selectedVehicle;

    public VehicleView(VehicleService vehicleService) {
        this.vehicleService = vehicleService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Fahrzeugverwaltung"));

        configureGrid();
        configureForm();

        // Layout: oben Tabelle, darunter Formular + Buttons
        VerticalLayout formWithButtons = new VerticalLayout(createFormLayout());
        formWithButtons.setPadding(false);
        formWithButtons.setSpacing(true);
        formWithButtons.setWidthFull();

        add(grid, formWithButtons);
        expand(grid); // Tabelle bekommt den meisten Platz

        refreshGrid();

        initValidationListeners();
    }

    // ----------------------------------------------------
    // Grid / Tabelle
    // ----------------------------------------------------

    private void configureGrid() {
        grid.setWidthFull();
        grid.setHeight("400px");

        grid.addColumn(Vehicle::getLicensePlate).setHeader("Kennzeichen");
        grid.addColumn(Vehicle::getBrand).setHeader("Marke");
        grid.addColumn(Vehicle::getModel).setHeader("Modell");
        grid.addColumn(Vehicle::getPriceCategory).setHeader("Preisklasse");
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

    // ----------------------------------------------------
    // Formular
    // ----------------------------------------------------

    private void configureForm() {
        // Nur optische Pflichtmarkierung, keine „roten“ Fehlermeldungen von Vaadin
        licensePlateField.setLabel("Kennzeichen");
        licensePlateField.setRequiredIndicatorVisible(true);

        brandField.setLabel("Marke");
        brandField.setRequiredIndicatorVisible(true);

        modelField.setLabel("Modell");
        modelField.setRequiredIndicatorVisible(true);

        priceCategoryBox.setLabel("Preisklasse");
        priceCategoryBox.setItems(PriceCategory.values());
        priceCategoryBox.setItemLabelGenerator(pc -> pc.name() + " (" + pc.getBaseRate() + " €)");

        mileageField.setLabel("Kilometerstand");

        licensePlateField.addValueChangeListener(e -> licensePlateField.setInvalid(false));
        brandField.addValueChangeListener(e -> brandField.setInvalid(false));
        modelField.addValueChangeListener(e -> modelField.setInvalid(false));

        // Breite
        licensePlateField.setWidthFull();
        brandField.setWidthFull();
        modelField.setWidthFull();
        priceCategoryBox.setWidthFull();
        mileageField.setWidthFull();

        clearValidation();
    }

    /**
     * Setzt alle Validierungsfehler (rote Markierung) zurück.
     */
    private void clearValidation() {
        licensePlateField.setInvalid(false);
        licensePlateField.setErrorMessage(null);

        brandField.setInvalid(false);
        brandField.setErrorMessage(null);

        modelField.setInvalid(false);
        modelField.setErrorMessage(null);

        priceCategoryBox.setInvalid(false);
        priceCategoryBox.setErrorMessage(null);

        mileageField.setInvalid(false);
        mileageField.setErrorMessage(null);
    }

    private void clearFormFields() {
        licensePlateField.clear();
        brandField.clear();
        modelField.clear();
        priceCategoryBox.clear();
        mileageField.clear();
    }


    private HorizontalLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("600px");

        formLayout.add(
                licensePlateField,
                brandField,
                modelField,
                priceCategoryBox,
                mileageField
        );

        // Buttons
        Button saveButton = new Button("Speichern", event -> saveVehicle());
        Button setAvailableButton = new Button("Verfügbar",
                e -> changeStatus(VehicleStatus.AVAILABLE));
        Button setRentedButton = new Button("Verliehen",
                e -> changeStatus(VehicleStatus.RENTED));
        Button setMaintenanceButton = new Button("In Wartung",
                e -> changeStatus(VehicleStatus.IN_MAINTENANCE));

        HorizontalLayout buttonBar =
                new HorizontalLayout(saveButton, setAvailableButton, setRentedButton, setMaintenanceButton);
        buttonBar.setSpacing(true);

        HorizontalLayout wrapper = new HorizontalLayout(formLayout, buttonBar);
        wrapper.setWidthFull();
        wrapper.setAlignItems(Alignment.START);

        return wrapper;
    }

    // ----------------------------------------------------
    // Formular <-> Entity
    // ----------------------------------------------------

    private void populateForm(Vehicle vehicle) {
        if (vehicle == null) {
            clearFormFields();
            clearValidation();
            return;
        }

        licensePlateField.setValue(
                vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "");
        brandField.setValue(
                vehicle.getBrand() != null ? vehicle.getBrand() : "");
        modelField.setValue(
                vehicle.getModel() != null ? vehicle.getModel() : "");
        priceCategoryBox.setValue(
                vehicle.getPriceCategory());
        mileageField.setValue(String.valueOf(vehicle.getMileage()));

        clearValidation();
    }

    private void saveVehicle() {
        try {
            // 1) Alte Fehlzustände zurücksetzen
            clearValidation();

            // 2) Pflichtfelder prüfen
            boolean valid = true;

            if (licensePlateField.isEmpty()) {
                licensePlateField.setInvalid(true);
                licensePlateField.setErrorMessage("Kennzeichen ist ein Pflichtfeld.");
                valid = false;
            }
            if (brandField.isEmpty()) {
                brandField.setInvalid(true);
                brandField.setErrorMessage("Marke ist ein Pflichtfeld.");
                valid = false;
            }
            if (modelField.isEmpty()) {
                modelField.setInvalid(true);
                modelField.setErrorMessage("Modell ist ein Pflichtfeld.");
                valid = false;
            }


            // Wenn etwas fehlt: Meldung anzeigen und abbrechen
            if (!valid) {
                Notification.show(
                        "Bitte füllen Sie alle Pflichtfelder aus (Kennzeichen, Marke, Modell).",
                        4000,
                        Notification.Position.MIDDLE
                );
                return;
            }

            // 3) Kilometerstand konvertieren
            int mileage = parseMileage(); // gibt 0 zurück, wenn leer oder ungültig

            // 4) Neues Fahrzeug oder bestehendes aktualisieren
            if (selectedVehicle == null) {
                // Neues Fahrzeug
                Vehicle vehicle = new Vehicle(
                        licensePlateField.getValue(),
                        brandField.getValue(),
                        modelField.getValue(),
                        priceCategoryBox.getValue()
                );
                vehicle.setMileage(mileage);

                vehicleService.createVehicle(vehicle);
                Notification.show("Fahrzeug wurde angelegt.");
            } else {
                // Bestehendes Fahrzeug aktualisieren
                selectedVehicle.setLicensePlate(licensePlateField.getValue());
                selectedVehicle.setBrand(brandField.getValue());
                selectedVehicle.setModel(modelField.getValue());
                selectedVehicle.setPriceCategory(priceCategoryBox.getValue());
                selectedVehicle.setMileage(mileage);

                vehicleService.updateVehicle(selectedVehicle);
                Notification.show("Fahrzeug wurde aktualisiert.");
            }

            // 5) Formular & Auswahl zurücksetzen und Tabelle aktualisieren
            selectedVehicle = null;
            grid.deselectAll();
            clearFormFields();
            clearValidation();
            refreshGrid();

        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage(),
                    5000, Notification.Position.MIDDLE);
        }
    }

    private void changeStatus(VehicleStatus newStatus) {
        if (selectedVehicle == null) {
            Notification.show("Bitte wählen Sie zuerst ein Fahrzeug in der Tabelle aus.");
            return;
        }

        try {
            vehicleService.changeStatus(selectedVehicle.getId(), newStatus, "" );
            Notification.show("Status wurde geändert zu: " + newStatus.getDisplayName());
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Fehler: " + ex.getMessage(),
                    5000, Notification.Position.MIDDLE);
        }
    }

    private int parseMileage() {
        if (mileageField.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(mileageField.getValue());
        } catch (NumberFormatException e) {
            Notification.show("Kilometerstand ist keine gültige Zahl. Es wird 0 gespeichert.");
            return 0;
        }
    }

    /**
     * Entfernt die rote Fehlermarkierung, sobald der Benutzer beginnt etwas einzugeben.
     */
    private void initValidationListeners() {
        licensePlateField.addValueChangeListener(e -> licensePlateField.setInvalid(false));
        brandField.addValueChangeListener(e -> brandField.setInvalid(false));
        modelField.addValueChangeListener(e -> modelField.setInvalid(false));
        priceCategoryBox.addValueChangeListener(e -> priceCategoryBox.setInvalid(false));
        mileageField.addValueChangeListener(e -> mileageField.setInvalid(false));
    }
}