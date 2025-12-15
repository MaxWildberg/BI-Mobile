package bimobile.views.customer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;
import java.util.function.Function;

public class DeleteDialog<T> extends Dialog {

    public DeleteDialog(
            T object,
            Function<T, String> displayNameFunction,
            Consumer<T> deleteFunction,
            Runnable afterDelete // optional, z. B. Grid aktualisieren
    ) {
        setWidth("auto");
        setHeaderTitle("Eintrag löschen?");

        VerticalLayout content = new VerticalLayout();
        Span span = new Span("Möchten Sie den Eintrag wirklich löschen?");
        span.getStyle().setFontWeight("bold");
        content.add(span);

        // Anzeige des Objekts
        content.add(new Span(displayNameFunction.apply(object)));

        // Buttons
        Button confirmButton = new Button("Löschen", e -> {
            try {
                deleteFunction.accept(object); // Löschen
                Notification.show("Eintrag erfolgreich gelöscht.");
                if (afterDelete != null) {
                    afterDelete.run();
                }
                close();
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
