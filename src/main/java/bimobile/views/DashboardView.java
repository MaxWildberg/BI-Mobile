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
public class DashboardView extends VerticalLayout {
	public DashboardView() {
		setSpacing(true);
		setPadding(true);
		setAlignItems(Alignment.CENTER);

		H2 title = new H2("BI-Mobile Dashbord");

		//Buttons zur Navigation
		Button standorteBTN = new Button("Standortübersicht öffnen",
				e -> getUI().ifPresent(ui-> ui.navigate("standorte")));
		standorteBTN.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button anlegenBTN = new Button("Neuen Standort anlegen",
				e -> getUI().ifPresent(ui -> ui.navigate("standort-anlegen")));
		anlegenBTN.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

		add(title, standorteBTN, anlegenBTN);
	}
}
