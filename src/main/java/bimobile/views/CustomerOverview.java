package bimobile.views;

import bimobile.service.CustomerService;
import bimobile.model.Customer;
import bimobile.security.AuthorizationUtils;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * Author: Lasse
 * Description: Customer overview with role-based access.
 */
@Route(value = "kunden", layout = MainLayout.class)
@PageTitle("Kundenübersicht")
@PermitAll
public class CustomerOverview extends VerticalLayout {

    private final CustomerService service;
    private final Grid<Customer> grid = new Grid<>();

    // MANAGEMENT + BRANCH_MANAGER -> dürfen löschen
    private final boolean canDelete =
            AuthorizationUtils.isManagement() || AuthorizationUtils.isBranchManager();

    // Edit und Create -> für alle erlaubt
    private final boolean canEdit = true;
    private final boolean canCreate = true;

    public CustomerOverview(CustomerService service){
        this.service = service;

        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        H2 title = new H2("Kundenübersicht");

        Button registerCustomerButton = new Button(
                "Neuen Kunden anlegen",
                new Icon(VaadinIcon.PLUS)
        );
        registerCustomerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerCustomerButton.setEnabled(canCreate);

        registerCustomerButton.addClickListener(e -> {
            EditCreateCustomerDialog dialog =
                    new EditCreateCustomerDialog(null, false, service, this::updateGrid);
            dialog.open();
        });

        HorizontalLayout header = new HorizontalLayout(title, registerCustomerButton);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        grid.addColumn(Customer::getCustomerId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Customer::getLastName).setHeader("Nachname").setAutoWidth(true);
        grid.addColumn(Customer::getFirstName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Customer::getEmail).setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn(Customer::getTelephone).setHeader("Telefonnummer").setAutoWidth(true);

        grid.addComponentColumn(customer -> {

            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.setEnabled(canEdit);
            bearbeiten.addClickListener(e -> {
                EditCreateCustomerDialog dialog =
                        new EditCreateCustomerDialog(customer, true, service, this::updateGrid);
                dialog.open();
            });

            Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
            loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            loeschen.setEnabled(canDelete);
            loeschen.addClickListener(e -> openDeleteDialog(customer));

            Button details = new Button(new Icon(VaadinIcon.INFO));
            details.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            details.addClickListener(e ->
                    UI.getCurrent().navigate("kunden/details/" + customer.getCustomerId())
            );

            return new HorizontalLayout(bearbeiten, loeschen, details);

        }).setHeader("Aktionen");

        updateGrid();

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        add(header, grid);
        setFlexGrow(1, grid);
    }

    private void updateGrid() {
        List<Customer> customers = service.findAllCustomers();
        grid.setItems(customers);
    }

    private void openDeleteDialog(Customer customer) {
        if (!canDelete) {
            Notification.show("Sie haben keine Berechtigung, Kunden zu löschen.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Kunde löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie den Kunden wirklich löschen?");
        content.add(customer.getFirstName() + " " + customer.getLastName()
                + ", Geburtsdatum: " + customer.getBirthday());

        Button confirmButton = new Button("Löschen", e -> {
            String result = service.deleteCustomer(customer.getCustomerId());

            Notification notification = Notification.show(result);
            if (result.startsWith("Erfolg")) {
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
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
