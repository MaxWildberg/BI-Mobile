package bimobile.views;

import bimobile.controller.FacilityController;
import bimobile.model.Facility;
import bimobile.security.AuthorizationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed; // Wichtig: Import geändert
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Route(value = "standorte", layout = MainLayout.class)
@PageTitle("Standortübersicht")
// WICHTIG: Nur Director und Manager dürfen rein. Employees werden hier hart geblockt.
@RolesAllowed({"MANAGING_DIRECTOR", "GENERAL_MANAGER"})
/**
 * Übesicht aller Standorte im BI-Mobile System.
 * Diese View stellt ein Grid zur Anzeige aller gespeicherten Standorte bereit und ermöglicht deren Verwaltung.
 *
 * @Author Ben Berlin, Jannick Braun
 */
public class LocationsOverviewView extends VerticalLayout {

    private final FacilityController controller;
    private final Grid<Facility> grid = new Grid<>(Facility.class, false);

    // Berechtigungs-Checks cachen
    private final boolean isManagement = AuthorizationUtils.isManagement(); // Director
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager(); // Manager
    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    @Autowired
    public LocationsOverviewView(FacilityController controller) {
        this.controller = controller;

        //Layout-Grundstruktur
        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        // 1. Anti-Crash-Check: Manager ohne zugewiesene Filiale abfangen
        if (isBranchManager && currentFacility == null) {
            removeAll();

            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.setAlignItems(Alignment.CENTER);
            errorLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            errorLayout.setSizeFull();

            Icon errorIcon = VaadinIcon.WARNING.create();
            errorIcon.setColor("var(--lumo-error-color)");
            errorIcon.setSize("48px");

            H2 errorTitle = new H2("Kein Standort zugewiesen");
            Span errorText = new Span("Ihrem Benutzerkonto ist keine Filiale zugeordnet. Bitte wenden Sie sich an die Geschäftsführung.");
            errorText.getStyle().set("color", "var(--lumo-secondary-text-color)");

            errorLayout.add(errorIcon, errorTitle, errorText);
            add(errorLayout);
            return; // Abbruch, Rest wird nicht geladen
        }

        H2 title = new H2("Standortübersicht");

        HorizontalLayout header = new HorizontalLayout(title);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Button "Neuen Standort anlegen" -> NUR für Director sichtbar
        if (isManagement) {
            Button neu = new Button("Neuen Standort anlegen", new Icon(VaadinIcon.PLUS));
            neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            neu.addClickListener(e -> openCreateDialog());
            header.add(neu);
        }

        //Standorte Grid Konfiguration
        grid.addColumn(Facility::getId).setHeader("ID").setWidth("80px").setFlexGrow(0); // Feste Breite für ID
        grid.addColumn(Facility::getAddress).setHeader("Adresse").setAutoWidth(true);
        grid.addColumn(Facility::getMail).setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn(Facility::getTelephoneNr).setHeader("Telefon").setAutoWidth(true);

        // Aktionen-Spalte (Bearbeiten / Löschen) -> NUR für Director sichtbar
        if (isManagement) {
            grid.addComponentColumn(facility -> {
                Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
                bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                bearbeiten.addClickListener(e -> openEditDialog(facility));

                Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
                loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                loeschen.addClickListener(e -> openDeleteDialog(facility));

                return new HorizontalLayout(bearbeiten, loeschen);
            }).setHeader("Aktionen").setAutoWidth(true);
        }

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        updateGrid();

        add(header, grid);
        setFlexGrow(1, grid);
    }

    /**
     * Aktualisiert das Grid.
     * Director sieht alle Standorte.
     * Manager sieht nur seinen eigenen Standort.
     */
    private void updateGrid() {
        if (isManagement) {
            // Chef sieht alles
            List<Facility> facilities = controller.getAllFacilities();
            grid.setItems(facilities);
        } else if (isBranchManager && currentFacility != null) {
            // Manager sieht nur seine Filiale
            grid.setItems(Collections.singletonList(currentFacility));
        }
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        H3 dialogTitle = new H3("Neuen Standort anlegen");

        TextField address = new TextField("Adresse");
        EmailField email = new EmailField("E-Mail");
        email.setRequiredIndicatorVisible(true);
        email.setErrorMessage("Bitte eine gültige E-Mail eingeben!");
        email.setClearButtonVisible(true);
        TextField phone = new TextField("Telefonnummer");

        Button save = new Button("Speichern", e -> {
            if (address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Notification.show("Bitte alle Felder ausfüllen!");
                return;
            }
            if (email.isInvalid()) {
                Notification.show("Ungültige E-Mail-Adresse!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try{
                String tel = phone.getValue().trim();
                String msg = controller.standortAnlegen(address.getValue(), email.getValue(), Integer.parseInt(tel));

                Notification.show(msg);

                if (msg.startsWith("Erfolg")) {
                    updateGrid();
                    dialog.close();
                }
            } catch (NumberFormatException ex) {
                Notification.show("Telefonnummer muss eine gültige Zahl sein!");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(address, email, phone);
        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
        dialog.add(layout);
        dialog.open();
    }

    private void openEditDialog(Facility facility) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        H3 dialogTitle = new H3("Standort bearbeiten");

        TextField addressField = new TextField("Adresse", facility.getAddress(), "");
        EmailField emailField = new EmailField("E-Mail", facility.getMail());
        emailField.setRequiredIndicatorVisible(true);
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Bitte eine gültige E-Mail eingeben!");
        TextField phoneField = new TextField("Telefonnummer", String.valueOf(facility.getTelephoneNr()), "");

        Button saveButton = new Button("Speichern", e -> {
            if(emailField.isInvalid()){
                Notification.show("Ungültige E-Mail-Adresse!");
                return;
            }
            try {
                String tel = phoneField.getValue();
                String result = controller.standortBearbeiten(
                        facility.getId(),
                        addressField.getValue(),
                        emailField.getValue(),
                        Integer.parseInt(tel)
                );

                if (result.startsWith("Erfolg")) {
                    Notification.show(result).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    updateGrid();
                    dialog.close();
                } else {
                    Notification.show(result).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (NumberFormatException ex) {
                Notification.show("Telefonnummer muss eine Zahl sein!");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(addressField, emailField, phoneField);
        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, form, actions);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openDeleteDialog(Facility facility) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 dialogTitle = new H3("Standort löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add("Möchten Sie den Standort wirklich löschen?");
        content.add("Adresse: " + facility.getAddress());

        Button confirmButton = new Button("Löschen", e -> {
            String result = controller.standortDeaktivieren(facility.getId());

            Notification notification = Notification.show(result);
            if (result.startsWith("Erfolg")) {
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }

            updateGrid();
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(confirmButton, cancelButton);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, content, actions);
        dialog.add(dialogLayout);
        dialog.open();
    }
}