package bimobile.views.customer;

import bimobile.model.customer.Company;
import bimobile.service.customer.CompanyService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.notification.Notification;

/**
 * Dialog zur Anzeige und Verwaltung von {@link Company}-Einträgen.
 * Stellt eine Übersicht aller Firmen in einem Grid dar
 * und bietet Aktionen zum Anlegen, Bearbeiten und Löschen von Firmen.
 * @author Max Wildberg
 */
public class CompanyOverviewDialog extends Dialog {

    /**
     * Service zur Verwaltung und Persistierung von {@link Company}-Objekten.
     */
    private final CompanyService companyService;

    /**
     * Grid zur tabellarischen Darstellung der Firmen.
     */
    private final Grid<Company> companyGrid = new Grid<>(Company.class, false);

    /**
     * Erstellt den Übersichtsdialog für Firmen.
     *
     * @param service der {@link CompanyService}, der für Datenzugriffe verwendet wird
     */
    public CompanyOverviewDialog(CompanyService service) {
        this.companyService = service;

        setWidth("800px");
        setHeight("600px");

        configureGrid();
        add(createToolbar(), companyGrid);
        updateGrid();
    }

    /**
     * Konfiguriert die Spalten und Grundeinstellungen des Firmen-Grids.
     */
    private void configureGrid() {
        companyGrid.addColumn(Company::getCompanyId).setHeader("ID").setWidth("50px").setFlexGrow(0);
        companyGrid.addColumn(Company::getName).setHeader("Name").setSortable(true);
        companyGrid.addColumn(Company::getAddress).setHeader("Adresse");
        //companyGrid.addColumn(Company::getPhoneNumber).setHeader("Telefon");
    }

    /**
     * Erstellt die Toolbar mit Aktionen zum Anlegen, Bearbeiten und Löschen von Firmen.
     *
     * @return eine {@link HorizontalLayout}-Toolbar mit Aktionsbuttons
     */
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

    /**
     * Öffnet den Dialog zum Anlegen oder Bearbeiten einer Firma.
     * Nach dem Schließen des Dialogs wird das Grid aktualisiert, sofern eine Firma gespeichert wurde.
     *
     * @param company die zu bearbeitende oder neu anzulegende Firma
     */
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

    /**
     * Aktualisiert die Daten im Grid durch erneutes Laden aller Firmen aus dem {@link CompanyService}.
     */
    private void updateGrid() {
        companyGrid.setItems(companyService.getAllCompanies());
    }
}
