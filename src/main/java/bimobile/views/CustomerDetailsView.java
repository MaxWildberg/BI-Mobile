package bimobile.views;

import bimobile.service.CustomerService;
import bimobile.model.BusinessCustomer;
import bimobile.model.CustomerInterface;
import bimobile.model.Invoice;
import bimobile.model.Rental;
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

/**
 * Beschreibung:
 * User Interface zur Darstellung eines spezifischen Kunden mit sämtlichen Informationen.
 * Enthält Tabs zur gesonderten Darstellung zwischen Attributen, sämtlicher Ausleihen und Rechnungen des Kunden.
 * Enthält Funktionen wie bearbeiten und löschen.
 *
 * @author Max Wildberg
 */

@Route(value = "kunden/details/:customerId", layout = MainLayout.class)
@PageTitle("Kunden Details")
@PermitAll
public class CustomerDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomerService service;
    private CustomerInterface customerInterface;
    private Long customerId;
    private Div content;
    private Tabs tabs;

    public CustomerDetailsView(CustomerService controller) {
        this.service = controller;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        if (customerInterface == null) {
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
        Span avatar = new Span(getInitials(customerInterface.getName(), customerInterface.getLastname()));
        avatar.getStyle().set("padding", "12px 16px");
        avatar.getStyle().set("border-radius", "50%");
        avatar.getStyle().set("background", "#eee");
        avatar.getStyle().set("font-weight", "600");

        H2 name = new H2(customerInterface.getName() + " " + customerInterface.getLastname());
        Span id = new Span("Kunden-ID: " + customerInterface.getCustomerId());
        id.getStyle().set("color", "gray");

        VerticalLayout nameBlock = new VerticalLayout(name, id);
        nameBlock.setPadding(false);
        nameBlock.setSpacing(false);

        HorizontalLayout left = new HorizontalLayout(avatar, nameBlock);
        left.setSpacing(true);
        left.setAlignItems(Alignment.CENTER);

        //
        Button export = new Button("Exportieren", new Icon(VaadinIcon.DOWNLOAD));
        export.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button edit = new Button("Bearbeiten", new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        edit.addClickListener(event -> {
            EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customerInterface, true, service, this::reloadCustomerData);
            dialog.open();
        });

        Button delete = new Button("Löschen", new Icon(VaadinIcon.DEL));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> {
            openDeleteDialog(this.customerInterface);
        });

        HorizontalLayout actions = new HorizontalLayout(export, edit, delete);
        HorizontalLayout right = new HorizontalLayout(actions);
        right.setSpacing(true);
        right.getStyle().set("margin-left", "auto");

        header.add(left, right);
        return header;
    }

    // Methode zum Aufbau der Tabs
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
                content.add(createInvoiceContent());
            }
        });

        return new VerticalLayout(tabs, content);
    }

    // Methode zur Kontrolle, ob ein Kunden-Objekt aus CustomerOverview übergeben wurde
    // Baut bei Erfolg das UI oder erzeugt eine Fehlermeldung
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
            customerInterface = service.getCustomerByID(customerId);
        } catch (NumberFormatException ex) {
            removeAll();
            add(new H2("Ungültige Kunden-ID"));
            return;
        }

        if (customerInterface == null) {
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
        leftCol.add(createCard("Gewerbeanschrift", createEmployerData()));

        rightCol.add(createCard("Statistiken", createStatisticsData()));
        rightCol.add(createCard("Dokumente", createDocumentsSummary()));

        twoCols.add(leftCol, rightCol);
        return twoCols;
    }

    // Hilfsmethode zur mehrfach wiederkehrenden erzeugung einer Overview Card
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

    /**
     * Methoden zur Füllung der Karten
     * Füllen ein Div mit Informationen über den spezifischen Kunden
     * Jeweils als Parameter übergeben in createCard Methode
     * @return
     */
    private Div createPersonalData() {
        Div d = new Div();

        d.add(new Paragraph("Vorname: " + customerInterface.getName()));
        d.add(new Paragraph("Nachname: " + customerInterface.getLastname()));
        d.add(new Paragraph("Geburtsdatum: " + customerInterface.getBirthday() + " (" + customerInterface.getAge() + " Jahre)"));

        return d;
    }

    private Div createAddressData() {
        Div d = new Div();
        d.add(new Paragraph(customerInterface.getAddress()));
        d.add(new Paragraph(customerInterface.getZip() + " " + customerInterface.getResidence()));
        d.add(new Paragraph(customerInterface.getCountry()));
        return d;
    }

    private Div createContactData() {
        Div d = new Div();

        d.add(new Paragraph("E-Mail: " + customerInterface.getEmail()));
        d.add(new Paragraph("Telefon: " + customerInterface.getTelephone()));

        return d;
    }

    private Div createEmployerData() {
        Div d = new Div();

        if (customerInterface instanceof BusinessCustomer businessCustomer) {
            d.add(new Paragraph("Unternehmen: " + businessCustomer.getCompany()));
            d.add(new Paragraph("Adresse des Unternehmens: " + businessCustomer.getCompanyAddress()));
        }
        return d;
    }

    private Div createStatisticsData() {
        Div d = new Div();

        d.add(new Paragraph("Anzahl Vermietungen: " + customerInterface.getRentCount()));
        d.add(new Hr());
        d.add(new Paragraph("Gesamtumsatz: € " + String.format("%.2f", customerInterface.getTotalRevenue())));

        return d;
    }

    private Div createDocumentsSummary() {
        Div d = new Div();
        d.add(new Paragraph("Führerscheinnummer: " + customerInterface.getDriversLicenseID()));
        d.add(new Paragraph("Ausweisnummer: " + customerInterface.getIdCardNumber()));
        return d;
    }

    // Füllt ein Grid mit Mieten des Kunden
    private Div createHistoryContent() {

        Div container = new Div();
        container.add(new Paragraph("Miethistorie (Platzhalter)"));

        if (customerInterface.getRents() == null || customerInterface.getRents().isEmpty()) {
            container.add(new Paragraph("Dieser Kunde hat noch kein Fahrzeug gemietet."));
        } else {
            Grid<Rental> grid = new Grid<>(Rental.class, true);
            grid.setItems(customerInterface.getRents());
            grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
            grid.setHeight("400px");
            container.add(grid);
        }
        return container;
    }

    /**
     * Bei Auswahl des Tabs "Rechnungen" wird ein Grid mit allen Rechnungen zugehörig zum Kunden gefüllt
     * @return Div mit Grid als Übersicht aller Rechnungen
     */
    private Div createInvoiceContent() {
        Div container = new Div();
        container.add(new Paragraph("Dokumente (Platzhalter)"));

        if (customerInterface.getInvoices() == null || customerInterface.getInvoices().isEmpty()) {
            container.add(new Paragraph("Dieser Kunde hat noch keine Rechnungen."));
        } else {
            Grid<Invoice> grid = new Grid<>(Invoice.class, true);
            grid.setItems(customerInterface.getInvoices());
            grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
            grid.setHeight("400px");
            container.add(grid);
        }
        return container;
    }

    /**
     * Liest die Initiale des Kunden aus zur Darstellung in der Detail Übersicht
     * @param first Initial des Vornamens
     * @param last Initial des Nachnamens
     * @return Rückgabe der Initiale an UI zur Darstellung
     */
    private String getInitials(String first, String last) {
        String f = first != null && !first.isBlank() ? first.substring(0, 1).toUpperCase() : "";
        String l = last != null && !last.isBlank() ? last.substring(0, 1).toUpperCase() : "";
        return (f + l).isEmpty() ? "?" : f + l;
    }

    /**
     * Lädt die entsprechenden Seiten bei auswahl der Tabs -> Übersicht, Miethistorie, Rechnungshistorie
     */
    private void reloadCustomerData(){
        if (customerId == null) return;

        this.customerInterface = service.getCustomerByID(customerId);

        content.removeAll();

        if (tabs.getSelectedTab().getLabel().equals("Übersicht")) {
            content.add(createOverviewContent());
        } if (tabs.getSelectedTab().getLabel().equals("Miethistore")) {
            content.add(createHistoryContent());
        } else {
            content.add(createInvoiceContent());
        }

        removeAll();
        add(createHeader());
        add(createTabs());
    }

    // Öffnet ein Dialog welches dem Nutzer die Wahl gibt, ob der Kunde gelöscht werden soll
    private void openDeleteDialog(CustomerInterface customer) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Kunde löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie den Kunden wirklich löschen?");
        content.add(customer.getName() + " " + customer.getLastname() + ", Geburtsdatum: " + customer.getBirthday());

        Button confirmButton = new Button("Löschen", e -> {
            String result = service.deleteCustomer(customer.getCustomerId());

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
