package bimobile.views;

import bimobile.controller.FacilityController;
import bimobile.model.Facility;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "standorte", layout = MainLayout.class)
@PageTitle("Standortübersicht")
@PermitAll
/**
 *  Übesicht aller Standorte im BI-Mobile System.

 *  Diese View stellt ein Grid zur Anzeige aller gespeicherten Standorte bereit und ermöglicht deren Verwaltung.
 *  Dazu gehören:
 *  - Anlegen neuer Standorte
 *  - Bearbeiten bestehender Standorte
 *  - Deaktivieren eines Standortes

 *  Die View kommuniziert direkt mit dem {@link FacilityController}, lädt dessen Daten in ein Grid
 *  und öffnet je nach Benutzeraktion passende Dialoge (Anlegen, Bearbeiten, Deaktivieren).

 * @Author Ben Berlin, Jannick Braun
 */
public class LocationsOverviewView extends VerticalLayout {

	private final FacilityController controller;
	private final Grid<Facility> grid = new Grid<>(Facility.class, false);

	@Autowired
	/**
	 * Erstellt dei Standortübersicht und initialisiert das Layout, das Grid sowie alle Aktionen über
	 * den übergebenen {@link FacilityController} werden die Standortdaten aus der Datenbank abgerufen
	 * und Änderungen verarbeitet.
	 *
	 * @param controller Controller für Standort-Operationen (CRUD)
	 *
	 * @Author Ben Berlin
	 */
	public LocationsOverviewView(FacilityController controller) {
		this.controller = controller;

		//Layout-Grundstruktur
		setPadding(true);
		setSizeFull();
		getStyle().set("background", "#f9fafb");
		getStyle().set("min-height", "100vh");


		H2 title = new H2("Standortübersicht");

		// Button, der den Anlegen-Dialog öffnet (statt neue View)
		Button neu = new Button("Neuen Standort anlegen", new Icon(VaadinIcon.PLUS));
		neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		neu.addClickListener(e -> openCreateDialog());

		HorizontalLayout header = new HorizontalLayout(title, neu);
		header.setWidthFull();
		header.setAlignItems(Alignment.CENTER);
		header.setJustifyContentMode(JustifyContentMode.BETWEEN);

		//Standorte Grid
		grid.addColumn(Facility::getId).setHeader("ID").setAutoWidth(true);
		grid.addColumn(Facility::getAddress).setHeader("Adresse").setAutoWidth(true);
		grid.addColumn(Facility::getMail).setHeader("E-Mail").setAutoWidth(true);
		grid.addColumn(Facility::getTelephoneNr).setHeader("Telefon").setAutoWidth(true);

		//Bearbeiten / Löschen
		grid.addComponentColumn(facility -> {
			Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
			bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			bearbeiten.addClickListener(e -> openEditDialog(facility));

			Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
			loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
			loeschen.addClickListener(e -> openDeleteDialog(facility));

			return new HorizontalLayout(bearbeiten, loeschen);
		}).setHeader("Aktionen");

		updateGrid();

		add(header, grid);
		setFlexGrow(1, grid);
	}

	/**
	 * Aktualisiert das Grid, indem alle Standorte aus dem {@link FacilityController} geladen
	 * und anschließend als Grid-Items gesetzt werden.

	 * Nach jedem anlegen, bearbeiten oder deaktivieren eines Standorts wird die Methode aufgerufen,
	 * um das UI konsistent zu halten.
	 *
	 * @Author Ben Berlin
	 */
	private void updateGrid() {
		List<Facility> facilities = controller.getAllFacilities();
		grid.setItems(facilities);
	}

	/**
	 * Öffnet ein Dialogfeld zum Anlegen eines neuen Standorts.
	 * Der Dialog enthält Eingabefelder für die Adresse, E-Mail und Telefonnummer.

	 * Es wird nach der eingabe geprüft ob:
	 *  1. Alle Felder ausgefüllt sind,
	 *  2. Das die Telefonnummer eine gültige Zahl ist.
	 *  Ungültige Eingaben führen zu visuellen Fehlerhinweisen und blockieren den Speichervorgang.

	 *  Beim Speichern:
	 *  - werden die Eingaben geprüft,
	 *  - wird der neue Standort über {@link FacilityController#standortAnlegen} angelegt,
	 *  - wird das Grid aktualisiert,
	 *  - der Dialog wird geschlossen.
	 *
	 * @Author Ben Berlin
	 */
	private void openCreateDialog() {
		Dialog dialog = new Dialog();
		dialog.setWidth("500px");
		dialog.setModal(true);
		dialog.setDraggable(true);

		H3 dialogTitle = new H3("Neuen Standort anlegen");

		TextField address = new TextField("Adresse");
		EmailField email = new EmailField("E-Mail");
		email.setRequiredIndicatorVisible(true);
		email.setErrorMessage("Bitte eine gültige E-Mail eingeben!");
		email.setClearButtonVisible(true);
		TextField phone = new TextField("Telefonnummer");

		//Speichern-Button
		Button save = new Button("Speichern", e -> {
				if (address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
					Notification.show("Bitte alle Felder ausfüllen!");
					return;
				}
				if (email.isInvalid()) {
					Notification.show("Ungültige E-Mail-Adresse!").addThemeVariants(NotificationVariant.LUMO_ERROR);
					return;
				}

				try{
					String tel = phone.getValue().trim();
					String msg = controller.standortAnlegen(address.getValue(), email.getValue(), tel);

					Notification.show(msg);

					if (msg.startsWith("Erfolg")) {
						updateGrid();
						dialog.close();
					}
			} catch (NumberFormatException ex) {
				Notification.show("Telefonnummer muss eine gültige Zahl sein!");
			}
		});
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button cancel = new Button("Abbrechen", e -> dialog.close());
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		FormLayout form = new FormLayout(address, email, phone);
		HorizontalLayout actions = new HorizontalLayout(save, cancel);
		actions.setJustifyContentMode(JustifyContentMode.END);

		VerticalLayout layout = new VerticalLayout(dialogTitle, form, actions);
		dialog.add(layout);
		dialog.open();
	}

