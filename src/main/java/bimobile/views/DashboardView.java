package bimobile.views;

import bimobile.security.AuthorizationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
@PermitAll
/**
 * Ein Dashbord Ansicht von BI-Mobile.
 * Diese View dient als Übersicht mit einer ersten Navigation und zeigt eine einfache Übersicht.
 *
 * Von hier aus können Benutzer direkt zur Standortsicht wechseln und könnte weitere Verwaltungsfunktionen aufrufen.
 *
 * @Author Ben Berlin
 */
public class DashboardView extends VerticalLayout {
    /**
     * Das Dashboard zeigt Navigation zu allen Kernbereichen (Standorte, Fahrzeuge, Ausleihen).
     */
    public DashboardView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.START);

        H2 title = new H2("BI-Mobile Dashbord");
        Paragraph subtitle = new Paragraph("Schnelle Sprungpunkte in die wichtigsten Verwaltungsbereiche.");

        FlexLayout overview = new FlexLayout();
        overview.setWidthFull();
        overview.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        overview.getStyle().set("gap", "16px");

        // Berechtigungen prüfen
        boolean canViewLocations = AuthorizationUtils.canAccessLocations();
        boolean canViewEmployees = AuthorizationUtils.canAccessEmployees();

        overview.add(
                createCard("Standorte", "Verwalten Sie bestehende Standorte und legen Sie neue Niederlassungen an.", "Zur Standortverwaltung", "standorte", canViewLocations),
                createCard("Fahrzeuge", "Fahrzeuge anlegen, pflegen und für Ausleihen bereitstellen.", "Zur Fahrzeugverwaltung", "vehicles", true),
                createCard("Ausleihen", "Alle laufenden und abgeschlossenen Ausleihen im Blick behalten.", "Zur Ausleihübersicht", "ausleihen", true),
                createCard("Mitarbeiter", "Mitarbeiterdaten und Rollen einfach pflegen.", "Zur Mitarbeiterverwaltung", "employees", canViewEmployees),
                createCard("Kunden", "Kundendaten verwalten und neue Kunden aufnehmen.", "Zur Kundenverwaltung", "kunden", true)
        );

        add(title, subtitle, overview);
    }

    private VerticalLayout createCard(String headline, String description, String buttonLabel, String navigationTarget, boolean enabled) {
        H2 heading = new H2(headline);
        heading.getStyle().set("margin", "0");

        Paragraph text = new Paragraph(description);
        text.getStyle().set("margin", "0");

        Button navigateButton = new Button(buttonLabel, e -> getUI().ifPresent(ui -> ui.navigate(navigationTarget)));
        navigateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        navigateButton.setEnabled(enabled);

        VerticalLayout card = new VerticalLayout(heading, text, navigateButton);
        card.setPadding(true);
        card.setSpacing(true);
        card.setWidth("320px");
        card.getStyle()
                .set("background", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.08)");

        if (!enabled) {
            card.getStyle().set("opacity", "0.5");
            card.getStyle().set("pointer-events", "none"); // Verhindert Klicks auf die Karte
        }

        return card;
    }
}