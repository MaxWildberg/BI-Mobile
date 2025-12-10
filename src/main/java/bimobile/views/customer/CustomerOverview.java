package bimobile.views.customer;

import bimobile.model.customer.BusinessCustomer;
import bimobile.model.customer.PrivateCustomer;
import bimobile.service.CustomerService;
import bimobile.model.customer.Customer;
import bimobile.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

    private final CustomerService service;
    private final Grid<Customer> grid = new Grid<>();

    public CustomerOverview(CustomerService service){
        this.service = service;

        //Layout-Grundstruktur
        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        H2 title = new H2("Kundenübersicht");

        List<Customer> allCustomers = service.findAllCustomers();
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

                EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customer, false, service, this::updateGrid);
                dialog.open();
            });
            typeSelectionDialog.open();
        });

        HorizontalLayout header = new HorizontalLayout(title, searchField, registerCustomerButton);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        grid.addColumn(c -> c.getCustomerId())
                .setHeader("ID")
                .setAutoWidth(true);
        grid.addColumn(c -> {
            if (c instanceof PrivateCustomer) return "Privatkunde";
            if (c instanceof BusinessCustomer) return "Firmenkunde";
            return "Unbekannt";
        }).setHeader("Typ").setAutoWidth(true);
        grid.addColumn(c -> c.getFullName())
                .setHeader("Name")
                .setAutoWidth(true);
        grid.addColumn(c -> c.getContactInfo().getMail())
                .setHeader("E-Mail")
                .setAutoWidth(true);
        grid.addColumn(c -> c.getContactInfo().getTelephone())
                .setHeader("Telefonnummer")
                .setAutoWidth(true);

        // fügt buttons "Bearbeiten" und "Löschen" zum Grid hinzu
        grid.addComponentColumn(customer -> {
            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> {
                EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customer, true, service, this::updateGrid);
                dialog.open();
            });

            Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
            loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            loeschen.addClickListener(e -> openDeleteDialog(customer));

            Button details = new Button(new Icon(VaadinIcon.INFO));
            details.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_TERTIARY);
            details.addClickListener(e -> {
                UI.getCurrent().navigate("kunden/details/" + customer.getCustomerId());
            });

            return new HorizontalLayout(bearbeiten, loeschen, details);
        }).setHeader("Aktionen");

        updateGrid();

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        add(header, grid);
        setFlexGrow(1, grid);

    }

    /**
     * Füllt Grid mit Daten bzw. Kunden
     * Aufruf nachdem mit EditCreateCustomerDialog ein Kunde erstellt oder bearbeitet wurde
     */
    private void updateGrid() {
        List<Customer> customers = service.findAllCustomers();
        grid.setItems(customers);
    }


    /**
     * Öffnet Dialog welches dem Nutzer den Kunden anzeigt und die Wahl gibt zu löschen
     * @param customer Kunde der gelöscht werden soll
     */
    private void openDeleteDialog(Customer customer) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Kunde löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie den Kunden wirklich löschen?");
        content.add(customer.getPersonalData().getFirstname() + " " + customer.getPersonalData().getLastname() + ", Kunden-ID: " + customer.getCustomerId());

        Button confirmButton = new Button("Löschen", e -> {
            try {
                service.deleteCustomer(customer.getCustomerId());
                Notification.show("Kunde erfolgreich gelöscht.");
            } catch (IllegalArgumentException ex) {
                Notification.show("Fehler: " + ex.getMessage());
            }

            updateGrid();
            dialog.close();
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
}