	/**
	 * Öffnet einen Dialog zum Bearbeiten eines bestehenden Standortes.

	 * Die aktuellen Werte des Standortes werden vorausgefüllt.
	 * Änderungen werden validiert.

	 * Beim Speichern:
	 * - werden die neuen Eingaben geprüft,
	 * - anschließend über {@link FacilityController#standortBearbeiten} aktualisiert und in der Datenbank gespeichert,
	 * - eine Erfolgsmeldung angezeigt,
	 * - und das Grid neu geladen.

	 * @param facility Der zu bearbeitende Standort
	 *
	 * @Author Ben Berlin, Jannick Braun
	 */
	private void openEditDialog(Facility facility) {
		Dialog dialog = new Dialog();
		dialog.setWidth("500px");

		H3 dialogTitle = new H3("Standort bearbeiten");

		TextField addressField = new TextField("Adresse", facility.getAddress(), "");
		EmailField emailField = new EmailField("E-Mail", facility.getMail());
		emailField.setRequiredIndicatorVisible(true);
		emailField.setClearButtonVisible(true);
		emailField.setErrorMessage("Bitte eine gültige E-Mail eingeben!");
		TextField phoneField = new TextField("Telefonnummer", String.valueOf(facility.getTelephoneNr()), "");

		Button saveButton = new Button("Speichern", e -> {
			if(emailField.isInvalid()){
				Notification.show("Ungültige E-Mail-Adresse!");
				return;
			}
			try {
				String tel = phoneField.getValue();
				String result = controller.standortBearbeiten(
						facility.getId(),
						addressField.getValue(),
						emailField.getValue(),
						tel
				);

				if (result.startsWith("Erfolg")) {
					Notification.show(result).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					updateGrid();
					dialog.close();
				} else {
					Notification.show(result).addThemeVariants(NotificationVariant.LUMO_ERROR);
				}
			} catch (NumberFormatException ex) {
				Notification.show("Telefonnummer muss eine Zahl sein!");
			}
		});
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button cancelButton = new Button("Abbrechen", e -> dialog.close());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		FormLayout form = new FormLayout(addressField, emailField, phoneField);
		HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
		actions.setJustifyContentMode(JustifyContentMode.END);

		VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, form, actions);
		dialog.add(dialogLayout);
		dialog.open();
	}

	/**
	 * Öffnet einen Bestätigungsdialog zum Deaktivieren eines Standortes.

	 * Der Benutzer muss die Löschaktion bestätigen.
	 * Nach der Bestätigung wird der Standort über {@link FacilityController#standortDeaktivieren} deaktiviert.
	 * Anschließend aktualisiert das Grid und zeigt eine passende Meldung.
	 *
	 * @param facility Der zu deaktivierende Standort
	 *
	 * @Author Jannick Braun
	 */
	private void openDeleteDialog(Facility facility) {
		Dialog dialog = new Dialog();
		dialog.setWidth("400px");

		H3 dialogTitle = new H3("Standort löschen?");
		VerticalLayout content = new VerticalLayout();
		content.add("Möchten Sie den Standort wirklich löschen?");
		content.add("Adresse: " + facility.getAddress());

		Button confirmButton = new Button("Löschen", e -> {
			String result = controller.standortDeaktivieren(facility.getId());

			Notification notification = Notification.show(result);
			if (result.startsWith("Erfolg")) {
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} else {
				notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
			}

			updateGrid();
			dialog.close();
		});
		confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

		Button cancelButton = new Button("Abbrechen", e -> dialog.close());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout actions = new HorizontalLayout(confirmButton, cancelButton);
		actions.setJustifyContentMode(JustifyContentMode.END);

		VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, content, actions);
		dialog.add(dialogLayout);
		dialog.open();
	}
}
