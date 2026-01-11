package bimobile.views;

import bimobile.controller.FacilityController;
import bimobile.model.Facility;
import bimobile.security.AuthorizationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Route(value = "standorte", layout = MainLayout.class)
@PageTitle("Standortübersicht")
@RolesAllowed({"MANAGING_DIRECTOR", "GENERAL_MANAGER"})
/**
 * Übersicht aller Standorte im BI-Mobile System.
 *
 * @Author Ben Berlin, Jannick Braun
 */
public class LocationsOverviewView extends VerticalLayout {

	private final FacilityController controller;
	private final Grid<Facility> grid = new Grid<>(Facility.class, false);

	private final boolean isManagement = AuthorizationUtils.isManagement();
	private final boolean isBranchManager = AuthorizationUtils.isBranchManager();
	private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

	@Autowired
	public LocationsOverviewView(FacilityController controller) {
		this.controller = controller;

		setPadding(true);
		setSizeFull();
		getStyle().set("background", "#f9fafb");
		getStyle().set("min-height", "100vh");

		// Manager ohne Standort abfangen
		if (isBranchManager && currentFacility == null) {
			removeAll();

			VerticalLayout errorLayout = new VerticalLayout();
			errorLayout.setAlignItems(Alignment.CENTER);
			errorLayout.setJustifyContentMode(JustifyContentMode.CENTER);
			errorLayout.setSizeFull();

			Icon errorIcon = VaadinIcon.WARNING.create();
			errorIcon.setColor("var(--lumo-error-color)");
			errorIcon.setSize("48px");

			H2 errorTitle = new H2("Kein Standort zugewiesen");
			Span errorText = new Span("Ihrem Benutzerkonto ist keine Filiale zugeordnet. Bitte wenden Sie sich an die Geschäftsführung.");
			errorText.getStyle().set("color", "var(--lumo-secondary-text-color)");

			errorLayout.add(errorIcon, errorTitle, errorText);
			add(errorLayout);
			return;
		}

		H2 title = new H2("Standortübersicht");

		HorizontalLayout header = new HorizontalLayout(title);
		header.setWidthFull();
		header.setAlignItems(Alignment.CENTER);
		header.setJustifyContentMode(JustifyContentMode.BETWEEN);

		if (isManagement) {
			Button neu = new Button("Neuen Standort anlegen", new Icon(VaadinIcon.PLUS));
			neu.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			neu.addClickListener(e -> openCreateDialog());
			header.add(neu);
		}

		// Grid
		grid.addColumn(Facility::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
		grid.addColumn(Facility::getAddress).setHeader("Adresse").setAutoWidth(true);
		grid.addColumn(Facility::getMail).setHeader("E-Mail").setAutoWidth(true);
		grid.addColumn(Facility::getTelephoneNr).setHeader("Telefon").setAutoWidth(true);

		if (isManagement) {
			grid.addComponentColumn(facility -> {
				Button bearbeiten = new Button(new Icon(VaadinIcon.EDIT));
				bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				bearbeiten.addClickListener(e -> openEditDialog(facility));

				Button loeschen = new Button(new Icon(VaadinIcon.TRASH));
				loeschen.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
				loeschen.addClickListener(e -> openDeleteDialog(facility));

				return new HorizontalLayout(bearbeiten, loeschen);
			}).setHeader("Aktionen").setAutoWidth(true);
		}

		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

		updateGrid();

		add(header, grid);
		setFlexGrow(1, grid);
	}

	private void updateGrid() {
		if (isManagement) {
			List<Facility> facilities = controller.getAllFacilities();
			grid.setItems(facilities);
		} else if (isBranchManager && currentFacility != null) {
			grid.setItems(Collections.singletonList(currentFacility));
		}
	}

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
		phone.setHelperText("Erlaubt: Ziffern, Leerzeichen, optional führendes + (z.B. +49 521 123456)");

		Button save = new Button("Speichern", e -> {
			if (address.isEmpty() || email.isEmpty() || phone.isEmpty()) {
				Notification.show("Bitte alle Felder ausfüllen!").addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}
			if (email.isInvalid()) {
				Notification.show("Ungültige E-Mail-Adresse!").addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}

			String tel = phone.getValue().trim();
			if (!tel.matches("^\\+?[0-9 ]+$")) {
				Notification.show("Telefonnummer darf nur Ziffern, Leerzeichen und optional ein führendes + enthalten!")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}

			String msg = controller.standortAnlegen(address.getValue(), email.getValue(), tel);

			Notification notification = Notification.show(msg);
			if (msg.startsWith("Erfolg")) {
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				updateGrid();
				dialog.close();
			} else {
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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

	private void openEditDialog(Facility facility) {
		Dialog dialog = new Dialog();
		dialog.setWidth("500px");

		H3 dialogTitle = new H3("Standort bearbeiten");

		TextField addressField = new TextField("Adresse", facility.getAddress(), "");
		EmailField emailField = new EmailField("E-Mail", facility.getMail());
		emailField.setRequiredIndicatorVisible(true);
		emailField.setClearButtonVisible(true);
		emailField.setErrorMessage("Bitte eine gültige E-Mail eingeben!");

		TextField phoneField = new TextField("Telefonnummer",
				facility.getTelephoneNr() == null ? "" : facility.getTelephoneNr(), "");
		phoneField.setHelperText("Erlaubt: Ziffern, Leerzeichen, optional führendes +");

		Button saveButton = new Button("Speichern", e -> {
			if (emailField.isInvalid()) {
				Notification.show("Ungültige E-Mail-Adresse!").addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}

			String tel = phoneField.getValue() == null ? "" : phoneField.getValue().trim();
			if (!tel.isEmpty() && !tel.matches("^\\+?[0-9 ]+$")) {
				Notification.show("Telefonnummer darf nur Ziffern, Leerzeichen und optional ein führendes + enthalten!")
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
				return;
			}

			String result = controller.standortBearbeiten(
					facility.getId(),
					addressField.getValue(),
					emailField.getValue(),
					tel
			);

			Notification notification = Notification.show(result);
			if (result.startsWith("Erfolg")) {
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				updateGrid();
				dialog.close();
			} else {
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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
