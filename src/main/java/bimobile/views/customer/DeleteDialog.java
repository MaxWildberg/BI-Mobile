package bimobile.views.customer;

import bimobile.model.customer.Customer;
import bimobile.service.CustomerService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class DeleteDialog extends Dialog {

    public DeleteDialog(Customer customer, CustomerService service, Runnable onDelete) {

        setWidth("auto");
        setHeaderTitle("Kunde löschen?");
        VerticalLayout content = new VerticalLayout();
        Span span = new Span("Möchten Sie den Kunden wirklich löschen?");
        span.getStyle().setFontWeight("bold");
        content.add(span);
        content.add(new Span(customer.getFullName()));
        content.add(new Span("Kunden-ID: " + customer.getCustomerId() + ", E-Mail: " + customer.getContactInfo().getMail()));

        Button confirmButton = new Button("Löschen", e -> {
            try {
                service.deleteCustomer(customer.getCustomerId());
                Notification.show("Kunde erfolgreich gelöscht.");
                onDelete.run();
                close();
            } catch (IllegalStateException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage());
            }
        });

        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Abbrechen", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout actions = new HorizontalLayout(confirmButton, cancelButton);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        VerticalLayout dialogLayout = new VerticalLayout(content, actions);
        add(dialogLayout);
    }
}
