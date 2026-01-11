package bimobile.views;

import bimobile.enums.FuelType;
import bimobile.model.*;
import bimobile.security.AuthorizationUtils;
import bimobile.service.FacilityService; // NEU
import bimobile.service.VehicleService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI Komponente für Fahrzeugverwaltung.
 * @author Halil Sentürk
 */
@Route(value = "vehicles", layout = MainLayout.class)
@PageTitle("Fahrzeugverwaltung")
@PermitAll
public class VehicleView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(VehicleView.class);
    private final VehicleService vehicleService;
    private final FacilityService facilityService; // NEU

    // Tabelle
    private final Grid<Vehicle> grid = new Grid<>(Vehicle.class, false);

    // Formularfelder
    private final TextField licensePlateField = new TextField("Kennzeichen");
    private final TextField brandField        = new TextField("Marke");
    private final TextField modelField        = new TextField("Modell");
    private final ComboBox<FuelType> fuelTypeField = new ComboBox<>("Antriebsart");
    private final ComboBox<PriceCategory> priceCategoryBox = new ComboBox<>("Preiskategorie");

    // [NEU] Standort-Auswahl
    private final ComboBox<Facility> facilityBox = new ComboBox<>("Standort");

    private final TextField mileageField      = new TextField("Kilometerstand");
    private final NumberField acquisitionPriceField = new NumberField("Beschaffungspreis (€)");

    private final DatePicker nextInspectionField   = new DatePicker("Nächste HU / Inspektion");
    private final DatePicker nextServiceField      = new DatePicker("Nächster Service");
    private final Checkbox maintenanceActiveField  = new Checkbox("Wartung aktiv");

    private final Checkbox smokingAllowedField = new Checkbox("Raucher-Fahrzeug");
    private final Checkbox navigationField     = new Checkbox("Navi vorhanden");
    private final Checkbox airConditionField   = new Checkbox("Klimaanlage");
    private final Checkbox winterTiresField    = new Checkbox("Winterreifen");

    private Vehicle selectedVehicle;

    // Security-Flag: MANAGEMENT + BRANCH_MANAGER dürfen bearbeiten
    private final boolean isManagement = AuthorizationUtils.isManagement();
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
    private final boolean canEdit = isManagement || isBranchManager;
    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    public VehicleView(VehicleService vehicleService, FacilityService facilityService) {
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;

        // Layout Grundstruktur
        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        // [NEU] Anti-Crash für Manager ohne Standort
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
            Span errorText = new Span("Ihrem Benutzerkonto ist keine Filiale zugeordnet. Bitte wenden Sie sich an die Zentrale.");
            errorLayout.add(errorIcon, errorTitle, errorText);
            add(errorLayout);
            return;
        }

        H2 title = new H2("Fahrzeugverwaltung");

        // Info-Hinweis, wenn der Nutzer nicht editieren darf
        if (!canEdit) {
            Span info = new Span("Sie haben keine Berechtigung, Fahrzeugdaten zu ändern. Anzeige ist schreibgeschützt.");
            info.getStyle().set("color", "var(--lumo-secondary-text-color)");
            add(info);
        }

        // Button "Neues Fahrzeug anlegen"
        Button neu = new Button("Neues Fahrzeug anlegen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neu.addClickListener(e -> {
            if (!canEdit) {
                Notification.show("Keine Berechtigung zum Anlegen von Fahrzeugen.");
                return;
            }
            openCreateDialog();
        });

        // Wenn keine Bearbeitungsrechte → Button deaktivieren
        if (!canEdit) {
            neu.setEnabled(false);
        }

        HorizontalLayout header = new HorizontalLayout(title, neu);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        configureGrid();
        configureForm();
        initValidationListeners();

        refreshGrid();

        add(header, grid);
        setFlexGrow(1, grid);
    }

    // ----------------------------------------------------
    // Grid / Tabelle
    // ----------------------------------------------------

    private void configureGrid() {
        grid.setWidthFull();
        grid.setHeight("400px");

        grid.addColumn(Vehicle::getLicensePlate).setHeader("Kennzeichen").setAutoWidth(true);
        grid.addColumn(Vehicle::getBrand).setHeader("Marke").setAutoWidth(true);
        grid.addColumn(Vehicle::getModel).setHeader("Modell").setAutoWidth(true);
        // [NEU] Standort-Spalte
        grid.addColumn(v -> v.getFacility() != null ? v.getFacility().getAddress() : "-").setHeader("Standort").setAutoWidth(true);

        grid.addColumn(v -> v.getFuelType() != null ? v.getFuelType().name() : "")
                .setHeader("Antriebsart").setAutoWidth(true);
        grid.addColumn(v -> v.getPriceCategory() != null ? v.getPriceCategory().name() : "")
                .setHeader("Preiskategorie").setAutoWidth(true);
        grid.addColumn(Vehicle::getMileage).setHeader("Kilometerstand").setAutoWidth(true);
        grid.addColumn(v -> v.getStatus() != null ? v.getStatus().getDisplayName() : "")
                .setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(vehicle -> {
            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> {
                if (!canEdit) {
                    Notification.show("Keine Berechtigung zum Bearbeiten von Fahrzeugen.");
                    return;
                }
                openEditDialog(vehicle);
            });

            Button history = new Button(new Icon(VaadinIcon.CLIPBOARD_TEXT));
            history.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            history.getElement().setProperty("title", "Lebenslauf anzeigen");
            history.addClickListener(e -> openHistoryDialog(vehicle));

            Button verkaufen = new Button(new Icon(VaadinIcon.EURO));
            verkaufen.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            verkaufen.getElement().setProperty("title", "Fahrzeug verkaufen");

            // Button deaktivieren, wenn schon verkauft/ausgemustert
            boolean isGone = (vehicle.getStatus() == VehicleStatus.SCRAPPED || vehicle.getStatus() == VehicleStatus.SOLD);
            if (isGone) {
                verkaufen.setEnabled(false);
                bearbeiten.setEnabled(false);
            }

            // Falls keine Bearbeitungsrechte → Bearbeiten + Verkaufen deaktivieren
            if (!canEdit) {
                bearbeiten.setEnabled(false);
                verkaufen.setEnabled(false);
            }

            // [NEU] Manager darf nur eigene Fahrzeuge bearbeiten
            if (isBranchManager && (vehicle.getFacility() == null || !vehicle.getFacility().getId().equals(currentFacility.getId()))) {
                bearbeiten.setEnabled(false);
                verkaufen.setEnabled(false);
            }

            verkaufen.addClickListener(e -> {
                if (!canEdit) {
                    Notification.show("Keine Berechtigung zum Verkaufen von Fahrzeugen.");
                    return;
                }
                openSellDialog(vehicle);
            });

            return new HorizontalLayout(bearbeiten, history, verkaufen);
        }).setHeader("Aktionen").setAutoWidth(true);
    }

    private void refreshGrid() {
        List<Vehicle> vehicles;
        // [NEU] Filterung für Manager
        if (isBranchManager && currentFacility != null) {
            vehicles = vehicleService.getVehiclesByFacility(currentFacility.getId());
        } else {
            vehicles = vehicleService.findAllVehicles();
        }
        grid.setItems(vehicles);
    }

    // ----------------------------------------------------
    // Formular
    // ----------------------------------------------------

    private void configureForm() {
        // Pflichtindikatoren
        licensePlateField.setRequiredIndicatorVisible(true);
        brandField.setRequiredIndicatorVisible(true);
        modelField.setRequiredIndicatorVisible(true);

        mileageField.setLabel("Kilometerstand");

        licensePlateField.addValueChangeListener(e -> licensePlateField.setInvalid(false));
        brandField.addValueChangeListener(e -> brandField.setInvalid(false));
        modelField.addValueChangeListener(e -> modelField.setInvalid(false));

        // Breite
        licensePlateField.setWidthFull();
        brandField.setWidthFull();
        modelField.setWidthFull();
        mileageField.setWidthFull();
        nextInspectionField.setWidthFull();
        nextServiceField.setWidthFull();
        acquisitionPriceField.setWidthFull();

        // FuelType
        fuelTypeField.setItems(FuelType.values());
        fuelTypeField.setRequiredIndicatorVisible(true);
        fuelTypeField.setWidthFull();

        // PriceCategory
        priceCategoryBox.setItems(PriceCategory.values());
        priceCategoryBox.setItemLabelGenerator(pc -> pc.name() + " (" + pc.getBaseRate() + " €/Tag)");
        priceCategoryBox.setWidthFull();

        // [NEU] Standort-Auswahl
        facilityBox.setItemLabelGenerator(Facility::getAddress);
        facilityBox.setWidthFull();
        if (isBranchManager) {
            facilityBox.setItems(currentFacility);
            facilityBox.setValue(currentFacility);
            facilityBox.setReadOnly(true);
        } else {
            facilityBox.setItems(facilityService.getAllFacilities());
        }

        // Ausstattung
        smokingAllowedField.setWidthFull();
        navigationField.setWidthFull();
        airConditionField.setWidthFull();
        winterTiresField.setWidthFull();

        clearValidation();
    }

    /**
     * Setzt alle Validierungsfehler zurück.
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
        nextInspectionField.clear();
        nextServiceField.clear();
        maintenanceActiveField.setValue(false);
        acquisitionPriceField.clear();
        smokingAllowedField.setValue(false);
        navigationField.setValue(false);
        airConditionField.setValue(false);
        winterTiresField.setValue(false);
        fuelTypeField.clear();

        // [NEU] Facility zurücksetzen
        if (isBranchManager) {
            facilityBox.setValue(currentFacility);
        } else {
            facilityBox.clear();
        }
    }

    //
    // Formular <-> Entity
    //

    private void populateForm(Vehicle vehicle) {
        if (vehicle == null) {
            clearFormFields();
            clearValidation();
            return;
        }

        selectedVehicle = vehicle;

        licensePlateField.setValue(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "");
        brandField.setValue(vehicle.getBrand() != null ? vehicle.getBrand() : "");
        modelField.setValue(vehicle.getModel() != null ? vehicle.getModel() : "");
        fuelTypeField.setValue(vehicle.getFuelType());
        priceCategoryBox.setValue(vehicle.getPriceCategory());
        mileageField.setValue(String.valueOf(vehicle.getMileage()));
        nextInspectionField.setValue(vehicle.getNextInspectionDate());
        nextServiceField.setValue(vehicle.getNextServiceDate());
        maintenanceActiveField.setValue(vehicle.isMaintenanceActive());
        smokingAllowedField.setValue(vehicle.isSmokingAllowed());
        navigationField.setValue(vehicle.isHasNavigationSystem());
        airConditionField.setValue(vehicle.isHasAirCondition());
        winterTiresField.setValue(vehicle.isHasWinterTires());

        // [NEU] Standort setzen
        if (isBranchManager) {
            facilityBox.setValue(currentFacility);
        } else {
            facilityBox.setValue(vehicle.getFacility());
        }

        Double acquisitionPrice = vehicle.getAcquisitionPrice();
        if (acquisitionPrice != null) {
            acquisitionPriceField.setValue(acquisitionPrice);
        } else {
            acquisitionPriceField.clear();
        }

        clearValidation();
    }

    private void saveVehicle() {
        if (!canEdit) {
            Notification.show("Keine Berechtigung zum Speichern von Fahrzeugen.");
            return;
        }

        try {
            clearValidation();

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
            if (priceCategoryBox.isEmpty()) {
                priceCategoryBox.setInvalid(true);
                priceCategoryBox.setErrorMessage("Preiskategorie ist ein Pflichtfeld.");
                valid = false;
            }
            // [NEU] Standort ist Pflicht
            if (facilityBox.isEmpty()) {
                facilityBox.setInvalid(true);
                facilityBox.setErrorMessage("Standort ist ein Pflichtfeld.");
                valid = false;
            }

            if (!valid) {
                Notification.show(
                        "Bitte füllen Sie alle Pflichtfelder aus (Kennzeichen, Marke, Modell, Preiskategorie, Standort).",
                        4000,
                        Notification.Position.MIDDLE
                );
                return;
            }

            int mileage = parseMileage();

            if (selectedVehicle == null) {
                // Neues Fahrzeug
                Vehicle vehicle = new Vehicle(
                        licensePlateField.getValue(),
                        brandField.getValue(),
                        modelField.getValue(),
                        priceCategoryBox.getValue()
                );
                vehicle.setMileage(mileage);

                vehicle.setFuelType(fuelTypeField.getValue());

                // [NEU] Facility setzen
                vehicle.setFacility(facilityBox.getValue());

                vehicle.setNextInspectionDate(nextInspectionField.getValue());
                vehicle.setNextServiceDate(nextServiceField.getValue());
                vehicle.setMaintenanceActive(maintenanceActiveField.getValue());
                vehicle.setSmokingAllowed(smokingAllowedField.getValue());
                vehicle.setHasNavigationSystem(navigationField.getValue());
                vehicle.setHasAirCondition(airConditionField.getValue());
                vehicle.setHasWinterTires(winterTiresField.getValue());

                Double acquisitionPrice = acquisitionPriceField.getValue();
                vehicle.setAcquisitionPrice(acquisitionPrice);

                vehicleService.createVehicle(vehicle);
                Notification.show("Fahrzeug wurde angelegt.");
            } else {
                // Bestehendes Fahrzeug aktualisieren
                selectedVehicle.setLicensePlate(licensePlateField.getValue());
                selectedVehicle.setBrand(brandField.getValue());
                selectedVehicle.setModel(modelField.getValue());
                selectedVehicle.setFuelType(fuelTypeField.getValue());
                selectedVehicle.setPriceCategory(priceCategoryBox.getValue());
                selectedVehicle.setMileage(mileage);

                // [NEU] Facility setzen
                selectedVehicle.setFacility(facilityBox.getValue());

                selectedVehicle.setNextInspectionDate(nextInspectionField.getValue());
                selectedVehicle.setNextServiceDate(nextServiceField.getValue());
                selectedVehicle.setMaintenanceActive(maintenanceActiveField.getValue());
                selectedVehicle.setSmokingAllowed(smokingAllowedField.getValue());
                selectedVehicle.setHasNavigationSystem(navigationField.getValue());
                selectedVehicle.setHasAirCondition(airConditionField.getValue());
                selectedVehicle.setHasWinterTires(winterTiresField.getValue());

                Double acquisitionPrice = acquisitionPriceField.getValue();
                selectedVehicle.setAcquisitionPrice(acquisitionPrice);

                vehicleService.updateVehicle(selectedVehicle);
                Notification.show("Fahrzeug wurde aktualisiert.");
            }

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
        if (!canEdit) {
            Notification.show("Keine Berechtigung, den Fahrzeugstatus zu ändern.");
            return;
        }

        try {
            vehicleService.changeStatus(selectedVehicle.getId(), newStatus, "");
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
        // [NEU]
        facilityBox.addValueChangeListener(e -> facilityBox.setInvalid(false));
    }

    private void openCreateDialog() {
        selectedVehicle = null;
        clearFormFields();
        clearValidation();

        Dialog dialog = buildVehicleDialog("Neues Fahrzeug anlegen");
        dialog.open();
    }

    private void openEditDialog(Vehicle vehicle) {
        selectedVehicle = vehicle;
        populateForm(vehicle);
        clearValidation();

        Dialog dialog = buildVehicleDialog("Fahrzeug bearbeiten");
        dialog.open();
    }

    private Dialog buildVehicleDialog(String titleText) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3(titleText);

        // Basisdaten
        H4 basisTitle = new H4("Basisdaten");
        FormLayout basisForm = new FormLayout(
                licensePlateField,
                brandField,
                modelField,
                fuelTypeField,
                priceCategoryBox,
                // [NEU]
                facilityBox
        );
        basisForm.setWidthFull();
        basisForm.setColspan(facilityBox, 2);

        // Technische Daten
        H4 techTitle = new H4("Technische Daten");
        FormLayout techForm = new FormLayout(
                mileageField,
                acquisitionPriceField
        );
        techForm.setWidthFull();

        // Wartung & Termine
        H4 maintenanceTitle = new H4("Wartung & Termine");
        FormLayout maintenanceForm = new FormLayout(
                nextInspectionField,
                nextServiceField,
                maintenanceActiveField
        );
        maintenanceForm.setWidthFull();

        // Ausstattung
        H4 featuresTitle = new H4("Ausstattung");
        FormLayout featuresForm = new FormLayout(
                smokingAllowedField,
                navigationField,
                airConditionField,
                winterTiresField
        );
        featuresForm.setWidthFull();

        Button saveButton = new Button("Speichern", e -> {
            if (!canEdit) {
                Notification n = Notification.show("Keine Berechtigung zum Speichern.",
                        4000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                saveVehicle();
                dialog.close();
            } catch (Exception ex) {
                Notification n = Notification.show("Fehler: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Wenn keine Edit-Rechte → Speichern-Button deaktivieren
        if (!canEdit) {
            saveButton.setEnabled(false);
        }

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(
                dialogTitle,
                basisTitle, basisForm,
                techTitle, techForm,
                maintenanceTitle, maintenanceForm,
                featuresTitle, featuresForm,
                actions
        );
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidthFull();

        dialog.add(dialogLayout);
        return dialog;
    }

    private void openHistoryDialog(Vehicle vehicle) {
        if (vehicle == null || vehicle.getId() == null) {
            Notification.show("Fahrzeug ist nicht gespeichert, es existiert keine Lebenslaufakte.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("70%");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 title = new H3("Fahrzeug-Lebenslauf: " + vehicle.getLicensePlate());

        Grid<VehicleHistoryEntry> historyGrid = new Grid<>(VehicleHistoryEntry.class, false);
        historyGrid.setWidthFull();
        historyGrid.setHeight("400px");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        historyGrid.addColumn(entry ->
                        entry.getDate() != null ? entry.getDate().format(dateFormatter) : "")
                .setHeader("Datum")
                .setAutoWidth(true);

        historyGrid.addColumn(entry ->
                        entry.getEventType() != null ? mapEventType(entry.getEventType()) : "")
                .setHeader("Ereignis")
                .setAutoWidth(true);

        historyGrid.addColumn(VehicleHistoryEntry::getDescription)
                .setHeader("Beschreibung")
                .setFlexGrow(1)
                .setAutoWidth(true);


        historyGrid.addColumn(entry ->
                        entry.getSalePrice() != null ? entry.getSalePrice() + " €" : "")
                .setHeader("Verkaufspreis")
                .setAutoWidth(true);

        try {
            var entries = vehicleService.getHistoryForVehicle(vehicle.getId());
            historyGrid.setItems(entries);
        } catch (Exception ex) {
            Notification.show("Fehler beim Laden der Lebenslaufakte: " + ex.getMessage());
        }

        Button close = new Button("Schließen", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(title, historyGrid, close);
        layout.setWidthFull();
        layout.setSpacing(true);

        dialog.add(layout);
        dialog.open();
    }

    /**
     * Wandelt den technischen EventType in eine lesbare deutsche Bezeichnung um.
     */
    private String mapEventType(EventType type) {
        if (type == null) return "";
        switch (type) {
            case CREATED:
                return "Anlage";
            case UPDATED:
                return "Aktualisierung";
            case STATUS_CHANGED:
                return "Statusänderung";
            case MAINTENANCE:
                return "Wartung / HU";
            case SOLD:
                return "Verkauf";
            case SCRAPPED:
                return "Ausmusterung";
            default:
                return type.name();
        }
    }

    private void openSellDialog(Vehicle vehicle) {
        if (!canEdit) {
            Notification.show("Keine Berechtigung zum Verkaufen von Fahrzeugen.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 title = new H3("Fahrzeug verkaufen");
        H4 subtitle = new H4(vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getLicensePlate() + ")");

        TextField buyerField = new TextField("Käufer (Name / Firma)");
        buyerField.setWidthFull();

        TextField mileageField = new TextField("End-Kilometerstand");
        mileageField.setWidthFull();
        mileageField.setValue(String.valueOf(vehicle.getMileage()));

        NumberField priceField = new NumberField("Verkaufspreis (€)");
        priceField.setWidthFull();

        Button sellButton = new Button("Verkauf abschließen", e -> {
            if (!canEdit) {
                Notification.show("Keine Berechtigung zum Verkaufen von Fahrzeugen.");
                return;
            }
            try {
                if (buyerField.isEmpty() || mileageField.isEmpty() || priceField.isEmpty()) {
                    Notification.show("Bitte alle Felder ausfüllen.");
                    return;
                }

                String buyer = buyerField.getValue();
                int finalMileage = Integer.parseInt(mileageField.getValue());
                double price = priceField.getValue();

                vehicleService.sellVehicle(vehicle.getId(), price, finalMileage, buyer);

                Notification.show("Fahrzeug erfolgreich verkauft!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                refreshGrid();
                dialog.close();

            } catch (NumberFormatException ex) {
                Notification.show("Bitte gültige Zahlen eingeben.");
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        sellButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // falls keine Editrechte → Button deaktivieren
        if (!canEdit) {
            sellButton.setEnabled(false);
        }

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(
                title, subtitle,
                buyerField, mileageField, priceField,
                new HorizontalLayout(sellButton, cancelButton)
        );
        layout.setAlignItems(Alignment.STRETCH);

        dialog.add(layout);
        dialog.open();
    }
}