package bimobile.views.rentals.dialogs;

import bimobile.model.customer.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.Vehicle;
import bimobile.service.FacilityService;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog zum Bearbeiten einer bestehenden Ausleihe.
 * <p>
 * Der Dialog zeigt die relevanten Eckdaten und ermöglicht:
 * <ul>
 *   <li>Standort ändern (nur wenn kein Standort im Security-Kontext fixiert ist)</li>
 *   <li>Mietzeitraum ändern</li>
 *   <li>Preisvorschau für den geänderten Zeitraum anzeigen</li>
 * </ul>
 * <p>
 * Kunde und Fahrzeug sind bewusst read-only, um die Konsistenz zu sichern
 * (ein Wechsel würde zusätzliche Regeln erfordern: Verfügbarkeit, Rechnungs-/Historienbezug etc.)
 * @author Ben Berlin
 */
public class RentalEditDialog extends Dialog {

    private final RentalService rentalService;
    private final VehicleService vehicleService;
    private final FacilityService facilityService;
    private final Facility currentFacility;
    private final Consumer<Rental> onRentalUpdated;

    /**
     * Erstellt den Bearbeitungsdialog.
     *
     * @param rental           Ausleihe, die bearbeitet wird
     * @param rentalService    Service für Updates
     * @param vehicleService   Service für Fahrzeuge (für Preisvorschau)
     * @param facilityService  Service für Standorte
     * @param currentFacility  aktueller Standort (kann null sein)
     * @param onRentalUpdated  Callback nach erfolgreichem Speichern
     */
    public RentalEditDialog(Rental rental,
                            RentalService rentalService,
                            VehicleService vehicleService,
                            FacilityService facilityService,
                            Facility currentFacility,
                            Consumer<Rental> onRentalUpdated) {
        this.rentalService = rentalService;
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;
        this.currentFacility = currentFacility;
        this.onRentalUpdated = onRentalUpdated;

        setWidth("600px");
        setModal(true);
        setDraggable(true);

        buildLayout(rental);
    }
	/**
	 * Baut das UI und verdrahtet die Logik:
	 * <ul>
	 *   <li>Read-only Anzeige von Kunde und Fahrzeug</li>
	 *   <li>Standortauswahl abhängig von currentFacility </li>
	 *   <li>Datumsvalidierung </li>
	 *   <li>Preisvorschau, damit Änderungen schnell geprüft werden können</li>
	 * </ul>
	 *
	 * @param rental die zu bearbeitende Ausleihe
	 */
    private void buildLayout(Rental rental) {
        double oldPrice = rental.calculateTotalPrice();
        H3 dialogTitle = new H3("Ausleihe bearbeiten (#" + rental.getId() + ")");

        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(rental.getCustomer());
        customerBox.setItemLabelGenerator(Customer::getFullName);
        customerBox.setValue(rental.getCustomer());
        customerBox.setReadOnly(true); //Kunde Read only

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        vehicleBox.setItems(vehicleService.findAllVehicles());
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());
        vehicleBox.setValue(rental.getVehicle());
        vehicleBox.setReadOnly(true); //Fahrzeug Read only

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItemLabelGenerator(Facility::getAddress);

        if (currentFacility != null) {
			//Standort fixiert
            facilityBox.setItems(currentFacility);
            facilityBox.setValue(currentFacility);
            facilityBox.setReadOnly(true);
        } else {
			//nicht fix: Admin kann Standortändern
            facilityBox.setItems(facilityService.getAllFacilities());
            facilityBox.setReadOnly(false);
        }

	    facilityBox.setValue(rental.getFacility());

        if (currentFacility == null) {
	        // Standortänderung soll (bei nicht fix) die Fahrzeugliste filtern.
	        // In diesem Dialog ist vehicleBox read-only, aber das Filtern hält die Regelkette konsistent
	        // (z.B. wenn in Zukunft vehicleBox editierbar werden sollte oder für spätere Erweiterungen).
			facilityBox.addValueChangeListener(event -> {
                Facility selectedFacility = event.getValue();
                Vehicle currentVehicle = vehicleBox.getValue();

                if (selectedFacility != null) {
                    List<Vehicle> filteredVehicles = vehicleService.getVehiclesByFacility(selectedFacility.getId());
                    vehicleBox.setItems(filteredVehicles);
					//Wenn das aktuelle Fahrzeug weiterhin passt, bleibt es ausgewählt.
                    if (currentVehicle != null && currentVehicle.getFacility() != null
                            && currentVehicle.getFacility().getId().equals(selectedFacility.getId())) {
                        vehicleBox.setValue(currentVehicle);
                    } else if (currentVehicle != null) {
                        // Blocken die Änderung, damit das Fahrzeug nicht verloren geht.
                        Notification.show("Standort passt nicht zum Fahrzeug. Auswahl bleibt bestehen.")
                                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        facilityBox.setValue(currentVehicle.getFacility());
                    }
                } else {
					//Admin sicht
                    vehicleBox.setItems(vehicleService.findAllVehicles());
                    vehicleBox.setValue(currentVehicle);
                }
            });
			// Falls zukünftig vehicleBox editierbar wird, wird der Standort passend nachgezogen.
            vehicleBox.addValueChangeListener(event -> {
                Vehicle selectedVehicle = event.getValue();
                if (selectedVehicle != null && selectedVehicle.getFacility() != null) {
                    if (!selectedVehicle.getFacility().equals(facilityBox.getValue())) {
                        facilityBox.setValue(selectedVehicle.getFacility());
                    }
                }
            });
        }

        DatePicker startDate = new DatePicker("Startdatum");
        startDate.setValue(rental.getStartDate());

        DatePicker endDate = new DatePicker("Enddatum");
        endDate.setValue(rental.getEndDate());

        TextField totalRateField = new TextField("Gesamtrate (Vorschau) €");
        totalRateField.setReadOnly(true);

		// Vorschau: basiert auf Tagesrate und Dauer.
	    Runnable recalcTotal = () -> RentalDialogSupport.updateTotalRatePreview(
			    vehicleBox.getValue(),
			    startDate.getValue(),
			    endDate.getValue(),
			    totalRateField
	    );
        recalcTotal.run();
		// Bei änderungen neu berechnen
        startDate.addValueChangeListener(e -> recalcTotal.run());
        endDate.addValueChangeListener(e -> recalcTotal.run());
        vehicleBox.addValueChangeListener(e -> recalcTotal.run());

	    RentalDialogSupport.enforceDateOrder(startDate, endDate);

        Button save = new Button("Speichern", e -> {
            try {
                if (facilityBox.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                    Notification.show("Bitte Standort sowie Start- und Enddatum vollständig ausfüllen.")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                Rental updated = rentalService.updateRental(
                        rental,
                        facilityBox.getValue(),
                        startDate.getValue(),
                        endDate.getValue()
                );
                double newPrice = updated.calculateTotalPrice();

                Notification.show("Ausleihe #" + updated.getId() + " erfolgreich aktualisiert.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                onRentalUpdated.accept(updated);
                if (oldPrice != newPrice) {
                    // Hinweis auf Preisänderung für schnelle Kontrolle im Büroalltag.
                    Notification.show("Preis geändert: " + oldPrice + " € → " + newPrice + " €")
                            .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                }
                close();

            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(customerBox, vehicleBox, facilityBox, startDate, endDate, totalRateField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
        add(layout);
    }
}
