package bimobile.views.rentals.dialogs;

import bimobile.model.Vehicle;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

/**
 * Gemeinsame Hilfslogik für Ausleih-Dialoge.
 * <p>
 * Diese Klasse bündelt wiederkehrende UI-Regeln
 * wie Datumsvalidierung und Preisvorschau.
 *
 * @author Ben Berlin
 */
final class RentalDialogSupport {

	private RentalDialogSupport() {
	}

	/**
	 * Hält die Datumsauswahl konsistent, damit Enddatum nie vor dem Start liegt.
	 *
	 * @param startDate Startdatum
	 * @param endDate   Enddatum
	 */
	static void enforceDateOrder(DatePicker startDate, DatePicker endDate) {
		endDate.setMin(startDate.getValue());
		startDate.addValueChangeListener(event -> {
			LocalDate start = event.getValue();
			endDate.setMin(start);
			if (start != null && endDate.getValue() != null && endDate.getValue().isBefore(start)) {
				endDate.clear();
			}
		});
	}

	/**
	 * Aktualisiert die Preisvorschau anhand von Fahrzeug und Datumsbereich.
	 *
	 * @param vehicle        ausgewähltes Fahrzeug
	 * @param startDate      Startdatum
	 * @param endDate        Enddatum
	 * @param totalRateField Textfeld für die Preisvorschau
	 */
	static void updateTotalRatePreview(Vehicle vehicle,
	                                   LocalDate startDate,
	                                   LocalDate endDate,
	                                   TextField totalRateField) {
		if (vehicle != null && startDate != null && endDate != null) {
			long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
			//min. 1 Tag
			if (days <= 0) {
				days = 1;
			}
			double dailyRate = vehicle.getDailyRate();
			totalRateField.setValue(String.valueOf(dailyRate * days));
		} else {
			totalRateField.clear();
		}
	}
}