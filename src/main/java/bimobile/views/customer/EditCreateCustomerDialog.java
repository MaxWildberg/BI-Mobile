package bimobile.views.customer;

import bimobile.model.customer.*;
import bimobile.service.customer.*;
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
 * Dialog zur Erstellung oder Bearbeitung eines Kunden.
 * Unterstützt private und Business-Kunden.
 * Übergibt Daten an den CustomerService zur Persistierung.
 * Zeigt Validierungsergebnisse und Notifications im UI an.
 *
 * @author Max Wildberg
 */
public class EditCreateCustomerDialog extends Dialog {

    private final CustomerService customerService;
    private final CompanyService companyService;
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

    /**
     * Erstellt den Dialog für einen Kunden.
     *
     * @param customer der Kunde, der bearbeitet oder neu erstellt wird
     * @param editMode true, wenn Bearbeitung eines bestehenden Kunden, false bei Neuanlage
     * @param service Service zur Verwaltung von Kunden
     * @param companyService Service zur Verwaltung von Unternehmen
     * @param onSaveSuccess Callback, der nach erfolgreichem Speichern ausgeführt wird (z. B. Grid aktualisieren)
     */
    public EditCreateCustomerDialog(Customer customer,
                                    boolean editMode,
                                    CustomerService service,
                                    CompanyService companyService,
                                    Runnable onSaveSuccess) {
        this.customer = customer;
        this.editMode = editMode;
        this.customerService = service;
        this.companyService = companyService;
        this.onSaveSuccess = onSaveSuccess;

        buildUI();
        configureBinder();

        CustomerFormDTO dto = CustomerFormDTO.fromCustomer(customer);
        binder.readBean(dto);
    }

    /**
     * Baut die Benutzeroberfläche des Dialogs auf,
     * zeigt Felder je nach Kundentyp an
     * und bindet die Button-Events.
     */
    private void buildUI() {

        setHeaderTitle(editMode ? "Kunden Details bearbeiten" : "Neuen Kunden anlegen");

        Paragraph subtitle = new Paragraph("Erfassen Sie alle erforderlichen Kundendaten");
        subtitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        add(subtitle);

        // Persönliche Daten
        birthday.setHelperText("Person muss mindestens 18 Jahre alt sein");
        birthday.setMax(LocalDate.now().minusYears(18));
        birthday.setMin(LocalDate.now().minusYears(120));
        title.setItems("Herr", "Frau", "Divers");
        title.setValue("Frau");
        add(sectionHeader("Persönliche Daten", VaadinIcon.USER));
        add(new FormLayout(title, firstName, lastName, birthday));

        // Adresse
        add(sectionHeader("Adresse", VaadinIcon.HOME));
        add(new FormLayout(street, city, zip, country));

        // Ausweisdokumente
        add(sectionHeader("Ausweisdokumente", VaadinIcon.CREDIT_CARD));
        add(new FormLayout(idCardNum, driversLicense));

        // Kontakt
        add(sectionHeader("Kontaktdaten", VaadinIcon.ENVELOPE));
        add(new FormLayout(email, telephone));

        // Unternehmen
        if (customer instanceof BusinessCustomer) {
            add(sectionHeader("Geschäftsinfo", VaadinIcon.BUILDING));
            add(new FormLayout(companyCombo, addCompanyButton));
        }

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setText(editMode ? "Speichern" : "Registrieren");
        addCompanyButton.addClickListener(addCompanyEvent -> {
            EditCreateCompanyDialog addCompanyDialog = new EditCreateCompanyDialog(companyService, false, null);
            addCompanyDialog.addDetachListener(comboEvent -> {
                Company saved = addCompanyDialog.getSavedCompany();
                if (saved != null) {
                    companyCombo.setItems(companyService.getAllCompanies());
                    companyCombo.setValue(saved);
                }
            });
            addCompanyDialog.open();
        });
        cancelButton.addClickListener(e -> close());
        saveButton.addClickListener(e -> onSave());

        getFooter().add(cancelButton, saveButton);
    }

    /**
     * Wiederverwendbare Hilfsmethode zum erstellen eines Abschnitts-Headers mit Icon.
     *
     * @param title Überschrift des Abschnitts
     * @param iconType Icon, das vor der Überschrift angezeigt wird
     * @return HorizontalLayout mit Icon und Label
     */
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

