package bimobile.views.rentals.dialogs;

import bimobile.model.Rental;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;

/**
 * Dialog für die Detailansicht einer Ausleihe.
 * <p>
 * Diese View ist rein informativ (read-only) und dient dazu,
 * alle wichtigen Eckdaten einer Ausleihe übersichtlich darzustellen.
 * <p>
 *
 * @author Ben Berlin
 */
public class RentalInfoDialog extends Dialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Erstellt die Informationsansicht zur Ausleihe.
     *
     * @param rental die anzuzeigende Ausleihe
     */
    public RentalInfoDialog(Rental rental) {
        setWidth("540px");
        setModal(true);
        setDraggable(true);

        buildLayout(rental);
    }
	/**
	 * Baut das UI-Layout des Dialogs.
	 * <p>
	 * Verwendet ein {@link FormLayout}, um Label/Wert-Paare
	 * responsiv und übersichtlich anzuordnen.
	 *
	 * @param rental die anzuzeigende Ausleihe
	 */
    private void buildLayout(Rental rental) {
        H3 dialogTitle = new H3("Übersicht Ausleihe #" + rental.getId());

        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("450px", 2)
        );

        infoLayout.add(
                buildInfoRow("Status", rental.getStatus().name()),
                buildInfoRow("Kunde", rental.getCustomer() != null ? rental.getCustomer().getFullName() : "-"),
                buildInfoRow("Fahrzeug", rental.getVehicle() != null ? rental.getVehicle().getLicensePlate() : "-"),
                buildInfoRow("Standort", rental.getFacility() != null ? rental.getFacility().getAddress() : "–"),
                buildInfoRow("Startdatum", rental.getStartDate() != null ? rental.getStartDate().format(DATE_FORMATTER) : "-"),
                buildInfoRow("Enddatum", rental.getEndDate() != null ? rental.getEndDate().format(DATE_FORMATTER) : "-"),
                buildInfoRow("Gesamtpreis", rental.getTotalPrice() + " €"),
                buildInfoRow("Letzte Aktualisierung",
                        rental.getUpdatedAt() != null ? rental.getUpdatedAt().format(DATE_TIME_FORMATTER) : "-")
        );

        Button close = new Button("Schließen", e -> close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(dialogTitle, infoLayout, close);
        layout.setAlignItems(Alignment.STRETCH);
        layout.setSpacing(true);

        add(layout);
    }

    /**
     * Baut eine kompakte Zeile für Label und Wert.
     *
     * @param label Feldname
     * @param value Feldwert
     * @return Layout-Block für das Formular
     */
    private VerticalLayout buildInfoRow(String label, String value) {
        Span headline = new Span(label);
        headline.getStyle().set("font-weight", "600");
        Span content = new Span(value != null ? value : "-");
        content.getStyle().set("color", "var(--lumo-secondary-text-color)");
        VerticalLayout row = new VerticalLayout(headline, content);
        row.setPadding(false);
        row.setSpacing(false);
        return row;
    }
}
