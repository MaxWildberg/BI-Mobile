package bimobile.views;

import bimobile.controller.FacilityController;
import bimobile.model.Facility;
import bimobile.security.AuthorizationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
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
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "standorte", layout = MainLayout.class)
@PageTitle("Standortübersicht")
@PermitAll
public class LocationsOverviewView extends VerticalLayout {

    private final FacilityController controller;
    private final Grid<Facility> grid = new Grid<>(Facility.class, false);

    // MANAGEMENT darf alles, BRANCH_MANAGER darf schreibgeschützt nur eigenen Standort sehen
    private final boolean isManagement = AuthorizationUtils.isManagement();
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();

    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    @Autowired
    public LocationsOverviewView(FacilityController controller) {
        this.controller = controller;

        setPadding(true);
        setSizeFull();
        getStyle().set("background", "#f9fafb");

        H2 title = new H2("Standortübersicht");

        // EMPLOYEE sollte hier niemals landen (Navigation filtert das bereits)
        if (!isManagement && !isBranchManager) {
            add(title, new Span("Keine Berechtigung."));
            return;
        }

        if (isBranchManager) {
            Span info = new Span("Sie sehen nur Ihren eigenen Standort. Schreibgeschützt.");
            info.getStyle().set("color", "var(--lumo-secondary-text-color)");
            add(title, info);
            configureGrid(false);
            updateGrid();
            add(grid);
            return;
        }

        // MANAGEMENT → volle Rechte
        Button neu = new Button("Neuen Standort anlegen", new Icon(VaadinIcon.PLUS));
        neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neu.addClickListener(e -> openCreateDialog());

        HorizontalLayout header = new HorizontalLayout(title, neu);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        configureGrid(true);
        updateGrid();

        add(header, grid);
        setFlexGrow(1, grid);
    }

    private void configureGrid(boolean editable) {
        grid.addColumn(Facility::getId).setHeader("ID");
        grid.addColumn(Facility::getAddress).setHeader("Adresse");
        grid.addColumn(Facility::getMail).setHeader("E-Mail");
        grid.addColumn(Facility::getTelephoneNr).setHeader("Telefon");

        if (!editable) return;

        grid.addComponentColumn(facility -> {
            Button edit = new Button(new Icon(VaadinIcon.EDIT));
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            edit.addClickListener(e -> openEditDialog(facility));

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delete.addClickListener(e -> openDeleteDialog(facility));

            return new HorizontalLayout(edit, delete);
        }).setHeader("Aktionen");
    }

    private void updateGrid() {
        List<Facility> facilities = controller.getAllFacilities();

        // Branch Manager → nur eigener Standort, schreibgeschützt
        if (isBranchManager && currentFacility != null) {
            facilities = facilities.stream()
                    .filter(f -> f.getId().equals(currentFacility.getId()))
                    .collect(Collectors.toList());
        }

        grid.setItems(facilities);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        H3 title = new H3("Neuen Standort anlegen");

        TextField address = new TextField("Adresse");
        EmailField email = new EmailField("E-Mail");
        TextField phone = new TextField("Telefonnummer");

        Button save = new Button("Speichern", e -> {
            if (address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Notification.show("Bitte alle Felder ausfüllen!");
                return;
            }

            try {
                String result = controller.standortAnlegen(
                        address.getValue(),
                        email.getValue(),
                        Integer.parseInt(phone.getValue())
                );

                Notification.show(result);
                if (result.startsWith("Erfolg")) {
                    updateGrid();
                    dialog.close();
                }

            } catch (NumberFormatException ex) {
                Notification.show("Telefonnummer muss eine Zahl sein.");
            }
        });

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        FormLayout form = new FormLayout(address, email, phone);
        dialog.add(new VerticalLayout(title, form, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private void openEditDialog(Facility facility) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        H3 title = new H3("Standort bearbeiten");

        TextField address = new TextField("Adresse", facility.getAddress());
        EmailField email = new EmailField("E-Mail", facility.getMail());
        TextField phone = new TextField("Telefonnummer", String.valueOf(facility.getTelephoneNr()));

        Button save = new Button("Speichern", e -> {
            try {
                String result = controller.standortBearbeiten(
                        facility.getId(),
                        address.getValue(),
                        email.getValue(),
                        Integer.parseInt(phone.getValue())
                );

                Notification.show(result);
                if (result.startsWith("Erfolg")) {
                    updateGrid();
                    dialog.close();
                }

            } catch (NumberFormatException ex) {
                Notification.show("Telefonnummer muss eine Zahl sein.");
            }
        });

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Abbrechen", e -> dialog.close());

        dialog.add(new VerticalLayout(title,
                new FormLayout(address, email, phone),
                new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }

    private void openDeleteDialog(Facility facility) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        H3 title = new H3("Standort löschen?");
        VerticalLayout content = new VerticalLayout(
                new Span("Adresse: " + facility.getAddress())
        );


        Button delete = new Button("Löschen", e -> {
            String result = controller.standortDeaktivieren(facility.getId());
            Notification.show(result);
            updateGrid();
            dialog.close();
        });

        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Abbrechen", e -> dialog.close());

        dialog.add(new VerticalLayout(title, content, new HorizontalLayout(delete, cancel)));
        dialog.open();
    }
}
