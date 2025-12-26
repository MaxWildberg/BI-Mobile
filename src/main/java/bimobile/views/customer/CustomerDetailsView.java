package bimobile.views.customer;

import bimobile.model.customer.*;
import bimobile.security.AuthorizationUtils; // [Neu] Import für Berechtigung
import bimobile.service.customer.*;
import bimobile.model.Invoice;
import bimobile.model.Rental;
import bimobile.service.PdfGeneratorService;
import bimobile.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
 * View zur Darstellung aller Details eines Kunden.
 * Enthält Tabs für Übersicht, Miethistorie und Rechnungen.
 * Bietet Funktionen zum Bearbeiten und Löschen des Kunden.
 *
 * @author Max Wildberg
 */
@Route(value = "kunden/details/:customerId", layout = MainLayout.class)
@PageTitle("Kunden Details")
@PermitAll
public class CustomerDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomerService customerService;
    private final CompanyService companyService;
    private final PdfGeneratorService pdfGeneratorService;
    private Customer customer;
    private Long customerId;
    private Div content;
    private Tabs tabs;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public CustomerDetailsView(CustomerService customerService, CompanyService companyService, PdfGeneratorService pdfGeneratorService) {
        this.customerService = customerService;
        this.companyService = companyService;
        this.pdfGeneratorService = pdfGeneratorService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
    }

    /**
     * Erstellt den Header der Detailansicht inklusive Avatar in Form der Initiale des Kunden, Name, Kunden-ID und Aktionen.
     * @return HorizontalLayout mit Header-Elementen
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

        Button edit = new Button("Bearbeiten", new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        edit.addClickListener(event -> {
            EditCreateCustomerDialog dialog = new EditCreateCustomerDialog(customer, true, customerService, companyService, this::reloadCustomerData);
            dialog.open();
        });

        // Löschen Button erstellen
        Button delete = new Button("Löschen", new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // [Neu] Berechtigungsprüfung: Darf der User löschen?
        boolean canDelete = AuthorizationUtils.canDeleteCustomers();
        delete.setEnabled(canDelete); // Deaktiviert den Button visuell (grau)

        // Listener nur hinzufügen, wenn erlaubt
        if (canDelete) {
            delete.addClickListener(e -> {
                DeleteDialog<Customer> deleteDialog = new DeleteDialog<>(
                        customer,
                        Customer::getFullName,
                        c -> customerService.deleteCustomer(customer.getCustomerId()),
                        this::navigateBackToOverview
                );
                deleteDialog.open();
            });
        }

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        HorizontalLayout right = new HorizontalLayout(actions);
        right.setSpacing(true);
        right.getStyle().set("margin-left", "auto");

        header.add(left, right);
        return header;
    }

    /**
     * Erstellt die Tabs für Übersicht, Miethistorie und Rechnungen.
     * Registriert den Listener zum Wechseln des Inhalts.
     * @return VerticalLayout mit Tabs und Inhalt
     */
    private VerticalLayout createTabs() {
        Tab uebersicht = new Tab("Übersicht");
        Tab historie = new Tab("Miethistorie");
        Tab dokumente = new Tab("Rechnungen");
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
     * Lädt die Kundendaten anhand der Route-Parameter.
     * Baut die Detailansicht auf oder zeigt Fehlermeldungen an, wenn die ID ungültig ist.
     * @param event BeforeEnterEvent der Route
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
     * Baut den Übersichts-Tab mit allen wichtigen Kundendaten.
     * @return FlexLayout mit den einzelnen Übersichtskarten
     */
    private FlexLayout createOverviewContent() {
        FlexLayout container = new FlexLayout();
        container.setWidthFull();
        container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        container.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
        container.setAlignItems(FlexLayout.Alignment.START);
        container.getStyle().set("gap", "clamp(8px, 1vw, 16px)");

        container.add(createCard("Persönliche Daten", createPersonalData()));
        container.add(createCard("Adresse", createAddressData()));
        container.add(createCard("Kontakt", createContactData()));

        if (customer instanceof BusinessCustomer) {
            container.add(createCard("Gewerbeanschrift", createEmployerData()));
        }

        container.add(createCard("Statistiken", createStatisticsData()));
        container.add(createCard("Dokumente", createDocumentsSummary()));

        container.getChildren().forEach(c -> c.getElement().getStyle().set("flex", "1 1 300px"));

        return container;
    }

    /**
     * Hilfsmethode zur Erstellung einer Übersichtskarte
     * @param title Titel der Karte
     * @param content Inhalt der Karte
     * @return Div als Karte
     */
    private Div createCard(String title, Div content) {
        Div card = new Div();

        H3 h = new H3(title);
        h.getStyle().set("margin", "0 0 12px 0");

        card.getStyle().set("border", "1px solid #e0e0e0");
        card.getStyle().set("padding", "16px");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");
        card.getStyle().set("background-color", "white");
        card.getStyle().set("display", "flex");
        card.getStyle().set("flex-direction", "column");
        card.getStyle().set("gap", "8px");

        card.add(h, content);
        return card;
    }

    /**
     * Hilfsmethoden
     * Baut die persönlichen Daten des Kunden für die Übersichtskarte.
     * @return Div mit persönlichen Daten
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

    /**
     * Baut die Miethistorie des Kunden als Grid auf.
     * @return Div mit Grid
     */
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
     * Baut die Rechnungsübersicht als Karten mit Download-Option.
     * @return Div mit Rechnungsübersicht
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

    /**
     * Ermittelt die Initialen des Kunden für die Detailübersicht.
     * @param first Vorname
     * @param last Nachname
     * @return Initialen
     */
    private String getInitials(String first, String last) {
        String f = first != null && !first.isBlank() ? first.substring(0, 1).toUpperCase() : "";
        String l = last != null && !last.isBlank() ? last.substring(0, 1).toUpperCase() : "";
        return (f + l).isEmpty() ? "?" : f + l;
    }

    /**
     * Lädt die Kundendaten neu und aktualisiert die Tabs entsprechend.
     */
    private void reloadCustomerData(){
        if (customerId == null) return;

        this.customer = customerService.getCustomerByID(customerId);

        content.removeAll();

        if (tabs.getSelectedTab().getLabel().equals("Übersicht")) {
            content.add(createOverviewContent());
        } else if (tabs.getSelectedTab().getLabel().equals("Miethistorie")) {
            content.add(createHistoryContent());
        } else {
            content.add(createInvoiceContent());
        }

        removeAll();
        add(createHeader());
        add(createTabs());
    }

    /**
     * Navigiert zurück zur Kundenübersicht.
     */
    private void navigateBackToOverview() {
        UI.getCurrent().navigate("kunden");
    }
}