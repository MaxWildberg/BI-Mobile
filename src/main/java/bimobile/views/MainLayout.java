package bimobile.views;

import bimobile.views.customer.CustomerOverview;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;

/**
 * Hauptlayout der BI-Mobile Verwaltungsoberfläche.

 * Dieses Layout definiert die globale Struktur der Anwendung, bestehend aus:
 * - einer Top-Bar mit Titel
 * - einer linken Navigationsleiste mit allen wichtigen Views.

 * Das Layout sorgt für ein einheitliches Styling und ein konsistentes Benutzererlebnis innerhalb der gesamten Anwendung.

 * Wird automatisch durch Vaadin verwendet, sobald Views das Layout in der @Route-Annotation angeben.
 *
 * @author Ben Berlin
 */
@PermitAll
public class MainLayout extends AppLayout {

    /**
     * Initialisiert das Hauptlayout der Anwendung.
     * Der Konstruktor:
     * - baut Top-Bar und die Linke Navigationsleiste auf.
     * - legt ein einheitliches visuelles Styling fest.
     */
    public MainLayout() {
        // TopBar
        H3 brand = new H3("BI-Mobile · Verwaltung");
        brand.getStyle().set("margin", "0");
        HorizontalLayout top = new HorizontalLayout(brand);
        top.setWidthFull();
        top.setPadding(true);
        addToNavbar(top);

        // Linke Navigationsleiste
        VerticalLayout nav = new VerticalLayout();
        nav.setWidth("240px");
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.getStyle().set("background", "#f9fafb");
        nav.getStyle().set("border-right", "1px solid #e5e7eb");
        nav.getStyle().set("box-shadow", "2px 0 6px rgba(0,0,0,0.05)");

        // Navigationseinträge
        RouterLink dashboard = new RouterLink("Dashboard", DashboardView.class);
        RouterLink facilities = new RouterLink("Standorte", LocationsOverviewView.class);
        RouterLink vehicles = new RouterLink("Fahrzeuge", VehicleView.class);
        RouterLink rentals = new RouterLink("Ausleihen", RentalsOverviewView.class);
        RouterLink employees = new RouterLink("Mitarbeiter", EmployeeView.class);
        RouterLink customers = new RouterLink("Kunden", CustomerOverview.class);

        styleLinks(dashboard, facilities, vehicles, rentals, employees, customers);

        nav.add(new H3("Navigation"), dashboard, facilities, vehicles, rentals, employees, customers);
        addToDrawer(nav);

    }

    // Gemeinsames Styling der Links
    private void styleLinks(RouterLink... links) {
        for (RouterLink link : links) {
            link.getElement().getStyle().set("padding", "10px 16px");
            link.getElement().getStyle().set("border-radius", "8px");
            link.getElement().getStyle().set("margin", "4px 8px");
        }
    }
}