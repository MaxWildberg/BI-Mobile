package bimobile.views.rentals.dialogs;

import bimobile.model.Rental;
import bimobile.service.RentalChangeLogService;
import bimobile.service.RentalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

/**
 * Dialog zum Löschen einer Ausleihe.
 * <p>
 * Ich lege den Fokus auf eine klare Bestätigung, damit keine Ausleihe aus Versehen
 * verschwindet. Das Logging wird bewusst vor dem Löschen ausgelöst.
 *
 * @author Ben Berlin
 */
public class RentalDeleteDialog extends Dialog {

    private final RentalService rentalService;
    private final RentalChangeLogService changeLogService;
    private final Consumer<Rental> onDeleteConfirmed;
    private final Runnable onDeleteSuccess;

    /**
     * Erstellt den Dialog zum Löschen einer Ausleihe.
     *
     * @param rental           Ausleihe, die gelöscht werden soll
     * @param rentalService    Service zum Löschen
     * @param changeLogService Service für Änderungsprotokoll
     * @param onDeleteConfirmed Callback, der vor dem Löschen ausgeführt wird
     * @param onDeleteSuccess  Callback nach erfolgreichem Löschen
     */
    public RentalDeleteDialog(Rental rental,
                              RentalService rentalService,
                              RentalChangeLogService changeLogService,
                              Consumer<Rental> onDeleteConfirmed,
                              Runnable onDeleteSuccess) {
        this.rentalService = rentalService;
        this.changeLogService = changeLogService;
        this.onDeleteConfirmed = onDeleteConfirmed;
        this.onDeleteSuccess = onDeleteSuccess;

        setWidth("400px");
        buildLayout(rental);
    }

    private void buildLayout(Rental rental) {
        H3 dialogTitle = new H3("Ausleihe löschen?");
        VerticalLayout content = new VerticalLayout();
        content.add(new Paragraph("Möchten Sie die Ausleihe wirklich löschen?"));
        content.add(new Paragraph("Kunde: " + rental.getCustomer().getFullName()));

        Button confirmButton = new Button("Löschen", e -> {
            try {
                onDeleteConfirmed.accept(rental);
                changeLogService.detachRental(rental);
                rentalService.deleteRental(rental);
                Notification.show("Ausleihe erfolgreich gelöscht.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                onDeleteSuccess.run();
                close();
            } catch (Exception ex) {
                Notification.show("Fehler beim Löschen.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Abbrechen", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(confirmButton, cancelButton);
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, content, actions);
        add(dialogLayout);
    }
}
