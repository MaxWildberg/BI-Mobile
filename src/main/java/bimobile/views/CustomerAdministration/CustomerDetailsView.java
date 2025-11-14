package bimobile.views.CustomerAdministration;

import bimobile.dao.CustomerDAO;
import bimobile.model.Customer;
import bimobile.service.CustomerService;
import bimobile.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route(value = "customer-details", layout = MainLayout.class)
@PageTitle("Kunde — Übersicht")
public class CustomerDetailsView extends VerticalLayout {

    private final CustomerService customerService;
    private Customer customer;

    public CustomerDetailsView(CustomerService customerService) {
        this.customerService = customerService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        this.customer = customerService.getCustomerByID(1L);

        add(createHeader());
        add(createTabs());
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
        Span id = new Span("Kunden-ID: " + customer.getId());
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

        Button delete = new Button("Löschen", new Icon(VaadinIcon.DEL));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

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
        Tabs tabs = new Tabs(uebersicht, historie, dokumente);

        Div content = new Div();
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

        rightCol.add(createCard("Statistiken", createStatisticsData()));
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

    private Div createStatisticsData() {
        Div d = new Div();

        d.add(new Paragraph("Anzahl Vermietungen: " + customer.getRentCount()));
        d.add(new Hr());
        d.add(new Paragraph("Gesamtumsatz: € " + String.format("%.2f", customer.getTotalRevenue())));

        return d;
    }

    private Div createDocumentsSummary() {
        Div d = new Div();
        d.add(new Paragraph("Führerscheinnummer: " + customer.getDriverslicenseID()));
        d.add(new Paragraph("Ausweisnummer: " + customer.getIdCardNumber()));
        return d;
    }

    private Div createHistoryContent() {
        Div d = new Div();
        d.add(new Paragraph("Miethistorie (Platzhalter)"));
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
}
