package bimobile.views.customer;

import bimobile.model.customer.BusinessCustomer;
import bimobile.model.customer.PrivateCustomer;
import bimobile.service.customer.CompanyService;
import bimobile.service.customer.CustomerService;
import bimobile.model.customer.Customer;
import bimobile.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Beschreibung:
 * Hauptübersicht aller Kunden der Kundenadministration.
 * Zeigt alle vorhandenen Kunden. Nutzer können auf alle Kunden zugreifen.
 * Bietet Funktionen im Quick-Access wie bearbeiten und löschen.
 *
 * @author Max Wildberg
 */

@Route(value = "kunden", layout = MainLayout.class)
@PageTitle("Kundenübersicht")
@PermitAll
public class CustomerOverview extends VerticalLayout {

    private final CustomerService customerService;
    private final CompanyService companyService;
    private final Grid<Customer> grid = new Grid<>();

    public CustomerOverview(CustomerService customerService, CompanyService companyService){
        this.customerService = customerService;
        this.companyService = companyService;

        //Layout-Grundstruktur
        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        H2 title = new H2("Kundenübersicht");

        List<Customer> allCustomers = customerService.findAllCustomers();
        TextField searchField = new TextField();
        searchField.setPlaceholder("Suchen...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("300px");

        searchField.addValueChangeListener(e -> {
            String filter = e.getValue().trim().toLowerCase();

            List<Customer> filtered = allCustomers.stream()
                    .filter(c -> c.getFullName().toLowerCase().contains(filter)
                            || c.getContactInfo().getMail().toLowerCase().contains(filter)
                            || c.getContactInfo().getTelephone().toLowerCase().contains(filter))
                    .collect(Collectors.toList());

            grid.setItems(filtered);
        });


        Button registerCustomerButton = new Button("Neuen Kunden anlegen", new Icon(VaadinIcon.PLUS));
        registerCustomerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerCustomerButton.addClickListener(e -> {
            CustomerTypeSelectionDialog typeSelectionDialog = new CustomerTypeSelectionDialog(type -> {
                Customer customer;
                switch (type) {
                    case PRIVATE -> customer = new PrivateCustomer();
                    case BUSINESS -> customer = new BusinessCustomer();
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                }

                EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customer, false, customerService, companyService, this::updateGrid);
                dialog.open();
            });
            typeSelectionDialog.open();
        });

        Button companyOverview = new Button("Firmenübersicht");
        companyOverview.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        companyOverview.addClickListener(e -> {
            CompanyOverviewDialog companyOverviewDialog = new CompanyOverviewDialog(companyService);
            companyOverviewDialog.open();
        });

        HorizontalLayout header = new HorizontalLayout();
        HorizontalLayout right = new HorizontalLayout(searchField, companyOverview, registerCustomerButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.add(title, right);

        grid.addColumn(c -> c.getCustomerId())
                .setHeader("ID")
                .setAutoWidth(true)
                .setFlexGrow(0);
        grid.addColumn(c -> {
            if (c instanceof PrivateCustomer) return "Privatkunde";
            if (c instanceof BusinessCustomer) return "Firmenkunde";
            return "Unbekannt";
        }).setHeader("Typ").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(c -> c.getFullName())
                .setHeader("Name")
                .setAutoWidth(true);
        grid.addColumn(c -> c.getContactInfo().getMail())
                .setHeader("E-Mail")
                .setAutoWidth(true);
        grid.addColumn(c -> c.getContactInfo().getTelephone())
                .setHeader("Telefonnummer")
                .setAutoWidth(true);

        // fügt buttons "Bearbeiten", "Löschen" und "Details" zum Grid hinzu
        grid.addComponentColumn(customer -> {
            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> {
                EditCreateCustomerDialog editCreateCustomerDialog = new EditCreateCustomerDialog(customer, true, customerService, companyService, this::updateGrid);
                editCreateCustomerDialog.open();
            });

            Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
            loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            loeschen.addClickListener(e -> {
                DeleteDialog<Customer> deleteDialog = new DeleteDialog<>(customer, Customer::getFullName,
                        c -> customerService.deleteCustomer(customer.getCustomerId()),
                        this::updateGrid
                );
                deleteDialog.open();
            });

            Button details = new Button(new Icon(VaadinIcon.INFO));
            details.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_TERTIARY);
            details.addClickListener(e -> {
                UI.getCurrent().navigate("kunden/details/" + customer.getCustomerId());
            });

            return new HorizontalLayout(bearbeiten, loeschen, details);
        }).setHeader("Aktionen");

        updateGrid();

        grid.setWidthFull();
        grid.getStyle().set("overflow-x", "auto");

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        add(header, grid);
        setFlexGrow(1, grid);
    }

    /**
     * Füllt Grid mit Daten bzw. Kunden
     * Aufruf nachdem mit {@link EditCreateCustomerDialog} ein Kunde erstellt oder bearbeitet wurde
     */
    private void updateGrid() {
        List<Customer> customers = customerService.findAllCustomers();
        grid.setItems(customers);
    }
}