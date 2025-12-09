package bimobile.views;

import bimobile.model.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import bimobile.model.User;
import bimobile.model.Vehicle;
import bimobile.service.RentalChangeLogService;
import bimobile.service.RentalService;
import bimobile.service.CustomerService;
import bimobile.service.VehicleService;
import bimobile.service.FacilityService;
import bimobile.dao.UserRepository;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Übersicht aller Ausleihen im BI-Mobile System.
 * <p>
 * Diese View stellt ein Grid zur Anzeige aller Ausleihen bereit und ermöglicht deren Verwaltung:
 *  - Anlegen neuer Ausleihen
 *  - Bearbeiten bestehender Ausleihen
 *  - Löschen von Ausleihen
 *  - Übersicht aller Änderungen an den Ausleihen
 * <p>
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

	/**
	 * Erstellt die Ausleihübersicht und initialisiert Layout, Grid und Aktionen.
	 *
	 * @param rentalService   Service für Ausleihoperationen
	 * @param customerService Service zum Laden von Kunden
	 * @param vehicleService  Service zum Laden von Fahrzeugen
	 * @param facilityService Service zum Laden von Standorten
     * @param changeLogService Service zum Speichern des Änderungsprotokolls
     * @param userRepository Repository zum Auflösen des angemeldeten Benutzers
	 */
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

        searchField = new TextField();
        searchField.setPlaceholder("Suche nach Kunde, Geburtstag oder Kennzeichen");
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

            Button zurueckgeben = new Button(new Icon(VaadinIcon.CHECK));
            zurueckgeben.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            zurueckgeben.addClickListener(e -> {
                try {
                    rentalService.returnRental(rental); // setzt Status COMPLETED + erstellt Rechnung
                    Notification.show("Ausleihe #" + rental.getId() + " zurückgegeben.")
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    logChange(rental, "Ausleihe zurückgegeben", "Ausleihe #" + rental.getId() + " abgeschlossen");
                    updateGrid();
                } catch (Exception ex) {
                    Notification.show("Fehler beim Zurückgeben der Ausleihe.")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    ex.printStackTrace();
                }
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
	 * Lädt alle Ausleihen aus dem {@link RentalService} und
	 * aktualisiert das Grid.
	 *
	 * Wird nach jeder Änderungsoperation aufgerufen.
	 */
    private void updateGrid() {
        allRentals.clear();
        allRentals.addAll(rentalService.findAllWithCustomerVehicleFacility());
        applyFilter(currentFilter);
        refreshChangeLog();
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
                logChange(updated, "Ausleihe aktualisiert", "Ausleihe #" + updated.getId() + " angepasst");
                updateGrid();
                dialog.close();

            } catch (IllegalStateException ex) {
                Notification.show("Validierungsfehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
     * Konfiguriert das Grid für {@link RentalChangeLog}, setzt Spalten, Layout und Styling
     * und bindet alle persistierten Einträge als Datenquelle.
     *
     * @return ein fertig konfiguriertes Layout mit Überschrift und Change-Log-Grid
     */
    private VerticalLayout buildChangeLogSection() {
        changeLogGrid.addColumn(entry -> entry.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .setHeader("Datum/Uhrzeit").setAutoWidth(true);
        changeLogGrid.addColumn(entry -> entry.getRental().getId()).setHeader("Ausleihe").setAutoWidth(true);
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

    private void applyFilter(String filterText) {
        currentFilter = filterText != null ? filterText.trim() : "";
        if (currentFilter.isBlank()) {
            grid.setItems(new ArrayList<>(allRentals));
            return;
        }

        String lowered = currentFilter.toLowerCase();
        LocalDate dateFilter = parseDate(currentFilter);

        grid.setItems(allRentals.stream()
                .filter(rental -> matchesFilter(rental, lowered, dateFilter))
                .collect(Collectors.toList()));
    }

    private boolean matchesFilter(Rental rental, String lowered, LocalDate dateFilter) {
        if (String.valueOf(rental.getId()).contains(lowered)) {
            return true;
        }

        if (rental.getCustomer() != null) {
            if ((rental.getCustomer().getLastName() != null && rental.getCustomer().getLastName().toLowerCase().contains(lowered)) ||
                    (rental.getCustomer().getFirstName() != null && rental.getCustomer().getFirstName().toLowerCase().contains(lowered)) ||
                    (rental.getCustomer().getEmail() != null && rental.getCustomer().getEmail().toLowerCase().contains(lowered))) {
                return true;
            }
            if (dateFilter != null && dateFilter.equals(rental.getCustomer().getBirthday())) {
                return true;
            }
        }

        return rental.getVehicle() != null && rental.getVehicle().getLicensePlate() != null &&
                rental.getVehicle().getLicensePlate().toLowerCase().contains(lowered);
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }
}

