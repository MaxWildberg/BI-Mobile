package bimobile.views;

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
		add(new Paragraph("Dashboard (Demo) – hier muss für den Prototyp nichts weiter sein."));
	}
}
