package bimobile.views.customer;

import bimobile.model.customer.*;
import bimobile.service.CustomerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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

import java.time.LocalDate;

/**
 * Beschreibung:
 * User Interface für Formular welches den Nutzer einen Kunden erstellen bzw. bearbeiten lässt.
 * Gleiches gilt für Business Kunden -> UI passt sich dementsprechend an.
 * Bekommt Kunden-Objekt von CustomerManager bzw. gibt es zum Speichern dahin weiter.
 *
 * @author Max Wildberg
 */

public class EditCreateCustomerDialog extends Dialog {

    private final CustomerService service;
    private final Runnable onSaveSuccess;
    private Customer customer;
    private boolean editMode;

    private final Binder<CustomerFormDTO> binder = new Binder<>(CustomerFormDTO.class);

    private final ComboBox<String> title = new ComboBox<>("Anrede/Geschlecht");
    private final DatePicker birthday = new DatePicker("Geburtsdatum");
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

    private final ComboBox<Company> companyCombo = new ComboBox<>();
    private final Button addCompanyButton = new Button("Neue Firma anlegen");
    private HorizontalLayout businessInfoHeader = sectionHeader("Geschäftsinfo", VaadinIcon.BUILDING);
    private FormLayout businessInfoLayout = new FormLayout(companyCombo);

    private final Button saveButton = new Button("Kunde registrieren");
    private final Button cancelButton = new Button("Abbrechen");

    // Konstruktor für Formular, verwendet in CustomerOverview und CustomerDetailsView
    public EditCreateCustomerDialog(Customer customer, boolean editMode, CustomerService service, Runnable onSaveSuccess) {
        this.customer = customer;
        this.editMode = editMode;
        this.service = service;
        this.onSaveSuccess = onSaveSuccess;

        buildUI();
        configureBinder();

        CustomerFormDTO dto = CustomerFormDTO.fromCustomer(customer);
        binder.readBean(dto);
    }

    // Baut das Fomular auf, je nach Use Case -> Kunde, Business Kunde? / Bearbeiten, neu erstellen?
    private void buildUI() {

        setHeaderTitle(editMode ? "Kunden Details bearbeiten" : "Neuen Kunden anlegen");

        Paragraph subtitle = new Paragraph("Erfassen Sie alle erforderlichen Kundendaten");
        subtitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        add(subtitle);

        // --- Persönliche Daten ---
        birthday.setHelperText("Person muss mindestens 18 Jahre alt sein");
        birthday.setMax(LocalDate.now().minusYears(18));
        birthday.setMin(LocalDate.now().minusYears(120));
        title.setItems("Herr", "Frau", "Divers");
        title.setValue("Frau");
        add(sectionHeader("Persönliche Daten", VaadinIcon.USER));
        add(new FormLayout(title, firstName, lastName, birthday));

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
        if (customer instanceof BusinessCustomer) {
            add(sectionHeader("Geschäftsinfo", VaadinIcon.BUILDING));
            add(new FormLayout(companyCombo, addCompanyButton));
        }

        // --- Footer Buttons ---
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setText(editMode ? "Speichern" : "Registrieren");
        addCompanyButton.addClickListener(e -> {
            AddCompanyDialog addCompanyDialog = new AddCompanyDialog(service, companyCombo);
            addCompanyDialog.open();
        });
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

    private void configureBinder() {
        // bindInstanceFields nutzt die Feldnamen der DTO-Felder
        binder.bindInstanceFields(this);

        // Basis-Felder
        binder.forField(title)
                .bind(CustomerFormDTO::getTitle, CustomerFormDTO::setTitle);

        binder.forField(firstName)
                .asRequired("Vorname erforderlich")
                .bind(CustomerFormDTO::getFirstname, CustomerFormDTO::setFirstname);

        binder.forField(lastName)
                .asRequired("Nachname erforderlich")
                .bind(CustomerFormDTO::getLastname, CustomerFormDTO::setLastname);

        binder.forField(birthday)
                .asRequired("Geburtsdatum erforderlich")
                .bind(CustomerFormDTO::getBirthday, CustomerFormDTO::setBirthday);

        binder.forField(email)
                .asRequired("E-Mail erforderlich")
                .bind(CustomerFormDTO::getEmail, CustomerFormDTO::setEmail);

        binder.forField(telephone)
                .asRequired("Telefonnummer erforderlich")
                .bind(CustomerFormDTO::getTelephone, CustomerFormDTO::setTelephone);

        // Address
        binder.forField(street)
                .asRequired("Straße erforderlich")
                .bind(CustomerFormDTO::getStreet, CustomerFormDTO::setStreet);

        binder.forField(zip)
                .asRequired("PLZ erforderlich")
                .bind(CustomerFormDTO::getZip, CustomerFormDTO::setZip);

        binder.forField(city)
                .asRequired("Ort erforderlich")
                .bind(CustomerFormDTO::getCity, CustomerFormDTO::setCity);

        binder.forField(country)
                .asRequired("Land erforderlich")
                .bind(CustomerFormDTO::getCountry, CustomerFormDTO::setCountry);

        // Dokumente
        binder.forField(driversLicense)
                .asRequired("Führerscheinnummer erforderlich")
                .bind(CustomerFormDTO::getDriversLicense, CustomerFormDTO::setDriversLicense);

        binder.forField(idCardNum)
                .asRequired("Ausweis erforderlich")
                .bind(CustomerFormDTO::getIdCardNum, CustomerFormDTO::setIdCardNum);


        // BusinessCustomer-spezifisch
        if (customer instanceof BusinessCustomer) {
            companyCombo.setItems(service.getAllCompanies());
            companyCombo.setItemLabelGenerator(Company::getName);

            binder.forField(companyCombo)
                    .asRequired("Bitte Unternehmen auswählen")
                    .bind(CustomerFormDTO::getCompany, CustomerFormDTO::setCompany);
        } else {
            businessInfoHeader.setVisible(false);
            businessInfoLayout.setVisible(false);
        }
    }

    // Führt Binder aus sobald das Formular gespeichert wird
    // Gibt Anweisung an CustomerManager für update oder neues Kunden-Objekt
    // Gibt Erfolgsmeldung in UI, wenn erfolgreich
    private void onSave() {
    CustomerFormDTO dto = new CustomerFormDTO();
        if (binder.writeBeanIfValid(dto)) {
            Customer updatedCustomer = mapDtoToCustomer(dto, customer);
            String msg;
            if (editMode) {
                msg = service.updateCustomer(updatedCustomer);
            } else {
                msg = service.registerCustomer(updatedCustomer);
            }

            Notification.show(msg);

            if (msg.startsWith("Erfolg")) {
                onSaveSuccess.run(); // refresh grid
                close();
            }
        }
    }
    private Customer mapDtoToCustomer(CustomerFormDTO dto, Customer customer) {
        customer.setPersonalData(new PersonalData(dto.getTitle(), dto.getFirstname(), dto.getLastname(), dto.getBirthday()));
        customer.setAddress(new Address(dto.getStreet(), dto.getZip(), dto.getCity(), dto.getCountry()));
        customer.setContactInfo(new ContactInfo(dto.getEmail(), dto.getTelephone()));
        customer.setIdentification(new Identification(dto.getDriversLicense(), dto.getIdCardNum()));

        if (customer instanceof BusinessCustomer bc && dto.getCompany() != null) {
            Company company = service.getCompanyById(dto.getCompanyId());
            bc.setCompany(company);
        }

        return customer;
    }


}