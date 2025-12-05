package bimobile.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
	 * Erzeugt das Dashboard und initialisiert die grundlegenden UI-Elemente.
	 * <p>
	 * Das Dashboard zeigt:
	 * - den Titel der Anwendung,
	 * - einen Button zu Navigation in die Standortübersicht.
	 */
	public DashboardView() {
		setSpacing(true);
		setPadding(true);
		setAlignItems(Alignment.STRETCH);

		H2 title = new H2("BI-Mobile Dashbord");
		Paragraph intro = new Paragraph("Schnellzugriff auf die wichtigsten Verwaltungsbereiche.");

		FlexLayout cards = new FlexLayout(
				createCard("Ausleihe",
						"Aktuelle Vermietungen einsehen, anlegen und verwalten.",
						"ausleihen"),
				createCard("Fahrzeuge",
						"Fahrzeugbestand prüfen, Details pflegen und Verfügbarkeiten im Blick behalten.",
						"vehicles"),
				createCard("Standorte",
						"Alle Filialen im Blick behalten und Adressen oder Kapazitäten anpassen.",
						"standorte"),
				createCard("Mitarbeiter",
						"Nutzer- und Rollenverwaltung für das gesamte Team.",
						"employees"));

		cards.setWidthFull();
		cards.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		cards.getStyle().set("gap", "16px");

		add(title, intro, cards);
	}

	private VerticalLayout createCard(String header, String description, String navigationTarget) {
		VerticalLayout card = new VerticalLayout();
		card.setPadding(true);
		card.setSpacing(false);
		card.setWidth("280px");
		card.getStyle().set("background", "#ffffff");
		card.getStyle().set("border", "1px solid #e5e7eb");
		card.getStyle().set("border-radius", "12px");
		card.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

		H3 title = new H3(header);
		Paragraph paragraph = new Paragraph(description);
		paragraph.getStyle().set("color", "#4b5563");
		paragraph.getStyle().set("margin-top", "0");

		Button open = new Button("Zur Übersicht",
				e -> getUI().ifPresent(ui -> ui.navigate(navigationTarget)));
		open.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		card.add(title, paragraph, open);
		return card;
	}
}
