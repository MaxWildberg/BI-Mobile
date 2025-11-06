package bimobile.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;

@PermitAll

public class MainLayout extends AppLayout {

	public MainLayout() {
		// Topbar (einfach)
		H3 brand = new H3("BI-Mobile · Verwaltung");
		brand.getStyle().set("margin", "0");
		HorizontalLayout top = new HorizontalLayout(brand);
		top.setWidthFull();
		top.setPadding(true);
		addToNavbar(top);

		// Linke Sidebar (Drawer)
		VerticalLayout nav = new VerticalLayout();
		nav.getStyle().set("background", "white");
		nav.getStyle().set("box-shadow", "2px 0 6px rgba(0,0,0,0.05)");
		nav.getStyle().set("padding", "1rem 0");
		nav.getStyle().set("border-radius", "0 10px 10px 0");

		nav.setPadding(false);
		nav.setSpacing(false);
		nav.setWidth("240px");
		nav.getStyle().set("background", "#f9fafb");
		nav.getStyle().set("border-right", "1px solid #e5e7eb");

		RouterLink dashboard = new RouterLink("Dashboard", DashboardView.class);
		RouterLink uebersicht = new RouterLink("Standorte", StandortUebersichtView.class);
		RouterLink anlegen = new RouterLink("Neuen Standort anlegen", StandortAnlegenView.class);

		for (var link : new RouterLink[]{dashboard, uebersicht, anlegen}) {
			link.getElement().getStyle().set("padding", "10px 16px");
			link.getElement().getStyle().set("border-radius", "8px");
			link.getElement().getStyle().set("margin", "4px 8px");
		}

		nav.add(new H3("Navigation"), dashboard, uebersicht, anlegen);
		addToDrawer(nav);
	}
}
