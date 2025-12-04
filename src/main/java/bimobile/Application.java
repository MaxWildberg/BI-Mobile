package bimobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Haupteinstiegspunkt der BI-Mobile Anwendung.

 * Diese Klasse startet die Spring-Boot-Applikation und initialisiert damit alle Komponenten des Systems, einschließlich:
 * - Spring Dependency Injection,
 * - Konfigurationen,
 * - Vaadin-Frontend,
 * - Controller, Services und Repositories.

 * Die Anwendung wird durch {@link SpringApplication#run(Class, String[])} gestartet und bildet damit den Startpunkt
 * für die gesamte BI-Mobile Verwaltungsoberfläche.

 * @author Ben
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
/**
 * Startpunkt der Anwendung.
 * Diese Methode startet den eingebetteten Spring-Boot-Server und lädt alle Projektkomponenten, sodass die Vaadin Weboberfläche erreichbar wird.
 *
 * @param args optionaler Startparameter
 */
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
