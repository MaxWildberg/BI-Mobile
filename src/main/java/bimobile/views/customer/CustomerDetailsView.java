package bimobile.views.customer;

import bimobile.model.customer.Customer;
import bimobile.service.CustomerService;
import bimobile.model.customer.BusinessCustomer;
import bimobile.model.Invoice;
import bimobile.model.Rental;
import bimobile.service.PdfGeneratorService;
import bimobile.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * User Interface zur Darstellung eines spezifischen Kunden mit sämtlichen Informationen.
 * Enthält Tabs zur gesonderten Darstellung zwischen Attributen, sämtlicher Ausleihen und Rechnungen des Kunden.
 * Enthält Funktionen wie bearbeiten und löschen eines Kunden.
 *
 * @author Max Wildberg
 */

@Route(value = "kunden/details/:customerId", layout = MainLayout.class)
@PageTitle("Kunden Details")
@PermitAll
public class CustomerDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomerService customerService;
    private final PdfGeneratorService pdfGeneratorService;
    private Customer customer;
    private Long customerId;
    private Div content;
    private Tabs tabs;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public CustomerDetailsView(CustomerService customerService, PdfGeneratorService pdfGeneratorService) {
        this.customerService = customerService;
        this.pdfGeneratorService = pdfGeneratorService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        if (customer == null) {
            add(new H2("Kunde konnte nicht gefunden werden"));
            return;
        }
    }

    /**
     *
     * @return
     */
    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(true);


        Span avatar = new Span(getInitials(customer.getPersonalData().getFirstname(), customer.getPersonalData().getLastname()));
        avatar.getStyle().set("padding", "12px 16px");
        avatar.getStyle().set("border-radius", "50%");
        avatar.getStyle().set("background", "#eee");
        avatar.getStyle().set("font-weight", "600");

        H2 name = new H2(customer.getPersonalData().getFirstname() + " " + customer.getPersonalData().getLastname());
        Span id = new Span("Kunden-ID: " + customer.getCustomerId());
        id.getStyle().set("color", "gray");

        VerticalLayout nameBlock = new VerticalLayout(name, id);
        nameBlock.setPadding(false);
        nameBlock.setSpacing(false);

        HorizontalLayout left = new HorizontalLayout(avatar, nameBlock);
        left.setSpacing(true);
        left.setAlignItems(Alignment.CENTER);

        Button export = new Button("Exportieren", new Icon(VaadinIcon.DOWNLOAD));
        export.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button edit = new Button("Bearbeiten", new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        edit.addClickListener(event -> {
            EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customer, true, customerService, this::reloadCustomerData);
            dialog.open();
        });

        Button delete = new Button("Löschen", new Icon(VaadinIcon.TRASH));
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

    /**
     *
     * @return
     */
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

    /**
     * Methode zur Kontrolle, ob ein Kunden-Objekt aus CustomerOverview übergeben wurde
     * Baut bei Erfolg das UI oder erzeugt eine Fehlermeldung
     * @param event
     */
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
            customer = customerService.getCustomerByID(customerId);
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

    /**
     * Hilfsmethode zur Darstellung der Kundendaten nach öffnen der DetailsView
     * @return gefülltes Flexlayout an CustomerDetailsView
     */
    private FlexLayout createOverviewContent() {
        // Hauptlayout
        FlexLayout twoCols = new FlexLayout();
        twoCols.setWidthFull();
        twoCols.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        twoCols.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
        twoCols.setAlignItems(FlexLayout.Alignment.START);
        twoCols.getStyle().set("gap", "var(--lumo-space-m)"); // Abstand zwischen den Spalten

        // Linke Spalte
        VerticalLayout leftCol = new VerticalLayout();
        leftCol.setPadding(false);
        leftCol.setSpacing(false);
        leftCol.setWidth("65%"); // Maximalbreite
        leftCol.add(createCard("Persönliche Daten", createPersonalData()));
        leftCol.add(createCard("Adresse", createAddressData()));
        leftCol.add(createCard("Kontakt", createContactData()));
        if (customer instanceof BusinessCustomer) {
            leftCol.add(createCard("Gewerbeanschrift", createEmployerData()));
        }

        // Rechte Spalte
        VerticalLayout rightCol = new VerticalLayout();
        rightCol.setPadding(false);
        rightCol.setSpacing(false);
        rightCol.setWidth("30%"); // Maximalbreite
        rightCol.add(createCard("Statistiken", createStatisticsData()));
        rightCol.add(createCard("Dokumente", createDocumentsSummary()));

        // Spalten hinzufügen
        twoCols.add(leftCol, rightCol);

        return twoCols;
    }


    /**
     * Hilfsmethode zur mehrfach wiederkehrenden erzeugung einer Overview Card
     * @param title Titel der Karte
     * @param content
     * @return
     */
    private Div createCard(String title, Div content) {
        Div card = new Div();

        // Überschrift
        H3 h = new H3(title);
        h.getStyle().set("margin", "0 0 12px 0"); // Abstand unter der Überschrift

        // Card Styling
        card.getStyle().set("border", "1px solid #e0e0e0"); // leichterer Rahmen
        card.getStyle().set("padding", "16px");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)"); // leichter Schatten
        card.getStyle().set("background-color", "white"); // sicherstellen, dass Hintergrund weiß ist
        card.getStyle().set("display", "flex");
        card.getStyle().set("flex-direction", "column");
        card.getStyle().set("gap", "8px"); // Abstand zwischen Überschrift und Inhalt

        card.add(h, content);
        return card;
    }


    /**
     * Methoden zur Füllung der Karten
     * Füllen ein Div mit Informationen über den spezifischen Kunden
     * @return div mit Informationen aus dem Kundenobjekt, übergeben an createCard zur Darstellung in UI
     */
    private Div createPersonalData() {
        Div d = new Div();

        LocalDate birthday = customer.getPersonalData().getBirthday();
        String formattedDate = birthday.format(formatter);

        d.add(new Paragraph("Vorname: " + customer.getPersonalData().getFirstname()));
        d.add(new Paragraph("Nachname: " + customer.getPersonalData().getLastname()));
        d.add(new Paragraph("Geburtsdatum: " + formattedDate + " (" + customer.getAge() + " Jahre)"));

        return d;
    }

    private Div createAddressData() {
        Div d = new Div();
        d.add(new Paragraph(customer.getAddress().getStreet()));
        d.add(new Paragraph(customer.getAddress().getZip() + " " + customer.getAddress().getCity()));
        d.add(new Paragraph(customer.getAddress().getCountry()));
        return d;
    }

    private Div createContactData() {
        Div d = new Div();

        d.add(new Paragraph("E-Mail: " + customer.getContactInfo().getMail()));
        d.add(new Paragraph("Telefon: " + customer.getContactInfo().getTelephone()));

        return d;
    }

    private Div createEmployerData() {
        Div d = new Div();

        if (customer instanceof BusinessCustomer businessCustomer) {
            d.add(new Paragraph("Unternehmen: " + businessCustomer.getCompany().getName()));
            d.add(new Paragraph("Anschrift: " + businessCustomer.getCompany().getAddress()));
        }
        return d;
    }

    private Div createStatisticsData() {
        Div d = new Div();

        d.add(new Paragraph("Anzahl Vermietungen: " + customer.getRentCount()));
        d.add(new Hr());
        d.add(new Paragraph("Gesamtumsatz: € " + String.format("%.2f", customer.getTotalRevenue())));

        return d;
    }

    private Div createDocumentsSummary() {
        Div d = new Div();
        d.add(new Paragraph("Führerscheinnummer: " + customer.getIdentification().getDriverslicense()));
        d.add(new Paragraph("Ausweisnummer: " + customer.getIdentification().getIdcard()));
        return d;
    }

    // Füllt ein Grid mit Mieten des Kunden
    private Div createHistoryContent() {

        Div container = new Div();

        if (customer.getRents() == null || customer.getRents().isEmpty()) {
            container.add(new Paragraph("Dieser Kunde hat noch kein Fahrzeug gemietet."));
        } else {

            Grid<Rental> rentalGrid = new Grid<>(Rental.class, false);

            rentalGrid.addColumn(r -> r.getVehicle().getLicensePlate()
                    + " - " + r.getVehicle().getBrand()
                    + " " + r.getVehicle().getModel())
                    .setHeader("Fahrzeug")
                    .setAutoWidth(true);

            rentalGrid.addColumn(rental -> rental.getStartDate().format(formatter))
                    .setHeader("Startdatum")
                    .setAutoWidth(true);

            rentalGrid.addColumn(rental -> rental.getEndDate().format(formatter))
                    .setHeader("Enddatum")
                    .setAutoWidth(true);

            rentalGrid.addColumn(Rental::pullDailyRateFromVehicle)
                    .setHeader("Tagespreis")
                    .setAutoWidth(true);

            rentalGrid.addColumn(Rental::calculateTotalPrice)
                    .setHeader("Gesamtpreis")
                    .setAutoWidth(true);

            rentalGrid.addColumn(r -> r.getStatus().name())
                    .setHeader("Status")
                    .setAutoWidth(true);

            List<Rental> rentals = customerService.findAllWithCustomerAndVehicle()
                    .stream()
                    .filter(r -> r.getCustomer().getCustomerId().equals(customer.getCustomerId()))
                    .toList();

            rentalGrid.setItems(rentals);
            container.add(rentalGrid);
        }
        return container;
    }

    /**
     * Bei Auswahl des Tabs "Rechnungen" wird ein Grid mit allen Rechnungen zugehörig zum Kunden gefüllt
     * @return Div mit Grid als Übersicht aller Rechnungen
     */
    private Div createInvoiceContent() {
        Div container = new Div();

        if (customer.getInvoices() == null || customer.getInvoices().isEmpty()) {
            container.add(new Paragraph("Dieser Kunde hat noch keine Rechnungen."));
        } else {
            VerticalLayout list = new VerticalLayout();
            list.setPadding(false);
            list.setSpacing(true);

            for (Invoice invoice : customer.getInvoices()) {
                Div card = new Div();
                card.getStyle()
                        .set("border", "1px solid #DDD")
                        .set("border-radius", "8px")
                        .set("padding", "1rem")
                        .set("box-shadow", "0 2px 5px rgba(0,0,0,0.05)")
                        .set("margin-bottom", "10px");

                Paragraph header = new Paragraph("Rechnung #" + invoice.getId()
                        + " – " + invoice.getInvoiceDate());
                Paragraph amount = new Paragraph("Brutto: " + invoice.getGrossAmount() + " €");

                // Download-Button als Anchor
                StreamResource resource = new StreamResource(
                        "rechnung-" + invoice.getId() + ".pdf",
                        () -> new ByteArrayInputStream(pdfGeneratorService.generateInvoicePdf(invoice))
                );
                Anchor downloadLink = new Anchor(resource, "PDF herunterladen");
                downloadLink.getElement().setAttribute("download", true);

                card.add(header, amount, downloadLink);
                list.add(card);
            }

            container.add(list);
        }

        return container;
    }


    /*private void downloadInvoice(Invoice invoice) {
        StreamResource resource = new StreamResource(
                "rechnung-" + invoice.getId() + ".pdf",
                () -> new ByteArrayInputStream(pdfGeneratorService.generateInvoicePdf(invoice))
        );

        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);

        downloadLink.clickInClient(); // Startet sofort den Download
    }*/



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

        this.customer = customerService.getCustomerByID(customerId);

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
    private void openDeleteDialog(Customer customer) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Kunde löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie den Kunden wirklich löschen?");
        content.add(customer.getPersonalData().getFirstname() + " " + customer.getPersonalData().getLastname() + ", Kunden-ID: " + customer.getCustomerId());

        Button confirmButton = new Button("Löschen", e -> {
            try {
                customerService.deleteCustomer(customer.getCustomerId());
                Notification.show("Kunde wurde erfolgreich gelöscht");
            } catch (IllegalArgumentException ex) {
                Notification.show("Fehler: " + ex.getMessage());
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