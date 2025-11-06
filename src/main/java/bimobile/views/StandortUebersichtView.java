package bimobile.views;

import bimobile.controller.FacilityController;
import bimobile.model.Facility;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "standorte", layout = MainLayout.class)
@PageTitle("Standortübersicht")
@PermitAll
public class StandortUebersichtView extends VerticalLayout {

    private final FacilityController controller;
    private final Grid<Facility> grid = new Grid<>(Facility.class, false);

    @Autowired
    public StandortUebersichtView(FacilityController controller) {
        this.controller = controller;

        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");
        getStyle().set("min-height", "100vh");

        H2 title = new H2("Standortübersicht");

        Button neu = new Button("Neuen Standort anlegen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neu.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("standort-anlegen")));

        HorizontalLayout header = new HorizontalLayout(title, neu);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);


        grid.addColumn(Facility::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Facility::getAddress).setHeader("Adresse").setAutoWidth(true);
        grid.addColumn(Facility::getMail).setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn(Facility::getTelephoneNr).setHeader("Telefon").setAutoWidth(true);

        grid.addComponentColumn(facility -> {
            Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> openEditDialog(facility));

            Button löschen = new Button(new Icon(VaadinIcon.TRASH));
            löschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            löschen.addClickListener(e -> openDeleteDialog(facility));

            return new HorizontalLayout(bearbeiten, löschen);
        }).setHeader("Aktionen");

        updateGrid();

        add(header, grid);
        setFlexGrow(1, grid);
    }

    private void updateGrid() {
        List<Facility> facilities = controller.getAllFacilities();
        grid.setItems(facilities);
    }


    private void openEditDialog(Facility facility) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        H3 dialogTitle = new H3("Standort bearbeiten");

        TextField addressField = new TextField("Adresse");
        addressField.setValue(facility.getAddress());

        EmailField emailField = new EmailField("E-Mail");
        emailField.setValue(facility.getMail());

        TextField phoneField = new TextField("Telefonnummer");
        phoneField.setValue(String.valueOf(facility.getTelephoneNr()));

        Button saveButton = new Button("Speichern", e -> {
            try {
                int tel = Integer.parseInt(phoneField.getValue());
                String result = controller.standortBearbeiten(
                        facility.getId(),
                        addressField.getValue(),
                        emailField.getValue(),
                        tel
                );

                if (result.startsWith("Erfolg")) {
                    Notification notification = Notification.show(result);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    updateGrid();
                    dialog.close();
                } else {
                    Notification notification = Notification.show(result);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (NumberFormatException ex) {
                Notification.show("Telefonnummer muss eine Zahl sein");
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