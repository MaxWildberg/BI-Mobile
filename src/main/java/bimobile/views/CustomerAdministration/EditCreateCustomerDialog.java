package bimobile.views.CustomerAdministration;

import bimobile.controller.CustomerController;
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

    private final CustomerController controller;
    private final Runnable onSaveSuccess;
    private Customer customer;
    private boolean editMode;

    private final Binder<Customer> binder = new Binder<>(Customer.class);

    private final DatePicker geburtsdatum = new DatePicker("Geburtsdatum");
    private final TextField vorname = new TextField("Vorname");
    private final TextField nachname = new TextField("Nachname");

    private final TextField strasse = new TextField("Straße und Hausnummer");
    private final TextField stadt = new TextField("Wohnort");
    private final TextField plz = new TextField("Postleitzahl");

    private final TextField ausweisnummer = new TextField("Ausweisnummer");
    private final TextField fuehrerscheinnummer = new TextField("Führerscheinnummer");

    private final EmailField email = new EmailField("E-Mail");
    private final TextField telefonnummer = new TextField("Telefonnummer");

    private final Button saveButton = new Button("Kunde registrieren");
    private final Button cancelButton = new Button("Abbrechen");

    public EditCreateCustomerDialog(Customer customer, boolean editMode, CustomerController controller, Runnable onSaveSuccess) {
        this.customer = customer;
        this.editMode = editMode;
        this.controller = controller;
        this.onSaveSuccess = onSaveSuccess;

        buildUI();
        configureBinder();
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
        add(new FormLayout(vorname, nachname, geburtsdatum));

        // --- Adresse ---
        add(sectionHeader("Adresse", VaadinIcon.HOME));
        add(new FormLayout(strasse, stadt, plz));

        // --- Dokumente ---
        add(sectionHeader("Ausweisdokumente", VaadinIcon.CREDIT_CARD));
        add(new FormLayout(ausweisnummer, fuehrerscheinnummer));

        // --- Kontakt ---
        add(sectionHeader("Kontaktdaten", VaadinIcon.ENVELOPE));
        add(new FormLayout(email, telefonnummer));

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

        binder.forField(vorname)
                .asRequired("Bitte Vornamen eingeben")
                .bind(Customer::getName, null);

        binder.forField(nachname)
                .asRequired("Bitte Nachnamen eingeben")
                .bind(Customer::getLastname, Customer::setLastname);

        binder.forField(strasse)
                .asRequired("Bitte Straße eingeben")
                .bind(Customer::getAddress, Customer::setAddress);

        binder.forField(stadt)
                .asRequired("Bitte Wohnort eingeben")
                .bind(Customer::getResidence, Customer::setResidence);

        binder.forField(plz)
                .asRequired("Bitte Postleitzahl eingeben")
                .bind(Customer::getZip, Customer::setZip);

        binder.forField(ausweisnummer)
                .asRequired("Bitte Ausweisnummer eingeben")
                .bind(Customer::getIdCardNumber, null);

        binder.forField(fuehrerscheinnummer)
                .asRequired("Bitte Führerscheinnummer eingeben")
                .bind(Customer::getDriverslicenseID, null);

        binder.forField(email)
                .asRequired("Bitte E-Mail eingeben")
                .bind(Customer::getEmail, Customer::setEmail);

        binder.forField(telefonnummer)
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
