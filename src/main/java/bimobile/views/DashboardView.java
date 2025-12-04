package bimobile.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
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

	 * Das Dashboard zeigt:
	 * - den Titel der Anwendung,
	 * - einen Button zu Navigation in die Standortübersicht.
	 */
	public DashboardView() {
		setSpacing(true);
		setPadding(true);
		setAlignItems(Alignment.CENTER);

		H2 title = new H2("BI-Mobile Dashbord");

		//Buttons zur Navigation
		Button standorteBTN = new Button("Standortübersicht öffnen",
				e -> getUI().ifPresent(ui-> ui.navigate("standorte")));
		standorteBTN.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


		add(title, standorteBTN);
	}
}
