package bimobile.views;

import bimobile.dao.UserRepository;
import bimobile.model.Facility;
import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import bimobile.model.User;
import bimobile.security.AuthorizationUtils;
import bimobile.service.customer.CompanyService;
import bimobile.service.customer.CustomerService;
import bimobile.service.FacilityService;
import bimobile.service.RentalChangeLogService;
import bimobile.service.RentalService;
import bimobile.service.VehicleService;
import bimobile.views.rentals.dialogs.RentalCreateDialog;
import bimobile.views.rentals.dialogs.RentalDeleteDialog;
import bimobile.views.rentals.dialogs.RentalEditDialog;
import bimobile.views.rentals.dialogs.RentalInfoDialog;
import bimobile.views.rentals.dialogs.RentalReturnDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    private final CompanyService companyService;

    private final Grid<Rental> grid = new Grid<>(Rental.class, false);
    private final Grid<RentalChangeLog> changeLogGrid = new Grid<>(RentalChangeLog.class, false);

    private final List<Rental> allRentals = new ArrayList<>();
    private TextField searchField;
    private String currentFilter = "";

    // Security Context
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    public RentalsOverviewView(RentalService rentalService,
                               CustomerService customerService,
                               VehicleService vehicleService,
                               FacilityService facilityService,
                               RentalChangeLogService changeLogService,
                               UserRepository userRepository,
                               CompanyService companyService) {

        this.rentalService = rentalService;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;
        this.changeLogService = changeLogService;
        this.userRepository = userRepository;
        this.companyService = companyService;

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
        RentalCreateDialog dialog = new RentalCreateDialog(
                rentalService,
                customerService,
                vehicleService,
                facilityService,
                companyService,
                currentFacility,
                this::updateGrid,
                rental -> {
                    logChange(rental, "Ausleihe erstellt", "Ausleihe #" + rental.getId() + " angelegt");
                    updateGrid();
                }
        );
        dialog.open();
    }

    private void openRentalInfoDialog(Rental rental) {
        RentalInfoDialog dialog = new RentalInfoDialog(rental);
        dialog.open();
    }

    private void openReturnDialog(Rental rental) {
        RentalReturnDialog dialog = new RentalReturnDialog(
                rental,
                rentalService,
                this::updateGrid
        );
        dialog.open();
    }

    private void openEditDialog(Rental rental) {
        RentalEditDialog dialog = new RentalEditDialog(
                rental,
                rentalService,
                vehicleService,
                facilityService,
                currentFacility,
                updated -> {
                    logChange(updated,
                            "Ausleihe aktualisiert",
                            "Ausleihe #" + updated.getId() + " angepasst");
                    updateGrid();
                }
        );
        dialog.open();
    }

    private void openDeleteDialog(Rental rental) {
        RentalDeleteDialog dialog = new RentalDeleteDialog(
                rental,
                rentalService,
                changeLogService,
                deleted -> logChange(deleted, "Ausleihe gelöscht", "Ausleihe #" + deleted.getId() + " entfernt"),
                this::updateGrid
        );
        dialog.open();
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
