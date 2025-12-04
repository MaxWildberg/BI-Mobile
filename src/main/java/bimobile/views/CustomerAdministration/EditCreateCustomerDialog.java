package bimobile.views.CustomerAdministration;

import bimobile.controller.CustomerManager;
import bimobile.model.BusinessCustomer;
import bimobile.model.Customer;
import bimobile.model.CustomerInterface;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

/**
 * Beschreibung:
 * User Interface für Formular welches den Nutzer einen Kunden erstellen bzw. bearbeiten lässt.
 * Gleiches gilt für Business Kunden -> UI passt sich dementsprechend an.
 * Bekommt Kunden-Objekt von CustomerManager bzw. gibt es zum Speichern dahin weiter.
 *
 * @author Max Wildberg
 */

public class EditCreateCustomerDialog extends Dialog {

    private final CustomerManager manager;
    private final Runnable onSaveSuccess;
    private CustomerInterface customer;
    private boolean editMode;

    private final Binder<CustomerInterface> binder = new Binder<>(CustomerInterface.class);

    private final DatePicker birthdate = new DatePicker("Geburtsdatum");
    private final TextField firstName = new TextField("Vorname");
    private final TextField lastName = new TextField("Nachname");

    private final TextField street = new TextField("Straße und Hausnummer");
    private final TextField city = new TextField("Wohnort");
    private final TextField zip = new TextField("Postleitzahl");
    private final TextField country = new TextField("Land");

    private final TextField idCardNum = new TextField("Ausweisnummer");
    private final TextField driversLicense = new TextField("Führerscheinnummer");

    private final EmailField email = new EmailField("E-Mail");
    private final TextField telephone = new TextField("Telefonnummer");

    private final TextField company = new TextField("Unternehmen");
    private final TextField companyAddress = new TextField("Unternehmensanschrift");
    private HorizontalLayout businessInfoHeader = sectionHeader("Geschäftsinfo", VaadinIcon.BUILDING);
    private FormLayout businessInfoLayout = new FormLayout(company, companyAddress);

    private final Button saveButton = new Button("Kunde registrieren");
    private final Button cancelButton = new Button("Abbrechen");

    // Konstruktor für Formular, verwendet in CustomerOverview und CustomerDetailsView
    public EditCreateCustomerDialog(CustomerInterface customer, boolean editMode, CustomerManager manager, Runnable onSaveSuccess) {
        if (customer instanceof Customer) {
            this.customer = customer != null ? customer : new Customer();
        } else {
            this.customer = customer != null ? customer : new BusinessCustomer();
        }

        this.editMode = editMode;
        this.manager = manager;
        this.onSaveSuccess = onSaveSuccess;

        buildUI();
        configureBinder();

        binder.readBean(customer);
    }

    // Baut das Fomular auf, je nach Use Case -> Kunde, Business Kunde? / Bearbeiten, neu erstellen?
    private void buildUI() {

        setHeaderTitle(editMode ? "Kunden Details bearbeiten" : "Neuen Kunden anlegen");

        Paragraph subtitle = new Paragraph("Erfassen Sie alle erforderlichen Kundendaten");
        subtitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        add(subtitle);

        // --- Persönliche Daten ---
        add(sectionHeader("Persönliche Daten", VaadinIcon.USER));
        add(new FormLayout(firstName, lastName, birthdate));

        // --- Adresse ---
        add(sectionHeader("Adresse", VaadinIcon.HOME));
        add(new FormLayout(street, city, zip, country));

        // --- Dokumente ---
        add(sectionHeader("Ausweisdokumente", VaadinIcon.CREDIT_CARD));
        add(new FormLayout(idCardNum, driversLicense));

        // --- Kontakt ---
        add(sectionHeader("Kontaktdaten", VaadinIcon.ENVELOPE));
        add(new FormLayout(email, telephone));

        // --- Unternehmen ----
        add(businessInfoHeader, businessInfoLayout);

        // --- Footer Buttons ---
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setText(editMode ? "Speichern" : "Registrieren");
        cancelButton.addClickListener(e -> close());

        saveButton.addClickListener(e -> onSave());

        getFooter().add(cancelButton, saveButton);
    }

