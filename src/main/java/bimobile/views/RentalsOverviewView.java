package bimobile.views;

import bimobile.model.Rental;
import bimobile.service.RentalService;
import bimobile.service.CustomerService;
import bimobile.service.VehicleService;
import bimobile.service.FacilityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "rentals", layout = MainLayout.class)
@PageTitle("Ausleihübersicht")
@PermitAll
public class RentalsOverviewView extends VerticalLayout {

    private final RentalService rentalService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final FacilityService facilityService;

    private final Grid<Rental> grid = new Grid<>(Rental.class, false);

    public RentalsOverviewView(RentalService rentalService,
                               CustomerService customerService,
                               VehicleService vehicleService,
                               FacilityService facilityService) {

        this.rentalService = rentalService;
        this.customerService = customerService;
        this.vehicleService = vehicleService;
        this.facilityService = facilityService;

        setSizeFull();
        setPadding(true);

        H2 title = new H2("Ausleihen");

        Button neu = new Button("Ausleihe erstellen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neu.addClickListener(e -> openCreateDialog());

        HorizontalLayout header = new HorizontalLayout(title, neu);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Spalten
        grid.addColumn(r -> r.getCustomer().getFullName()).setHeader("Kunde");
        grid.addColumn(r -> r.getVehicle().getLicensePlate()).setHeader("Fahrzeug");
        grid.addColumn(Rental::getStartDate).setHeader("Start");
        grid.addColumn(Rental::getEndDate).setHeader("Ende");
        grid.addColumn(Rental::getTotalPrice).setHeader("Preis (€)");

        updateGrid();

        add(header, grid);
        setFlexGrow(1, grid);
    }

    private void updateGrid() {
        grid.setItems(rentalService.findAll());
    }

    private void openCreateDialog() {
        Dialog dialog = new RentalCreateDialog(
                customerService, vehicleService, facilityService, rentalService, this::updateGrid
        );
        dialog.open();
    }
}
