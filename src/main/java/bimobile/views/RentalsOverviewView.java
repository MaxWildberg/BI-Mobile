package bimobile.views;

import bimobile.dao.UserRepository;
import bimobile.model.customer.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import bimobile.model.User;
import bimobile.model.Vehicle;
import bimobile.service.CustomerService;
import bimobile.service.FacilityService;
import bimobile.service.RentalChangeLogService;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Übersicht aller Ausleihen im BI-Mobile System.
 * <p>
 * Diese View fungiert als Schaltzentrale für die Mietvorgänge: hier werden Datensätze
 * angelegt, angepasst, zurückgegeben oder gelöscht. Um den Lerncharakter zu unterstreichen,
 * sind alle Komponenten klar strukturiert und nachvollziehbar kommentiert.
 * Funktionen:
 *  <ul>
 *      <li>Anlegen, Bearbeiten, Löschen von Ausleihen</li>
 *      <li>Zurückgeben inkl. Rechnungs-Erstellung (über RentalService)</li>
 *      <li>Suchfeld für Kunde (Vor-/Nachname, Vollname) und Kennzeichen</li>
 *      <li>Persistentes Änderungsprotokoll über RentalChangeLog</li>
 *  </ul>
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
	private final RentalChangeLogService changeLogService;
	private final UserRepository userRepository;

	/**
	 * Grid zur Anzeige aller Ausleihen.
	 */
	private final Grid<Rental> grid = new Grid<>(Rental.class, false);

	/**
	 * Grid zur Anzeige des Änderungsprotokolls für Ausleihvorgänge.
	 */
	private final Grid<RentalChangeLog> changeLogGrid = new Grid<>(RentalChangeLog.class, false);

	private final List<Rental> allRentals = new ArrayList<>();
	private TextField searchField;
	private String currentFilter = "";

	public RentalsOverviewView(RentalService rentalService,
	                           CustomerService customerService,
	                           VehicleService vehicleService,
	                           FacilityService facilityService,
	                           RentalChangeLogService changeLogService,
	                           UserRepository userRepository) {

		this.rentalService = rentalService;
		this.customerService = customerService;
		this.vehicleService = vehicleService;
		this.facilityService = facilityService;
		this.changeLogService = changeLogService;
		this.userRepository = userRepository;

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

		// Suchfeld (ohne Geburtstag)
		searchField = new TextField();
		searchField.setPlaceholder("Suche nach Kunde oder Kennzeichen");
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(event -> applyFilter(event.getValue()));

		HorizontalLayout header = new HorizontalLayout(title, searchField, neu);
		header.setWidthFull();
		header.setAlignItems(Alignment.CENTER);
		header.setJustifyContentMode(JustifyContentMode.BETWEEN);
		header.setFlexGrow(1, title);
		header.setFlexGrow(2, searchField);

		// Grid-Konfiguration
		grid.addColumn(Rental::getId).setHeader("ID").setAutoWidth(true);
		grid.addColumn(r -> r.getCustomer().getFullName()).setHeader("Kunde").setAutoWidth(true);
		grid.addColumn(r -> r.getVehicle().getLicensePlate()).setHeader("Fahrzeug").setAutoWidth(true);
		grid.addColumn(Rental::getStartDate).setHeader("Startdatum").setAutoWidth(true);
		grid.addColumn(Rental::getEndDate).setHeader("Enddatum").setAutoWidth(true);
		grid.addColumn(Rental::calculateTotalPrice).setHeader("Gesamtpreis (€)").setAutoWidth(true);
		grid.addColumn(r -> r.getStatus().name()).setHeader("Status").setAutoWidth(true);

		// Aktionsspalte (Bearbeiten / Löschen / Zurückgeben)
		grid.addComponentColumn(rental -> {
			Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
			bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			bearbeiten.addClickListener(e -> openEditDialog(rental));

			Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
			loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
			loeschen.addClickListener(e -> openDeleteDialog(rental));

			Button zurueckgeben = new Button(new Icon(VaadinIcon.CHECK));
			zurueckgeben.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
			zurueckgeben.addClickListener(e -> {
				openReturnDialog(rental);
			});
			return new HorizontalLayout(bearbeiten, loeschen, zurueckgeben);

		}).setHeader("Aktionen");

		// Initiale Datenladung
		updateGrid();

		// Übersicht + Änderungsprotokoll in Layout einbinden
		add(header, grid, buildChangeLogSection());
		setFlexGrow(1, grid);
	}

	/**
	 * Lädt alle Ausleihen und aktualisiert Grid + ChangeLog.
	 */
	private void updateGrid() {
		allRentals.clear();
		allRentals.addAll(rentalService.findAllWithCustomerVehicleFacility());
		applyFilter(currentFilter);
		refreshChangeLog();
	}

	// ------------------------------------------------------------------------------------
	// Dialog: Neue Ausleihe
	// ------------------------------------------------------------------------------------
	private void openCreateDialog() {
		Dialog dialog = new Dialog();
		dialog.setWidth("600px");
		dialog.setModal(true);
		dialog.setDraggable(true);

		H3 dialogTitle = new H3("Neue Ausleihe anlegen");

		ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
		customerBox.setItems(customerService.findAllCustomers());
		customerBox.setItemLabelGenerator(Customer::getFullName);

		ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
		vehicleBox.setItems(vehicleService.findAllVehicles());
		vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());

		ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
		facilityBox.setItems(facilityService.getAllFacilities());
		facilityBox.setItemLabelGenerator(Facility::getAddress);

		DatePicker startDate = new DatePicker("Startdatum");
		DatePicker endDate = new DatePicker("Enddatum");

		// Vorschau der Gesamtrate
		TextField totalRateField = new TextField("Gesamtrate (Vorschau) €");
		totalRateField.setReadOnly(true);

		Runnable recalcTotal = () -> {
			Vehicle v = vehicleBox.getValue();
			LocalDate start = startDate.getValue();
			LocalDate end = endDate.getValue();
			if (v != null && start != null && end != null) {
				long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
				double dailyRate = v.getPriceCategory().getBaseRate();
				totalRateField.setValue(String.valueOf(dailyRate * days));
			} else {
				totalRateField.clear();
			}
		};

		// Listener für Änderungen
		vehicleBox.addValueChangeListener(e -> recalcTotal.run());
		startDate.addValueChangeListener(e -> recalcTotal.run());
		endDate.addValueChangeListener(e -> recalcTotal.run());

		// Enddatum >= Startdatum erzwingen
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
				logChange(rental, "Ausleihe erstellt", "Ausleihe #" + rental.getId() + " angelegt");
				updateGrid();
				dialog.close();

			} catch (IllegalStateException ex) {
				Notification.show("Validierungsfehler: " + ex.getMessage())
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
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

		FormLayout form = new FormLayout(customerBox, vehicleBox, facilityBox, startDate, endDate, totalRateField);
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
	private void openReturnDialog(Rental rental) {
		Dialog dialog = new Dialog();
		dialog.setWidth("480px");
		dialog.setModal(true);
		dialog.setDraggable(true);

		H3 dialogTitle = new H3("Ausleihe zurückgeben (#" + rental.getId() + ")");

		int currentMileage = rental.getVehicle().getMileage();

		IntegerField endMileageField = new IntegerField("Kilometerstand bei Rückgabe");
		endMileageField.setRequiredIndicatorVisible(true);
		endMileageField.setHelperText("Aktueller Stand: " + currentMileage + " km");
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
				rentalService.returnRental(rental, endMileage);
				Notification.show("Ausleihe #" + rental.getId() + " zurückgegeben.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				updateGrid();
				dialog.close();
			} catch (IllegalArgumentException ex) {
				Notification.show(ex.getMessage())
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (Exception ex) {
				Notification.show("Fehler beim Zurückgeben der Ausleihe.")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

		Button cancel = new Button("Abbrechen", e -> dialog.close());
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout actions = new HorizontalLayout(confirm, cancel);
		actions.setWidthFull();
		actions.setJustifyContentMode(JustifyContentMode.END);

		VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, endMileageField, actions);
		dialog.add(dialogLayout);
		dialog.open();
	}

	// ------------------------------------------------------------------------------------
	// Dialog: Ausleihe bearbeiten
	// ------------------------------------------------------------------------------------
	private void openEditDialog(Rental rental) {
		double oldPrice = rental.calculateTotalPrice();
		Dialog dialog = new Dialog();
		dialog.setWidth("600px");
		dialog.setModal(true);
		dialog.setDraggable(true);

		H3 dialogTitle = new H3("Ausleihe bearbeiten (#" + rental.getId() + ")");

		// Kunde & Fahrzeug nur lesbar anzeigen
		ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
		customerBox.setItems(customerService.findAllCustomers());
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

		// Vorschau der Gesamtrate
		TextField totalRateField = new TextField("Gesamtrate (Vorschau) €");
		totalRateField.setReadOnly(true);

		Runnable recalcTotal = () -> {
			Vehicle v = vehicleBox.getValue();
			LocalDate start = startDate.getValue();
			LocalDate end = endDate.getValue();
			if (v != null && start != null && end != null) {
				long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
				double dailyRate = v.getPriceCategory().getBaseRate();
				totalRateField.setValue(String.valueOf(dailyRate * days));
			} else {
				totalRateField.clear();
			}
		};

		vehicleBox.addValueChangeListener(e -> recalcTotal.run());
		startDate.addValueChangeListener(e -> recalcTotal.run());
		endDate.addValueChangeListener(e -> recalcTotal.run());
		// Initiale Anzeige
		recalcTotal.run();

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
				double newPrice = updated.calculateTotalPrice();

				Notification.show("Ausleihe #" + updated.getId() + " erfolgreich aktualisiert.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				logChange(updated,
						"Ausleihe aktualisiert",
						"Ausleihe #" + updated.getId() + " angepasst (Preis: " + oldPrice + " € -> " + newPrice + " €)"
				);
				updateGrid();
				dialog.close();

			} catch (IllegalStateException ex) {
				Notification.show("Validierungsfehler: " + ex.getMessage())
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (IllegalArgumentException ex) {
				Notification.show("Fehler: " + ex.getMessage())
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (Exception ex) {
				ex.printStackTrace();
				Notification.show("Unerwarteter Fehler bei der Aktualisierung.")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button cancel = new Button("Abbrechen", e -> dialog.close());
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		FormLayout form = new FormLayout(customerBox, vehicleBox, facilityBox, startDate, endDate, totalRateField);
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

	// ------------------------------------------------------------------------------------
	// Dialog: Löschen
	// ------------------------------------------------------------------------------------
	private void openDeleteDialog(Rental rental) {
		Dialog dialog = new Dialog();
		dialog.setWidth("400px");

		H3 dialogTitle = new H3("Ausleihe löschen?");

		VerticalLayout content = new VerticalLayout();
		content.add(new Paragraph("Möchten Sie die Ausleihe wirklich löschen?"));
		content.add(new Paragraph("Kunde: " + rental.getCustomer().getFullName()));
		content.add(new Paragraph("Fahrzeug: " + rental.getVehicle().getLicensePlate()));
		content.add(new Paragraph("Zeitraum: " + rental.getStartDate() + " bis " + rental.getEndDate()));

		Button confirmButton = new Button("Löschen", e -> {
			try {
				logChange(rental, "Ausleihe gelöscht", "Ausleihe #" + rental.getId() + " entfernt");
				changeLogService.detachRental(rental);
				rentalService.deleteRental(rental);
				Notification.show("Ausleihe erfolgreich gelöscht.")
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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

	// ------------------------------------------------------------------------------------
	// Datumskonsistenz
	// ------------------------------------------------------------------------------------
	private void enforceDateOrder(DatePicker startDate, DatePicker endDate) {
		endDate.setMin(startDate.getValue());

		startDate.addValueChangeListener(event -> {
			LocalDate start = event.getValue();
			endDate.setMin(start);

			if (start != null && endDate.getValue() != null && endDate.getValue().isBefore(start)) {
				endDate.clear();
			}
		});
	}

	// ------------------------------------------------------------------------------------
	// ChangeLog-Section
	// ------------------------------------------------------------------------------------
	private VerticalLayout buildChangeLogSection() {
		changeLogGrid.addColumn(entry ->
						entry.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
				.setHeader("Datum/Uhrzeit").setAutoWidth(true);

		changeLogGrid.addColumn(entry -> {
			if (entry.getRental() != null) {
				return entry.getRental().getId();
			} else {
				return entry.getRentalIdSnapshot();
			}
		}).setHeader("Ausleihe").setAutoWidth(true);

		changeLogGrid.addColumn(RentalChangeLog::getUserIdentifier).setHeader("Benutzer").setAutoWidth(true);
		changeLogGrid.addColumn(RentalChangeLog::getAction).setHeader("Aktion").setAutoWidth(true);
		changeLogGrid.addColumn(RentalChangeLog::getDetails).setHeader("Details").setAutoWidth(true);

		changeLogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		changeLogGrid.setItems(changeLogService.getAllEntries());

		VerticalLayout changeLogLayout = new VerticalLayout();
		changeLogLayout.setPadding(true);
		changeLogLayout.setWidthFull();
		changeLogLayout.getStyle().set("background", "#ffffff");
		changeLogLayout.getStyle().set("border-radius", "8px");
		changeLogLayout.getStyle().set("box-shadow", "0 2px 6px rgba(0,0,0,0.05)");

		changeLogLayout.add(new H3("Änderungsprotokoll"), changeLogGrid);
		return changeLogLayout;
	}

	private void refreshChangeLog() {
		changeLogGrid.setItems(changeLogService.getAllEntries());
	}

	private void logChange(Rental rental, String action, String details) {
		String user = resolveCurrentUser();
		changeLogService.logChange(rental, user, action, details);
		refreshChangeLog();
	}

	private String resolveCurrentUser() {
		String principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.map(auth -> auth.getName())
				.orElse("Unbekannt");
		return userRepository.findByEmail(principal)
				.map(User::getFullName)
				.orElse(principal);
	}

	// ------------------------------------------------------------------------------------
	// Suche
	// ------------------------------------------------------------------------------------
	private void applyFilter(String filterText) {
		currentFilter = filterText != null ? filterText.trim() : "";
		if (currentFilter.isBlank()) {
			grid.setItems(new ArrayList<>(allRentals));
			return;
		}

		String lowered = currentFilter.toLowerCase();

		grid.setItems(allRentals.stream()
				.filter(rental -> matchesFilter(rental, lowered))
				.collect(Collectors.toList()));
	}

	private boolean matchesFilter(Rental rental, String lowered) {
		// ID
		if (String.valueOf(rental.getId()).contains(lowered)) {
			return true;
		}

		// Kunde: Vorname, Nachname, Vollname
		if (rental.getCustomer() != null) {
			String first = rental.getCustomer().getPersonalData().getFirstname() != null
					? rental.getCustomer().getPersonalData().getFirstname().toLowerCase() : "";
			String last = rental.getCustomer().getPersonalData().getLastname() != null
					? rental.getCustomer().getPersonalData().getLastname().toLowerCase() : "";
			String fullName = (first + " " + last).trim();

			if (first.contains(lowered) || last.contains(lowered) || fullName.contains(lowered)) {
				return true;
			}
		}

		// Kennzeichen
		return rental.getVehicle() != null
				&& rental.getVehicle().getLicensePlate() != null
				&& rental.getVehicle().getLicensePlate().toLowerCase().contains(lowered);
	}
}
