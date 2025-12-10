package bimobile.views;

import bimobile.controller.FacilityController;
import bimobile.security.AuthorizationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Author: Lasse
 * Description: View for creating a new facility.
 */
@Route(value = "standort-anlegen", layout = MainLayout.class)
@PageTitle("Neuen Standort anlegen")
public class FacilityOverviewView extends VerticalLayout {

    private final FacilityController controller;

    public FacilityOverviewView(FacilityController controller) {
        this.controller = controller;

        // Only MANAGEMENT may access
        if (!AuthorizationUtils.isManagement()) {
            Notification.show("Keine Berechtigung.");
            getUI().ifPresent(ui -> ui.navigate("standorte"));
            return;
        }

        buildUI();
    }

    private void buildUI() {
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Neuen Standort anlegen");

        TextField address = new TextField("Adresse");
        EmailField email = new EmailField("E-Mail");
        TextField phone = new TextField("Telefonnummer");

        Button save = new Button("Speichern", e -> {
            if (address.isEmpty()) {
                Notification.show("Bitte Adresse eingeben");
                return;
            }
            if (email.isEmpty()) {
                Notification.show("Bitte E-Mail eingeben");
                return;
            }
            if (phone.isEmpty()) {
                Notification.show("Bitte Telefonnummer eingeben");
                return;
            }

            try {
                int tel = Integer.parseInt(phone.getValue().trim());
                String msg = controller.standortAnlegen(address.getValue(), email.getValue(), tel);

                Notification.show(msg);

                if (msg.startsWith("Erfolg")) {
                    clear(address, email, phone);
                }
            } catch (NumberFormatException ex) {
                Notification.show("Telefonnummer muss eine gültige Zahl sein");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Abbrechen",
                e -> getUI().ifPresent(ui -> ui.navigate("standorte")));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(address, email, phone);
        HorizontalLayout actions = new HorizontalLayout(save, cancel);

        add(title, form, actions);
    }

    private void clear(TextField a, EmailField e, TextField p) {
        a.clear();
        e.clear();
        p.clear();
    }
}
