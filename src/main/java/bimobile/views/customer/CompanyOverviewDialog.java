package bimobile.views.customer;

import bimobile.model.customer.Company;
import bimobile.service.customer.CompanyService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.notification.Notification;

public class CompanyOverviewDialog extends Dialog {

    private final CompanyService companyService;
    private final Grid<Company> companyGrid = new Grid<>(Company.class, false);

    public CompanyOverviewDialog(CompanyService service) {
        this.companyService = service;

        setWidth("800px");
        setHeight("600px");

        configureGrid();
        add(createToolbar(), companyGrid);
        updateGrid();
    }

    private void configureGrid() {
        companyGrid.addColumn(Company::getCompanyId).setHeader("ID").setWidth("50px").setFlexGrow(0);
        companyGrid.addColumn(Company::getName).setHeader("Name").setSortable(true);
        companyGrid.addColumn(Company::getAddress).setHeader("Adresse");
        //companyGrid.addColumn(Company::getPhoneNumber).setHeader("Telefon");

        companyGrid.asSingleSelect().addValueChangeListener(event -> {
            Company selected = event.getValue();
            // Optional: Details anzeigen oder bearbeiten
        });
    }

    private HorizontalLayout createToolbar() {
        Button addButton = new Button("Neu", e -> openCompanyForm(new Company()));
        Button editButton = new Button("Bearbeiten", e -> {
            Company selected = companyGrid.asSingleSelect().getValue();
            if (selected != null) {
                openCompanyForm(selected);
            } else {
                Notification.show("Bitte eine Firma auswählen.");
            }
        });
        Button deleteButton = new Button("Löschen", e -> {
            Company selected = companyGrid.asSingleSelect().getValue();
            if (selected != null) {
                DeleteDialog<Company> deleteDialog = new DeleteDialog<>(
                        selected,
                        Company::getName,
                        c -> companyService.deleteCompany(c.getCompanyId()),
                        this::updateGrid
                );
                deleteDialog.open();
            } else {
                Notification.show("Bitte eine Firma auswählen.");
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(addButton, editButton, deleteButton);
        toolbar.setSpacing(true);
        return toolbar;
    }

    private void openCompanyForm(Company company) {
        AddCompanyDialog addCompanyDialog = new AddCompanyDialog(companyService);
        addCompanyDialog.addDetachListener(e -> {
            Company saved = addCompanyDialog.getSavedCompany();
            if (saved != null) {
                updateGrid();
            }
        });
        addCompanyDialog.open();
    }

    private void updateGrid() {
        companyGrid.setItems(companyService.getAllCompanies());
    }
}

