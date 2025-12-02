package bimobile.views.CustomerAdministration;

import bimobile.controller.CustomerManager;
import bimobile.model.Customer;
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

public class EditCreateCustomerDialog extends Dialog {

    private final CustomerManager controller;
    private final Runnable onSaveSuccess;
    private Customer customer;
    private boolean editMode;

    private final Binder<Customer> binder = new Binder<>(Customer.class);

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

    private final Button saveButton = new Button("Kunde registrieren");
    private final Button cancelButton = new Button("Abbrechen");

    public EditCreateCustomerDialog(Customer customer, boolean editMode, CustomerManager controller, Runnable onSaveSuccess) {
        this.customer = customer != null ? customer : new Customer();
        this.editMode = editMode;
        this.controller = controller;
        this.onSaveSuccess = onSaveSuccess;

        buildUI();
        configureBinder();

        binder.readBean(customer);
    }

    // ------------------------------------------------------------
    // UI
    // ------------------------------------------------------------
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

        // --- Footer Buttons ---
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setText(editMode ? "Speichern" : "Registrieren");
        cancelButton.addClickListener(e -> close());

        saveButton.addClickListener(e -> onSave());

        getFooter().add(cancelButton, saveButton);
    }

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

    // ------------------------------------------------------------
    // Binder-Konfiguration
    // ------------------------------------------------------------
    private void configureBinder() {

        binder.forField(birthdate)
                .asRequired("Bitte Geburtsdatum eingeben")
                .bind(Customer::getBirthday, Customer::setBirthday);

        binder.forField(firstName)
                .asRequired("Bitte Vornamen eingeben")
                .bind(Customer::getName, Customer::setName);

        binder.forField(lastName)
                .asRequired("Bitte Nachnamen eingeben")
                .bind(Customer::getLastname, Customer::setLastname);

        binder.forField(street)
                .asRequired("Bitte Straße eingeben")
                .bind(Customer::getAddress, Customer::setAddress);

        binder.forField(city)
                .asRequired("Bitte Wohnort eingeben")
                .bind(Customer::getResidence, Customer::setResidence);

        binder.forField(zip)
                .asRequired("Bitte Postleitzahl eingeben")
                .bind(Customer::getZip, Customer::setZip);

        binder.forField(country)
                .asRequired("Bitte Land eingeben")
                .bind(Customer::getCountry, Customer::setCountry);

        binder.forField(idCardNum)
                .asRequired("Bitte Ausweisnummer eingeben")
                .bind(Customer::getIdCardNumber, Customer::setIdCardNumber);

        binder.forField(driversLicense)
                .asRequired("Bitte Führerscheinnummer eingeben")
                .bind(Customer::getDriverslicenseID, Customer::setDriverslicenseID);

        binder.forField(email)
                .asRequired("Bitte E-Mail eingeben")
                .bind(Customer::getEmail, Customer::setEmail);

        binder.forField(telephone)
                .asRequired("Bitte Telefonnummer eingeben")
                .bind(Customer::getTelephone, Customer::setTelephone);
    }

    // ------------------------------------------------------------
    // Save Handler
    // ------------------------------------------------------------
    private void onSave() {

        if (binder.writeBeanIfValid(customer)) {

            String msg;

            if (editMode) {
                msg = controller.updateCustomer(customer);
            } else {
                msg = controller.registerCustomer(customer);
                /*msg = controller.registerCustomer(
                        vorname.getValue(),
                        nachname.getValue(),
                        geburtsdatum.getValue(),
                        strasse.getValue(),
                        plz.getValue(),
                        stadt.getValue(),
                        fuehrerscheinnummer.getValue(),
                        ausweisnummer.getValue(),
                        email.getValue(),
                        telefonnummer.getValue()
                );*/
            }

            Notification.show(msg);

            if (msg.startsWith("Erfolg")) {
                onSaveSuccess.run(); // refresh grid
                close();
            }
        }
    }

}