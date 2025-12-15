package bimobile.views.customer;

import bimobile.model.customer.Company;
import bimobile.service.customer.CustomerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class AddCompanyDialog extends Dialog {

    public AddCompanyDialog(CustomerService service, ComboBox<Company> companyCombo) {
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
                Company saved = service.saveCompany(newCompany);

                companyCombo.setItems(service.getAllCompanies());
                companyCombo.setValue(saved);

                close();
            }
        });

        Button cancel = new Button("Abbrechen", e -> close());
        getFooter().add(cancel, save);
        add(form);
    }
}