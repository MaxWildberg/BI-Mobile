package bimobile.views;

import bimobile.dao.UserRepository;
import bimobile.model.customer.Customer;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import bimobile.model.User;
import bimobile.model.Vehicle;
import bimobile.security.AuthorizationUtils;
import bimobile.service.customer.CustomerService;
import bimobile.service.FacilityService;
import bimobile.service.RentalChangeLogService;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Übersicht aller Ausleihen im BI-Mobile System.
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

    private final Grid<Rental> grid = new Grid<>(Rental.class, false);
    private final Grid<RentalChangeLog> changeLogGrid = new Grid<>(RentalChangeLog.class, false);

    private final List<Rental> allRentals = new ArrayList<>();
    private TextField searchField;
    private String currentFilter = "";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Security Context
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

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

        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        // Anti-Crash für Manager ohne Standort
        if (isBranchManager && currentFacility == null) {
            removeAll();
            VerticalLayout error = new VerticalLayout();
            error.setAlignItems(Alignment.CENTER);
            error.setJustifyContentMode(JustifyContentMode.CENTER);
            error.add(new H2("Kein Standort zugewiesen"), new Span("Bitte wenden Sie sich an die Zentrale."));
            add(error);
            return;
        }

        H2 title = new H2("Ausleihübersicht");

        Button neu = new Button("Neue Ausleihe anlegen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neu.addClickListener(e -> openCreateDialog());

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

        configureGrid();
        updateGrid();

        add(header, grid, buildChangeLogSection());
        setFlexGrow(1, grid);
    }

    private void configureGrid() {
        grid.setWidthFull();
        grid.addColumn(Rental::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(r -> r.getCustomer().getFullName()).setHeader("Kunde").setAutoWidth(true);
        grid.addColumn(r -> r.getVehicle().getLicensePlate()).setHeader("Fahrzeug").setAutoWidth(true);
        grid.addColumn(r -> r.getFacility() != null ? r.getFacility().getAddress() : "-").setHeader("Standort").setAutoWidth(true);
        grid.addColumn(Rental::getStartDate).setHeader("Startdatum").setAutoWidth(true);
        grid.addColumn(Rental::getEndDate).setHeader("Enddatum").setAutoWidth(true);
        grid.addColumn(Rental::calculateTotalPrice).setHeader("Gesamtpreis (€)").setAutoWidth(true);
        grid.addColumn(r -> r.getStatus().name()).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(rental -> {
            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> openEditDialog(rental));

            Button info = new Button(new Icon(VaadinIcon.INFO_CIRCLE));
            info.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            info.getElement().setProperty("title", "Details anzeigen");
            info.addClickListener(e -> openRentalInfoDialog(rental));

            Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
            loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            loeschen.addClickListener(e -> openDeleteDialog(rental));

            Button zurueckgeben = new Button(new Icon(VaadinIcon.CHECK));
            zurueckgeben.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            zurueckgeben.addClickListener(e -> {
                openReturnDialog(rental);
            });

            // Sperren wenn nicht eigener Standort (Sicherheitsebene 2)
            if (currentFacility != null && (rental.getFacility() == null || !rental.getFacility().getId().equals(currentFacility.getId()))) {
                bearbeiten.setEnabled(false);
                loeschen.setEnabled(false);
                zurueckgeben.setEnabled(false);
            }

            HorizontalLayout actions = new HorizontalLayout(bearbeiten, info, loeschen, zurueckgeben);
            actions.setSpacing(true);
            return actions;

        }).setHeader("Aktionen").setAutoWidth(true).setFlexGrow(0);
    }

    private void updateGrid() {
        allRentals.clear();
        // Filterung: Wer eine Filiale hat, sieht nur deren Daten
        if (currentFacility != null) {
            allRentals.addAll(rentalService.findRentalsByFacility(currentFacility.getId()));
        } else {
            allRentals.addAll(rentalService.findAllWithCustomerVehicleFacility());
        }
        applyFilter(currentFilter);
        refreshChangeLog();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3("Neue Ausleihe anlegen");

        ComboBox<Customer> customerBox = new ComboBox<>("Kunde");
        customerBox.setItems(customerService.findAllCustomers());
        customerBox.setItemLabelGenerator(Customer::getFullName);

        Button createCustomerButton = new Button("Neuen Kunden anlegen", VaadinIcon.USER_CARD.create());
        createCustomerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        createCustomerButton.addClickListener(click -> UI.getCurrent().navigate("kunden"));

        HorizontalLayout customerSelection = new HorizontalLayout(customerBox, createCustomerButton);
        customerSelection.setAlignItems(Alignment.END);

        ComboBox<Vehicle> vehicleBox = new ComboBox<>("Fahrzeug");
        // Initiale Befüllung
        if (currentFacility != null) {
            vehicleBox.setItems(vehicleService.getVehiclesByFacility(currentFacility.getId()));
        } else {
            vehicleBox.setItems(vehicleService.findAllVehicles());
        }
        vehicleBox.setItemLabelGenerator(v -> v.getLicensePlate() + " – " + v.getModel());

        ComboBox<Facility> facilityBox = new ComboBox<>("Standort");
        facilityBox.setItemLabelGenerator(Facility::getAddress);

        // Standort-Logik Initial
        if (currentFacility != null) {
            facilityBox.setItems(currentFacility);
            facilityBox.setValue(currentFacility);
            facilityBox.setReadOnly(true);
        } else {
            facilityBox.setItems(facilityService.getAllFacilities());
            facilityBox.setReadOnly(false);
        }

        // [LOGIK FIX] Interaktive Filterung ohne Ping-Pong
        if (currentFacility == null) {

            // 1. Standort-Listener
            facilityBox.addValueChangeListener(event -> {
                Facility selectedFacility = event.getValue();
                Vehicle currentlySelectedVehicle = vehicleBox.getValue(); // Das Auto merken!

                if (selectedFacility != null) {
                    List<Vehicle> filteredVehicles = vehicleService.getVehiclesByFacility(selectedFacility.getId());
                    vehicleBox.setItems(filteredVehicles); // Das löscht leider die Auswahl

                    // Prüfen, ob das gemerkte Auto noch gültig ist
                    if (currentlySelectedVehicle != null && currentlySelectedVehicle.getFacility() != null &&
                            currentlySelectedVehicle.getFacility().getId().equals(selectedFacility.getId())) {
                        // Ja -> Sofort wieder setzen!
                        vehicleBox.setValue(currentlySelectedVehicle);
                    } else {
                        // Nein -> Auswahl leeren
                        vehicleBox.clear();
                        if (currentlySelectedVehicle != null) {
                            Notification.show("Fahrzeugauswahl zurückgesetzt (passt nicht zum Standort).").addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        }
                    }
                } else {
                    // Kein Standort -> Alle Autos
                    vehicleBox.setItems(vehicleService.findAllVehicles());
                    vehicleBox.setValue(currentlySelectedVehicle); // Versuch, Auswahl zu behalten
                }
            });

            // 2. Auto-Listener
            vehicleBox.addValueChangeListener(event -> {
                Vehicle selectedVehicle = event.getValue();
                if (selectedVehicle != null && selectedVehicle.getFacility() != null) {
                    // Nur den Standort setzen, wenn er noch nicht passt
                    if (!selectedVehicle.getFacility().equals(facilityBox.getValue())) {
                        facilityBox.setValue(selectedVehicle.getFacility());
                    }
                }
            });
        }

        DatePicker startDate = new DatePicker("Startdatum");
        DatePicker endDate = new DatePicker("Enddatum");

        TextField totalRateField = new TextField("Gesamtrate (Vorschau) €");
        totalRateField.setReadOnly(true);

        Runnable recalcTotal = () -> {
            Vehicle v = vehicleBox.getValue();
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();
            if (v != null && start != null && end != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                if (days <= 0) days = 1;
                double dailyRate = v.getPriceCategory().getBaseRate();
                totalRateField.setValue(String.valueOf(dailyRate * days));
            } else {
                totalRateField.clear();
            }
        };

        vehicleBox.addValueChangeListener(e -> recalcTotal.run());
        startDate.addValueChangeListener(e -> recalcTotal.run());
        endDate.addValueChangeListener(e -> recalcTotal.run());

        enforceDateOrder(startDate, endDate);

        Button save = new Button("Speichern", e -> {
            try {
                if (customerBox.isEmpty() || vehicleBox.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || facilityBox.isEmpty()) {
                    Notification.show("Bitte mindestens Kunde, Fahrzeug, Standort, Start- und Enddatum angeben.")
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

            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(customerSelection, vehicleBox, facilityBox, startDate, endDate, totalRateField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        form.setColspan(customerSelection, 2);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
        dialog.add(layout);
        dialog.open();
    }

    private void openRentalInfoDialog(Rental rental) {
        Dialog dialog = new Dialog();
        dialog.setWidth("540px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3("Übersicht Ausleihe #" + rental.getId());

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
                buildInfoRow("Startdatum", rental.getStartDate() != null ? rental.getStartDate().format(dateFormatter) : "-"),
                buildInfoRow("Enddatum", rental.getEndDate() != null ? rental.getEndDate().format(dateFormatter) : "-"),
                buildInfoRow("Gesamtpreis", rental.getTotalPrice() + " €"),
                buildInfoRow("Letzte Aktualisierung", rental.getUpdatedAt() != null ? rental.getUpdatedAt().format(dateTimeFormatter) : "-")
        );

        Button close = new Button("Schließen", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(dialogTitle, infoLayout, close);
        layout.setAlignItems(Alignment.STRETCH);
        layout.setSpacing(true);

        dialog.add(layout);
        dialog.open();
    }

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
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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

    private void openEditDialog(Rental rental) {
        double oldPrice = rental.calculateTotalPrice();
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3("Ausleihe bearbeiten (#" + rental.getId() + ")");

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
        facilityBox.setItemLabelGenerator(Facility::getAddress);
        facilityBox.setValue(rental.getFacility());

        // Standort-Logik beim Bearbeiten
        if (currentFacility != null) {
            facilityBox.setItems(currentFacility);
            facilityBox.setValue(currentFacility);
            facilityBox.setReadOnly(true);
        } else {
            facilityBox.setItems(facilityService.getAllFacilities());
            facilityBox.setReadOnly(false);
        }

        // [LOGIK FIX] Interaktive Filterung auch beim Bearbeiten
        if (currentFacility == null) {
            facilityBox.addValueChangeListener(event -> {
                Facility selectedFacility = event.getValue();
                Vehicle currentVehicle = vehicleBox.getValue(); // Merken!

                if (selectedFacility != null) {
                    List<Vehicle> filteredVehicles = vehicleService.getVehiclesByFacility(selectedFacility.getId());
                    vehicleBox.setItems(filteredVehicles); // Löscht Auswahl

                    // Wiederherstellen wenn möglich
                    if (currentVehicle != null && currentVehicle.getFacility() != null &&
                            currentVehicle.getFacility().getId().equals(selectedFacility.getId())) {
                        vehicleBox.setValue(currentVehicle);
                    } else {
                        vehicleBox.clear();
                        if (currentVehicle != null) {
                            Notification.show("Fahrzeugauswahl zurückgesetzt.").addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        }
                    }
                } else {
                    vehicleBox.setItems(vehicleService.findAllVehicles());
                    vehicleBox.setValue(currentVehicle);
                }
            });

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

        Runnable recalcTotal = () -> {
            Vehicle v = vehicleBox.getValue();
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();
            if (v != null && start != null && end != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                if (days <= 0) days = 1;
                double dailyRate = v.getPriceCategory().getBaseRate();
                totalRateField.setValue(String.valueOf(dailyRate * days));
            } else {
                totalRateField.clear();
            }
        };
        // Initiale Berechnung
        recalcTotal.run();

        startDate.addValueChangeListener(e -> recalcTotal.run());
        endDate.addValueChangeListener(e -> recalcTotal.run());
        vehicleBox.addValueChangeListener(e -> recalcTotal.run());

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

            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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

    private void openDeleteDialog(Rental rental) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        H3 dialogTitle = new H3("Ausleihe löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add(new Paragraph("Möchten Sie die Ausleihe wirklich löschen?"));
        content.add(new Paragraph("Kunde: " + rental.getCustomer().getFullName()));

        Button confirmButton = new Button("Löschen", e -> {
            try {
                logChange(rental, "Ausleihe gelöscht", "Ausleihe #" + rental.getId() + " entfernt");
                changeLogService.detachRental(rental);
                rentalService.deleteRental(rental);
                Notification.show("Ausleihe erfolgreich gelöscht.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Fehler beim Löschen.").addThemeVariants(NotificationVariant.LUMO_ERROR);
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

    private VerticalLayout buildChangeLogSection() {
        changeLogGrid.addColumn(entry ->
                        entry.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .setHeader("Datum/Uhrzeit").setAutoWidth(true);
        changeLogGrid.addColumn(entry -> {
            if (entry.getRental() != null) return entry.getRental().getId();
            else return entry.getRentalIdSnapshot();
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
        if (String.valueOf(rental.getId()).contains(lowered)) return true;
        if (rental.getCustomer() != null) {
            String first = rental.getCustomer().getPersonalData().getFirstname() != null
                    ? rental.getCustomer().getPersonalData().getFirstname().toLowerCase() : "";
            String last = rental.getCustomer().getPersonalData().getLastname() != null
                    ? rental.getCustomer().getPersonalData().getLastname().toLowerCase() : "";
            String fullName = (first + " " + last).trim();
            if (first.contains(lowered) || last.contains(lowered) || fullName.contains(lowered)) return true;
        }
        return rental.getVehicle() != null
                && rental.getVehicle().getLicensePlate() != null
                && rental.getVehicle().getLicensePlate().toLowerCase().contains(lowered);
    }
}