    /**
     * Konfiguriert den Binder für Validierung und Datenbindung.
     * Bindet alle Formularfelder an die DTO-Felder.
     */
    private void configureBinder() {
        binder.bindInstanceFields(this);
        String regex = "^[a-zA-Z0-9 ]+$";

        binder.forField(title)
                .bind(CustomerFormDTO::getTitle, CustomerFormDTO::setTitle);

        binder.forField(firstName)
                .asRequired("Vorname erforderlich")
                .withValidator(s -> s != null && s.trim().matches("[A-Za-z ]+"),
                        "Nur Buchstaben ohne Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getFirstname, CustomerFormDTO::setFirstname);

        binder.forField(lastName)
                .asRequired("Nachname erforderlich")
                .withValidator(s -> s != null && s.trim().matches("[A-Za-z ]+"),
                        "Nur Buchstaben ohne Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getLastname, CustomerFormDTO::setLastname);

        binder.forField(birthday)
                .asRequired("Geburtsdatum erforderlich")
                .bind(CustomerFormDTO::getBirthday, CustomerFormDTO::setBirthday);

        binder.forField(email)
                .asRequired("E-Mail erforderlich")
                .withValidator(new com.vaadin.flow.data.validator.EmailValidator(
                        "Bitte eine gültige E-Mail-Adresse eingeben"))
                .bind(CustomerFormDTO::getEmail, CustomerFormDTO::setEmail);

        binder.forField(telephone)
                .asRequired("Telefonnummer erforderlich")
                .withValidator(tel -> tel.matches("\\d+"), "Nur Zahlen erlaubt")
                .bind(CustomerFormDTO::getTelephone, CustomerFormDTO::setTelephone);

        binder.forField(street)
                .asRequired("Straße erforderlich")
                .withValidator(street -> street.matches(regex),
                        "Nur Buchstaben, Zahlen und Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getStreet, CustomerFormDTO::setStreet);

        binder.forField(zip)
                .asRequired("PLZ erforderlich")
                .withValidator(zip -> zip.matches("\\d{5}"),
                        "PLZ muss genau 5 Ziffern enthalten")
                .bind(CustomerFormDTO::getZip, CustomerFormDTO::setZip);

        binder.forField(city)
                .asRequired("Ort erforderlich")
                .withValidator(city -> city.matches(regex),
                        "Nur Buchstaben, Zahlen und Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getCity, CustomerFormDTO::setCity);

        binder.forField(country)
                .asRequired("Land erforderlich")
                .withValidator(s -> s != null && s.trim().matches("[A-Za-z]+"),
                        "Nur Buchstaben ohne Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getCountry, CustomerFormDTO::setCountry);

        binder.forField(driversLicense)
                .asRequired("Führerscheinnummer erforderlich")
                .withValidator(s -> s.matches("[A-Z0-9]+"),
                        "Nur Buchstaben und Zahlen ohne Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getDriversLicense, CustomerFormDTO::setDriversLicense);

        binder.forField(idCardNum)
                .asRequired("Ausweis erforderlich")
                .withValidator(s -> s.matches("[A-Z0-9]+"),
                        "Nur Buchstaben und Zahlen ohne Leerzeichen erlaubt")
                .bind(CustomerFormDTO::getIdCardNum, CustomerFormDTO::setIdCardNum);

        if (customer instanceof BusinessCustomer) {
            companyCombo.setItems(companyService.getAllCompanies());
            companyCombo.setItemLabelGenerator(Company::getName);

            binder.forField(companyCombo)
                    .asRequired("Bitte Unternehmen auswählen")
                    .bind(CustomerFormDTO::getCompany, CustomerFormDTO::setCompany);
        } else {
            businessInfoHeader.setVisible(false);
            businessInfoLayout.setVisible(false);
        }
    }

    /**
     * Führt die Speicherung des Formulars aus.
     * Ruft CustomerService auf und zeigt Erfolgsmeldungen.
     * Bei Fehlern werden Notifications angezeigt.
     */
    private void onSave() {
        CustomerFormDTO dto = new CustomerFormDTO();
        if (binder.writeBeanIfValid(dto)) {
            try {
                Customer updatedCustomer = mapDtoToCustomer(dto, customer);
                if (editMode) {
                    customerService.updateCustomer(updatedCustomer);
                    Notification.show("Kunde erfolgreich aktualisiert.");
                } else {
                    customerService.registerCustomer(updatedCustomer);
                    Notification.show("Kunde erfolgreich angelegt.");
                }

                onSaveSuccess.run();
                close();

            } catch (CustomerNotFoundException | DuplicateCustomerException | CustomerTooYoungException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Unerwarteter Fehler: " + ex.getMessage());
            }
        }
    }

    /**
     * Überträgt die Daten aus dem DTO in das Customer-Objekt.
     *
     * @param dto das Formular-Datenobjekt
     * @param customer das Ziel-Customer-Objekt
     * @return das aktualisierte Customer-Objekt
     */
    private Customer mapDtoToCustomer(CustomerFormDTO dto, Customer customer) {
        customer.setPersonalData(new PersonalData(dto.getTitle(), dto.getFirstname(), dto.getLastname(), dto.getBirthday()));
        customer.setAddress(new Address(dto.getStreet(), dto.getZip(), dto.getCity(), dto.getCountry()));
        customer.setContactInfo(new ContactInfo(dto.getEmail(), dto.getTelephone()));
        customer.setIdentification(new Identification(dto.getDriversLicense(), dto.getIdCardNum()));

        if (customer instanceof BusinessCustomer bc && dto.getCompany() != null) {
            Company company = companyService.getCompanyById(dto.getCompanyId());
            bc.setCompany(company);
        }

        return customer;
    }
}
