package bimobile.views.rentals.dialogs;

import bimobile.model.Rental;
import bimobile.service.RentalService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

/**
 * Dialog für die Rückgabe einer Ausleihe.
 * <p>
 * Ich nutze den Dialog, um den Kilometerstand sauber abzufragen und die
 * Rückgabe direkt im System abzuschließen. Das spart Nacharbeit im Alltag.
 *
 * @author Ben Berlin
 */
public class RentalReturnDialog extends Dialog {

    private final RentalService rentalService;
    private final Runnable onReturnSuccess;

    /**
     * Erstellt den Rückgabedialog.
     *
     * @param rental          die zurückzugebende Ausleihe
     * @param rentalService   Service für Rückgabe-Logik
     * @param onReturnSuccess Callback nach erfolgreicher Rückgabe
     */
    public RentalReturnDialog(Rental rental,
                              RentalService rentalService,
                              Runnable onReturnSuccess) {
        this.rentalService = rentalService;
        this.onReturnSuccess = onReturnSuccess;

        setWidth("480px");
        setModal(true);
        setDraggable(true);

        buildLayout(rental);
    }

    private void buildLayout(Rental rental) {
        H3 dialogTitle = new H3("Ausleihe zurückgeben (#" + rental.getId() + ")");

        int currentMileage = rental.getVehicle().getMileage();

        IntegerField endMileageField = new IntegerField("Kilometerstand bei Rückgabe");
        endMileageField.setRequiredIndicatorVisible(true);
        endMileageField.setHelperText("Aktueller Stand: " + currentMileage + " km");
        endMileageField.setMin(currentMileage);
        endMileageField.setStepButtonsVisible(true);
        endMileageField.setAutoselect(true);

        Button confirm = new Button("Zurückgeben", event -> {
            try {
                if (endMileageField.isEmpty()) {
                    Notification.show("Bitte einen Kilometerstand angeben.")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                int endMileage = endMileageField.getValue();
                rentalService.returnRental(rental, endMileage);
                Notification.show("Ausleihe #" + rental.getId() + " zurückgegeben.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                onReturnSuccess.run();
                close();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button cancel = new Button("Abbrechen", e -> close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(confirm, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, endMileageField, actions);
        add(dialogLayout);
    }
}
