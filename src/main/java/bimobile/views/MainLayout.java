package bimobile.views;

import bimobile.security.AuthorizationUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout() {
        buildTopBar();
        buildSideNavigation();
    }

    private void buildTopBar() {
        H3 brand = new H3("BI-Mobile · Verwaltung");
        brand.getStyle().set("margin", "0");

        HorizontalLayout top = new HorizontalLayout(brand);
        top.setWidthFull();
        top.setPadding(true);

        addToNavbar(top);
    }

    private void buildSideNavigation() {

        VerticalLayout nav = new VerticalLayout();
        nav.setWidth("240px");
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.getStyle().set("background", "#f9fafb");
        nav.getStyle().set("border-right", "1px solid #e5e7eb");
        nav.add(new H3("Navigation"));

        nav.add(styledLink("Dashboard", DashboardView.class));

        // MANAGEMENT + BRANCH_MANAGER sehen Standorte
        if (AuthorizationUtils.isManagement() || AuthorizationUtils.isBranchManager()) {
            nav.add(styledLink("Standorte", LocationsOverviewView.class));
        }

        // Fahrzeuge & Ausleihen → alle Rollen
        nav.add(styledLink("Fahrzeuge", VehicleView.class));
        nav.add(styledLink("Ausleihen", RentalsOverviewView.class));

        // Mitarbeiterverwaltung → MANAGEMENT + BRANCH_MANAGER
        if (AuthorizationUtils.isManagement() || AuthorizationUtils.isBranchManager()) {
            nav.add(styledLink("Mitarbeiter", EmployeeView.class));
        }

        // Kunden immer sichtbar
        nav.add(styledLink("Kunden", CustomerOverview.class));

        addToDrawer(nav);
    }

    private RouterLink styledLink(String text, Class<? extends Component> target) {
        RouterLink link = new RouterLink(text, target);
        link.getElement().getStyle().set("padding", "10px 16px");
        link.getElement().getStyle().set("border-radius", "8px");
        return link;
    }
}
