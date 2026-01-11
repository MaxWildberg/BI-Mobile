package bimobile.views.rentals.dialogs;

import bimobile.model.Rental;
import bimobile.service.RentalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.time.format.DateTimeFormatter;

/**
 * Dialog für die Rückgabe einer Ausleihe.
 * <p>
 *
 * Der Dialog zeigt die wichtigsten Informationen der Ausleihe an (Read-Only) und
 * der Kilometerstand wird erfasst.
 * Danach wird die Rückgabe im System abgeschlossen.
 *
 * @author Ben Berlin
 */
public class RentalReturnDialog extends Dialog {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final RentalService rentalService;
	private final Runnable onReturnSuccess;

	/**
	 * Erstellt den Rückgabedialog.
	 *
	 * @param rental          die zurückzugebende Ausleihe
	 * @param rentalService   Service für Rückgabe-Logik
	 * @param onReturnSuccess Callback nach erfolgreicher Rückgabe
	 */
	public RentalReturnDialog(Rental rental,
	                          RentalService rentalService,
	                          Runnable onReturnSuccess) {
		this.rentalService = rentalService;
		this.onReturnSuccess = onReturnSuccess;

		setWidth("480px");
		setModal(true);
		setDraggable(true);

		buildLayout(rental);
	}
	/**
	 * Baut das UI-Layout (Info-Block + Eingabefeld + Aktionen).
	 * <p>
	 * Enthält:
	 * <ul>
	 *   <li>Übersichtsdaten zur Ausleihe (read-only)</li>
	 *   <li>Eingabe des End-Kilometerstands mit Mindestwert = aktueller Kilometerstand</li>
	 *   <li>Bestätigen/Abbrechen Buttons</li>
	 * </ul>
	 *
	 * @param rental die zu bearbeitende Ausleihe
	 */
	private void buildLayout(Rental rental) {
		H3 dialogTitle = new H3("Ausleihe zurückgeben (#" + rental.getId() + ")");

		//Bais für KM check (Rückgabe KM darf nicht unter gespeicherten KM-Stand liegen)
		int currentMileage = rental.getVehicle().getMileage();

		FormLayout infoLayout = new FormLayout();
		infoLayout.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1),
				new FormLayout.ResponsiveStep("450px", 2)
		);
		infoLayout.add(
				buildInfoRow("Status", rental.getStatus().name()),
				buildInfoRow("Kunde", rental.getCustomer() != null ? rental.getCustomer().getFullName() : "-"),
				buildInfoRow("Fahrzeug", rental.getVehicle() != null ? rental.getVehicle().getLicensePlate() : "-"),
				buildInfoRow("Standort", rental.getFacility() != null ? rental.getFacility().getAddress() : "–"),
				buildInfoRow("Startdatum",
						rental.getStartDate() != null ? rental.getStartDate().format(DATE_FORMATTER) : "-"),
				buildInfoRow("Enddatum",
						rental.getEndDate() != null ? rental.getEndDate().format(DATE_FORMATTER) : "-"),
				buildInfoRow("Aktueller Kilometerstand", currentMileage + " km")
		);

		IntegerField endMileageField = new IntegerField("Kilometerstand bei Rückgabe");
		endMileageField.setRequiredIndicatorVisible(true);
		endMileageField.setHelperText("Aktueller Stand: " + currentMileage + " km");
		// UI-Plausibilitätsregel:
		// Der Rückgabe-KM darf nicht kleiner sein als der aktuell gespeicherte Stand.
		endMileageField.setMin(currentMileage);
		endMileageField.setStepButtonsVisible(true);
		endMileageField.setAutoselect(true);

		Button confirm = new Button("Zurückgeben", event -> {
			try {
				if (endMileageField.isEmpty()) {
					Notification.show("Bitte einen Kilometerstand angeben.")
							.addThemeVariants(NotificationVariant.LUMO_ERROR);
					return;
				}
				int endMileage = endMileageField.getValue();
				//Abschluss der Ausleihe
				rentalService.returnRental(rental, endMileage);
				Notification.show("Ausleihe #" + rental.getId() + " zurückgegeben.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				onReturnSuccess.run();
				close();
			} catch (Exception ex) {
				Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

		Button cancel = new Button("Abbrechen", e -> close());
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout actions = new HorizontalLayout(confirm, cancel);
		actions.setWidthFull();
		actions.setJustifyContentMode(JustifyContentMode.END);

		VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, infoLayout, endMileageField, actions);
		dialogLayout.setAlignItems(Alignment.STRETCH);
		add(dialogLayout);
	}
	/**
	 * Baut eine kompakte Darstellungszeile für ein Label-Wert-Paar (read-only).
	 * <p>
	 * Dient zur schnellen visuellen Erfassung der Ausleihdaten im Dialog.
	 *
	 * @param label Feldname
	 * @param value Feldwert
	 * @return Layout-Block für die Anzeige im {@link FormLayout}
	 */
	private VerticalLayout buildInfoRow(String label, String value) {
		Span headline = new Span(label);
		headline.getStyle().set("font-weight", "600");
		Span content = new Span(value != null ? value : "-");
		content.getStyle().set("color", "var(--lumo-secondary-text-color)");
		VerticalLayout row = new VerticalLayout(headline, content);
		row.setPadding(false);
		row.setSpacing(false);
		return row;
	}
}