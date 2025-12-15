package bimobile.views.customer;

import bimobile.model.customer.Company;
import bimobile.service.customer.CompanyService;
import bimobile.service.customer.CustomerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

/**
 * Dialog zum Anlegen einer neuen {@link Company}.
 * Stellt ein Formular zur Eingabe von Firmenname und Adresse bereit
 * und speichert die Firma nach erfolgreicher Validierung.
 *
 * @author Max Wildberg
 */
public class AddCompanyDialog extends Dialog {

    /**
     * Die gespeicherte Firma nach erfolgreichem Speichervorgang.
     */
    private Company savedCompany;

    /**
     * Erstellt einen Dialog zum Anlegen einer neuen Firma.
     *
     * @param service Service zum Speichern der Firma
     */
    public AddCompanyDialog(CompanyService service) {
        setHeaderTitle("Neue Firma anlegen");

        TextField nameField = new TextField("Firmenname");
        nameField.setRequired(true);

        TextField addressField = new TextField("Adresse");
        addressField.setRequired(true);

        FormLayout form = new FormLayout(nameField, addressField);

        Binder<Company> binder = new Binder<>(Company.class);
        Company newCompany = new Company();

        binder.forField(nameField)
                .asRequired("Name erforderlich")
                .bind(Company::getName, Company::setName);

        binder.forField(addressField)
                .asRequired("Adresse erforderlich")
                .bind(Company::getAddress, Company::setAddress);

        Button save = new Button("Speichern", e -> {
            if (binder.validate().isOk()) {
                binder.writeBeanIfValid(newCompany);
                this.savedCompany = service.saveCompany(newCompany);
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
