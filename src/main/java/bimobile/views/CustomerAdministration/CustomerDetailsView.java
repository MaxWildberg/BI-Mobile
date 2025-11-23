package bimobile.views.CustomerAdministration;

import bimobile.controller.CustomerManager;
import bimobile.model.Customer;
import bimobile.model.Rental;
import bimobile.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import jakarta.annotation.security.PermitAll;

@Route(value = "kunden/details/:customerId", layout = MainLayout.class)
@PageTitle("Kunden Details")
@PermitAll
public class CustomerDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomerManager controller;
    private Customer customer;
    private Long customerId;
    private Div content;
    private Tabs tabs;

    public CustomerDetailsView(CustomerManager controller) {
        this.controller = controller;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        if (customer == null) {
            add(new H2("Kunde konnte nicht gefunden werden"));
            return;
        }


        //add(createHeader());
        //add(createTabs());
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);

        // Initialen als "Avatar"
        Span avatar = new Span(getInitials(customer.getName(), customer.getLastname()));
        avatar.getStyle().set("padding", "12px 16px");
        avatar.getStyle().set("border-radius", "50%");
        avatar.getStyle().set("background", "#eee");
        avatar.getStyle().set("font-weight", "600");

        H2 name = new H2(customer.getName() + " " + customer.getLastname());
        Span id = new Span("Kunden-ID: " + customer.getCustomerId());
        id.getStyle().set("color", "gray");

        VerticalLayout nameBlock = new VerticalLayout(name, id);
        nameBlock.setPadding(false);
        nameBlock.setSpacing(false);

        HorizontalLayout left = new HorizontalLayout(avatar, nameBlock);
        left.setSpacing(true);
        left.setAlignItems(Alignment.CENTER);

        // Aktionen
        Button export = new Button("Exportieren", new Icon(VaadinIcon.DOWNLOAD));
        export.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button edit = new Button("Bearbeiten", new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        edit.addClickListener(event -> {
            EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customer, true, controller, this::reloadCustomerData);
            dialog.open();
        });

        Button delete = new Button("Löschen", new Icon(VaadinIcon.DEL));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> {
            openDeleteDialog(this.customer);
        });

        HorizontalLayout actions = new HorizontalLayout(export, edit, delete);
        HorizontalLayout right = new HorizontalLayout(actions);
        right.setSpacing(true);
        right.getStyle().set("margin-left", "auto");

        header.add(left, right);
        return header;
    }

    private VerticalLayout createTabs() {
        Tab uebersicht = new Tab("Übersicht");
        Tab historie = new Tab("Miethistorie");
        Tab dokumente = new Tab("Dokumente");
        tabs = new Tabs(uebersicht, historie, dokumente);

        content = new Div();
        content.setWidthFull();
        content.add(createOverviewContent());

        tabs.addSelectedChangeListener(e -> {
            content.removeAll();
            if (tabs.getSelectedTab() == uebersicht) {
                content.add(createOverviewContent());
            } else if (tabs.getSelectedTab() == historie) {
                content.add(createHistoryContent());
            } else {
                content.add(createDocumentsContent());
            }
        });

        return new VerticalLayout(tabs, content);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idStr = event.getRouteParameters().get("customerId").orElse(null);
        if (idStr == null) {
            removeAll();
            add(new H2("Keine Kunden-ID übergeben"));
            return;
        }

        try {
            customerId = Long.valueOf(idStr);
            customer = controller.getCustomerByID(customerId);
        } catch (NumberFormatException ex) {
            removeAll();
            add(new H2("Ungültige Kunden-ID"));
            return;
        }

        if (customer == null) {
            removeAll();
            add(new H2("Kunde nicht gefunden (ID: " + customerId + ")"));
            return;
        }

        removeAll();
        add(createHeader());
        add(createTabs());
    }

    private HorizontalLayout createOverviewContent() {
        HorizontalLayout twoCols = new HorizontalLayout();
        twoCols.setWidthFull();
        twoCols.setSpacing(true);

        VerticalLayout leftCol = new VerticalLayout();
        leftCol.setWidth("65%");
        leftCol.setPadding(false);

        VerticalLayout rightCol = new VerticalLayout();
        rightCol.setWidth("35%");
        rightCol.setPadding(false);

        leftCol.add(createCard("Persönliche Daten", createPersonalData()));
        leftCol.add(createCard("Adresse", createAddressData()));
        leftCol.add(createCard("Kontakt", createContactData()));

        // rightCol.add(createCard("Statistiken", createStatisticsData()));
        rightCol.add(createCard("Dokumente", createDocumentsSummary()));

        twoCols.add(leftCol, rightCol);
        return twoCols;
    }

    private Div createCard(String title, Div content) {
        Div card = new Div();

        H3 h = new H3(title);
        h.getStyle().set("margin-top", "0");

        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("padding", "16px");
        card.getStyle().set("border-radius", "8px");

        card.add(h, content);
        return card;
    }

    private Div createPersonalData() {
        Div d = new Div();

        d.add(new Paragraph("Vorname: " + customer.getName()));
        d.add(new Paragraph("Nachname: " + customer.getLastname()));
        d.add(new Paragraph("Geburtsdatum: " + customer.getBirthday() + " (" + customer.getAge() + " Jahre)"));

        return d;
    }

    private Div createAddressData() {
        Div d = new Div();
        d.add(new Paragraph(customer.getAddress()));
        d.add(new Paragraph(customer.getZip() + " " + customer.getResidence()));
        d.add(new Paragraph(customer.getCountry()));
        return d;
    }

    private Div createContactData() {
        Div d = new Div();

        d.add(new Paragraph("E-Mail: " + customer.getEmail()));
        d.add(new Paragraph("Telefon: " + customer.getTelephone()));

        return d;
    }