    // Hilfsmethode für mehrfach wiederkehrende Erstellung eines Absatzes in UI
    private HorizontalLayout sectionHeader(String title, VaadinIcon iconType) {
        Icon icon = iconType.create();
        icon.setColor("var(--lumo-primary-color)");

        Paragraph label = new Paragraph(title);
        label.getStyle().set("font-size", "var(--lumo-font-size-m)");

        HorizontalLayout layout = new HorizontalLayout(icon, label);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setWidthFull();
        return layout;
    }

    // Konfiguriert Binder, liest eingegebene Daten aus dem Formular und erstellt/updated damit das Kunden-Objekt
    // Passt sich ebenfalls an Objekt an -> Kunde oder Business Kunde
    private void configureBinder() {

        binder.forField(birthdate)
                .asRequired("Bitte Geburtsdatum eingeben")
                .bind(CustomerInterface::getBirthday, CustomerInterface::setBirthday);

        binder.forField(firstName)
                .asRequired("Bitte Vornamen eingeben")
                .bind(CustomerInterface::getName, CustomerInterface::setName);

        binder.forField(lastName)
                .asRequired("Bitte Nachnamen eingeben")
                .bind(CustomerInterface::getLastname, CustomerInterface::setLastname);

        binder.forField(street)
                .asRequired("Bitte Straße eingeben")
                .bind(CustomerInterface::getAddress, CustomerInterface::setAddress);

        binder.forField(city)
                .asRequired("Bitte Wohnort eingeben")
                .bind(CustomerInterface::getResidence, CustomerInterface::setResidence);

        binder.forField(zip)
                .asRequired("Bitte Postleitzahl eingeben")
                .bind(CustomerInterface::getZip, CustomerInterface::setZip);

        binder.forField(country)
                .asRequired("Bitte Land eingeben")
                .bind(CustomerInterface::getCountry, CustomerInterface::setCountry);

        binder.forField(idCardNum)
                .asRequired("Bitte Ausweisnummer eingeben")
                .bind(CustomerInterface::getIdCardNumber, CustomerInterface::setIdCardNumber);

        binder.forField(driversLicense)
                .asRequired("Bitte Führerscheinnummer eingeben")
                .bind(CustomerInterface::getDriverslicenseID, CustomerInterface::setDriverslicenseID);

        binder.forField(email)
                .asRequired("Bitte E-Mail eingeben")
                .bind(CustomerInterface::getEmail, CustomerInterface::setEmail);

        binder.forField(telephone)
                .asRequired("Bitte Telefonnummer eingeben")
                .bind(CustomerInterface::getTelephone, CustomerInterface::setTelephone);

        if (customer instanceof BusinessCustomer) {

            binder.forField(company)
                    .asRequired("Bitte Unternehmen eingeben")
                    .bind(
                            c -> ((BusinessCustomer) c).getCompany(),
                            (c, v) -> ((BusinessCustomer) c).setCompany(v)
                    );

            binder.forField(companyAddress)
                    .bind(
                            c -> ((BusinessCustomer) c).getCompanyAddress(),
                            (c, v) -> ((BusinessCustomer) c).setCompanyAddress(v)
                    );

            businessInfoHeader.setVisible(true);
            businessInfoLayout.setVisible(true);

        } else {
            businessInfoHeader.setVisible(false);
            businessInfoLayout.setVisible(false);
        }

    }

    // Führt Binder aus sobald das Formular gespeichert wird
    // Gibt Anweisung an CustomerManager für update oder neues Kunden-Objekt
    // Gibt Erfolgsmeldung in UI, wenn erfolgreich
    private void onSave() {

        if (binder.writeBeanIfValid(customer)) {

            String msg;

            if (editMode) {
                msg = manager.updateCustomer(customer);
            } else {
                msg = manager.registerCustomer(customer);
            }

            Notification.show(msg);

            if (msg.startsWith("Erfolg")) {
                onSaveSuccess.run(); // refresh grid
                close();
            }
        }
    }

}
