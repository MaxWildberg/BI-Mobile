package bimobile.views.customer;

import bimobile.model.customer.Company;
import bimobile.service.customer.CompanyService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import javax.annotation.Nullable;

/**
 * Dialog zum Anlegen einer neuen {@link Company}.
 * Stellt ein Formular zur Eingabe von Firmenname und Adresse bereit
 * und speichert die Firma nach erfolgreicher Validierung.
 *
 * @author Max Wildberg
 */
public class EditCreateCompanyDialog extends Dialog {

    /**
     * Die gespeicherte Firma nach erfolgreichem Speichervorgang.
     */
    private final CompanyService service;
    private Company savedCompany;
    private boolean editMode;
    private Company currentCompany;

    /**
     * Erstellt einen Dialog zum Anlegen einer neuen Firma.
     *
     * @param service Service zum Speichern der Firma
     */
    public EditCreateCompanyDialog(CompanyService service, boolean editMode, Company updateCompany) {
        this.service = service;
        this.editMode = editMode;

        if(editMode && updateCompany != null) {
            this.currentCompany = updateCompany;
        } else {
            this.currentCompany = new Company();
        }

        setHeaderTitle(editMode ? "Firmen Details bearbeiten" : "Neue Firma anlegen");

        TextField nameField = new TextField("Firmenname");
        nameField.setRequired(true);

        TextField addressField = new TextField("Adresse");
        addressField.setRequired(true);

        if (editMode) {
            nameField.setValue(updateCompany.getName());
            addressField.setValue(updateCompany.getAddress());
        }

        FormLayout form = new FormLayout(nameField, addressField);

        Binder<Company> binder = new Binder<>(Company.class);
        binder.setBean(this.currentCompany);

        binder.forField(nameField)
                .asRequired("Name erforderlich")
                .bind(Company::getName, Company::setName);

        binder.forField(addressField)
                .asRequired("Adresse erforderlich")
                .bind(Company::getAddress, Company::setAddress);

        Button save = new Button("Speichern", e -> {
            if (binder.validate().isOk()) {
                //binder.writeBeanIfValid(currentCompany);
                if (editMode) {
                    this.savedCompany = service.updateCompany(currentCompany);
                    Notification.show("Firma erfolgreich aktualisiert.");
                } else {
                    this.savedCompany = service.saveCompany(currentCompany);
                    Notification.show("Firma erfolgreich angelegt.");
                }
                close();
            }
        });

        Button cancel = new Button("Abbrechen", e -> close());
        getFooter().add(cancel, save);
        add(form);
    }

    /**
     * Liefert die gespeicherte Firma zurück.
     *
     * @return die gespeicherte {@link Company} oder null,
     *         falls nicht gespeichert wurde
     */
    public Company getSavedCompany() {
        return savedCompany;
    }
}
