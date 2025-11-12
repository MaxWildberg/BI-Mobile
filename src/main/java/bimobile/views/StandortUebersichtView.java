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
public class StandortUebersichtView extends VerticalLayout {

	private final FacilityController controller;
	private final Grid<Facility> grid = new Grid<>(Facility.class, false);

	@Autowired
	public StandortUebersichtView(FacilityController controller) {
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

	// Holt alle Standorte aus dem Controller und aktualisiert das Grid
	private void updateGrid() {
		List<Facility> facilities = controller.getAllFacilities();
		grid.setItems(facilities);
	}

	//Standort anlegen als Dialog
	private void openCreateDialog() {
		Dialog dialog = new Dialog();
		dialog.setWidth("500px");
		dialog.setModal(true);
		dialog.setDraggable(true);

		H3 dialogTitle = new H3("Neuen Standort anlegen");

		TextField address = new TextField("Adresse");
		EmailField email = new EmailField("E-Mail");
		TextField phone = new TextField("Telefonnummer");

		//Speichern-Button
		Button save = new Button("Speichern", e -> {
			try {
				if (address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
					Notification.show("Bitte alle Felder ausfüllen!");
					return;
				}

				int tel = Integer.parseInt(phone.getValue().trim());
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

	//Bearbeiten eines Standorts
	private void openEditDialog(Facility facility) {
		Dialog dialog = new Dialog();
		dialog.setWidth("500px");

		H3 dialogTitle = new H3("Standort bearbeiten");

		TextField addressField = new TextField("Adresse", facility.getAddress(), "");
		EmailField emailField = new EmailField("E-Mail"); //später einfügen
		TextField phoneField = new TextField("Telefonnummer", String.valueOf(facility.getTelephoneNr()), "");

		Button saveButton = new Button("Speichern", e -> {
			try {
				int tel = Integer.parseInt(phoneField.getValue());
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

	//Löschen eines Standorts
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
