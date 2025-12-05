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
import com.vaadin.flow.component.grid.GridVariant;
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
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

	/**
	 * Grid zur Anzeige aller Ausleihen.
	 */
	private final Grid<Rental> grid = new Grid<>(Rental.class, false);

	/**
	 * Grid zur Anzeige des Änderungsprotokolls für Ausleihvorgänge.
	 */
	private final Grid<ChangeLogEntry> changeLogGrid = new Grid<>(ChangeLogEntry.class, false);

	/**
	 * In-Memory-Liste der Change-Log-Einträge für die aktuelle Session.
	 */
	private final List<ChangeLogEntry> changeLogEntries = new ArrayList<>();

	/**
	 * Erstellt die Ausleihübersicht und initialisiert Layout, Grid und Aktionen.
	 *
	 * @param rentalService   Service für Ausleihoperationen
	 * @param customerService Service zum Laden von Kunden
	 * @param vehicleService  Service zum Laden von Fahrzeugen
	 * @param facilityService Service zum Laden von Standorten
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

		// Button zum Anlegen einer neuen Ausleihe
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

		// Initiale Datenladung
		updateGrid();

		// Übersicht + Änderungsprotokoll in Layout einbinden
		add(header, grid, buildChangeLogSection());
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
	 * - Pflichtfelder müssen befüllt sein
	 * - Enddatum muss nach Startdatum liegen
	 * - Fahrzeug darf nicht belegt sein (Prüfung im Service)
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
		vehicleBox.setItems(vehicleService.findAllVehicles());
		vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());

		ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
		facilityBox.setItems(facilityService.getAllFacilities());
		facilityBox.setItemLabelGenerator(Facility::getAddress);

		DatePicker startDate = new DatePicker("Startdatum");
		DatePicker endDate = new DatePicker("Enddatum");

		// Stellt sicher, dass Enddatum nicht vor Startdatum gewählt werden kann
		enforceDateOrder(startDate, endDate);

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
				addChangeLogEntry(
						"admin@bi-mobile.de",
						"Ausleihe erstellt",
						"Ausleihe #" + rental.getId() + " angelegt",
						"Erfolgreich"
				);
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
		vehicleBox.setItems(vehicleService.findAllVehicles());
		vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());
		vehicleBox.setValue(rental.getVehicle());
		vehicleBox.setReadOnly(true);

		ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
		facilityBox.setItems(facilityService.getAllFacilities());
		facilityBox.setItemLabelGenerator(Facility::getAddress);
		facilityBox.setValue(rental.getFacility());

		DatePicker startDate = new DatePicker("Startdatum");
		startDate.setValue(rental.getStartDate());

		DatePicker endDate = new DatePicker("Enddatum");
		endDate.setValue(rental.getEndDate());

		// Validierung der zeitlichen Reihenfolge
		enforceDateOrder(startDate, endDate);

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
				addChangeLogEntry(
						"admin@bi-mobile.de",
						"Ausleihe aktualisiert",
						"Ausleihe #" + updated.getId() + " angepasst",
						"Erfolgreich"
				);
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
				addChangeLogEntry(
						"admin@bi-mobile.de",
						"Ausleihe gelöscht",
						"Ausleihe #" + rental.getId() + " entfernt",
						"Erfolgreich"
				);
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

	/**
	 * Erzwingt eine konsistente Reihenfolge der Datumsangaben für
	 * Start- und Enddatum einer Ausleihe.
	 *
	 * - Das minimale Enddatum wird auf das aktuell gesetzte Startdatum gesetzt.
	 * - Wird das Startdatum nachträglich geändert und liegt das bestehende Enddatum davor,
	 *   wird das Enddatum zurückgesetzt (geleert).
	 *
	 * @param startDate DatePicker für das Startdatum
	 * @param endDate   DatePicker für das Enddatum
	 */
	private void enforceDateOrder(DatePicker startDate, DatePicker endDate) {
		// Initiales Minimum für das Enddatum anhand des aktuellen Startdatums setzen
		endDate.setMin(startDate.getValue());

		startDate.addValueChangeListener(event -> {
			LocalDate start = event.getValue();
			endDate.setMin(start);

			// Falls das bestehende Enddatum vor dem neuen Startdatum liegt, zurücksetzen
			if (start != null && endDate.getValue() != null && endDate.getValue().isBefore(start)) {
				endDate.clear();
			}
		});
	}

	/**
	 * Erstellt den Bereich für das Änderungsprotokoll unterhalb der Ausleihübersicht.
	 *
	 * Konfiguriert das Grid für {@link ChangeLogEntry}, setzt Spalten, Layout und Styling
	 * und bindet die aktuelle Liste {@link #changeLogEntries} als Datenquelle.
	 *
	 * @return ein fertig konfiguriertes Layout mit Überschrift und Change-Log-Grid
	 */
	private VerticalLayout buildChangeLogSection() {
		changeLogGrid.addColumn(entry -> entry.timestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
				.setHeader("Datum/Uhrzeit").setAutoWidth(true);
		changeLogGrid.addColumn(ChangeLogEntry::user).setHeader("Benutzer").setAutoWidth(true);
		changeLogGrid.addColumn(ChangeLogEntry::action).setHeader("Aktion").setAutoWidth(true);
		changeLogGrid.addColumn(ChangeLogEntry::details).setHeader("Details").setAutoWidth(true);
		changeLogGrid.addColumn(ChangeLogEntry::status).setHeader("Status").setAutoWidth(true);

		// Visuelles Styling für ein reduziertes, flaches Grid
		changeLogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		changeLogGrid.setItems(changeLogEntries);

		VerticalLayout changeLogLayout = new VerticalLayout();
		changeLogLayout.setPadding(true);
		changeLogLayout.setWidthFull();
		changeLogLayout.getStyle().set("background", "#ffffff");
		changeLogLayout.getStyle().set("border-radius", "8px");
		changeLogLayout.getStyle().set("box-shadow", "0 2px 6px rgba(0,0,0,0.05)");

		changeLogLayout.add(new H3("Änderungsprotokoll"), changeLogGrid);
		return changeLogLayout;
	}

	/**
	 * Fügt einen neuen Eintrag zum Änderungsprotokoll hinzu und aktualisiert die Anzeige.
	 *
	 * Der Eintrag wird am Anfang der Liste eingefügt, sodass die jüngsten Änderungen
	 * im Grid oben stehen.
	 *
	 * @param user    Benutzerkennung, die die Aktion ausgelöst hat (z. B. E-Mail-Adresse)
	 * @param action  Kurzbeschreibung der Aktion (z. B. "Ausleihe erstellt")
	 * @param details Detailbeschreibung der Änderung
	 * @param status  Ergebnisstatus (z. B. "Erfolgreich" oder "Fehlgeschlagen")
	 */
	private void addChangeLogEntry(String user, String action, String details, String status) {
		// Eintrag mit aktuellem Zeitstempel an den Anfang der Liste setzen
		changeLogEntries.add(0, new ChangeLogEntry(LocalDateTime.now(), user, action, details, status));
		// Grid-Datenprovider aktualisieren, damit Änderungen direkt sichtbar werden
		changeLogGrid.getDataProvider().refreshAll();
	}

	/**
	 * Interner Datentyp für Einträge im Änderungsprotokoll.
	 *
	 * Speichert Zeitstempel, Benutzer, Aktion, Detailbeschreibung und Status eines
	 * Vorgangs in der Ausleihverwaltung.
	 *
	 * @param timestamp Zeitpunkt der Aktion
	 * @param user      auslösender Benutzer
	 * @param action    Kurzbeschreibung der Aktion
	 * @param details   Detailinformation zur Aktion
	 * @param status    Ergebnisstatus der Aktion
	 */
	private record ChangeLogEntry(LocalDateTime timestamp, String user, String action, String details, String status) {
	}
}