//    private Div createStatisticsData() {
//        Div d = new Div();
//
//        d.add(new Paragraph("Anzahl Vermietungen: " + customer.getRentCount()));
//        d.add(new Hr());
//        d.add(new Paragraph("Gesamtumsatz: € " + String.format("%.2f", customer.getTotalRevenue())));
//
//        return d;
//    }

    private Div createDocumentsSummary() {
        Div d = new Div();
        d.add(new Paragraph("Führerscheinnummer: " + customer.getDriverslicenseID()));
        d.add(new Paragraph("Ausweisnummer: " + customer.getIdCardNumber()));
        return d;
    }

    private Div createHistoryContent() {

        Div d = new Div();
        d.add(new Paragraph("Miethistorie (Platzhalter)"));

        if (customer.getRents() == null || customer.getRents().isEmpty()) {
            d.add(new Paragraph("Dieser Kunde hat noch kein Fahrzeug gemietet."));
        } else {
            Grid<Rental> grid = new Grid<>(Rental.class, true);
            grid.setItems(customer.getRents()); // populate the grid
            grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
            d.add(grid);
        }

        return d;
    }

    private Div createDocumentsContent() {
        Div d = new Div();
        d.add(new Paragraph("Dokumente (Platzhalter)"));
        return d;
    }

    private String getInitials(String first, String last) {
        String f = first != null && !first.isBlank() ? first.substring(0, 1).toUpperCase() : "";
        String l = last != null && !last.isBlank() ? last.substring(0, 1).toUpperCase() : "";
        return (f + l).isEmpty() ? "?" : f + l;
    }

    private void reloadCustomerData(){
        if (customerId == null) return;

        this.customer = controller.getCustomerByID(customerId);

        content.removeAll();

        if (tabs.getSelectedTab().getLabel().equals("Übersicht")) {
            content.add(createOverviewContent());
        } if (tabs.getSelectedTab().getLabel().equals("Miethistore")) {
            content.add(createHistoryContent());
        } else {
            content.add(createDocumentsContent());
        }

        removeAll();
        add(createHeader());
        add(createTabs());
    }

    private void openDeleteDialog(Customer customer) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Kunde löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie den Kunden wirklich löschen?");
        content.add(customer.getName() + " " + customer.getLastname() + ", Geburtsdatum: " + customer.getBirthday());

        Button confirmButton = new Button("Löschen", e -> {
            String result = controller.deleteCustomer(customer.getCustomerId());

            Notification notification = Notification.show(result);
            if (result.startsWith("Erfolg")) {
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }

            dialog.close();
            UI.getCurrent().navigate("kunden");
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
