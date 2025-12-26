package bimobile.views;

import bimobile.controller.EmployeeController;
import bimobile.model.Employee;
import bimobile.model.Facility;
import bimobile.model.RoleType;
import bimobile.service.FacilityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modal-Dialog zum Erstellen und Bearbeiten von Mitarbeitern.
 * Jetzt mit Validierung für Passwort und Login-Name.
 */
public class EmployeeFormDialog extends Dialog {

    private final EmployeeController controller;
    private final FacilityService facilityService;
    private final Runnable onSaveSuccess;
    private final boolean isBranchManager;
    private final Facility currentFacility;

    private Employee employee;
    private final Binder<Employee> binder = new Binder<>(Employee.class);

    // UI Felder
    private final TextField name = new TextField("Vorname");
    private final TextField lastname = new TextField("Nachname");
    private final DatePicker birthday = new DatePicker("Geburtsdatum");
    private final EmailField email = new EmailField("E-Mail");
    private final TextField phoneNumber = new TextField("Telefon");
    private final TextField loginName = new TextField("Login-Name");
    private final PasswordField passwordHash = new PasswordField("Passwort");
    private final ComboBox<RoleType> role = new ComboBox<>("Rolle");
    private final ComboBox<Facility> facility = new ComboBox<>("Standort");

    private final Button saveButton = new Button("Speichern");
    private final Button cancelButton = new Button("Abbrechen");
    private final Button deactivateButton = new Button("Deaktivieren");

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public EmployeeFormDialog(Employee employeeToEdit,
                              EmployeeController controller,
                              FacilityService facilityService,
                              boolean isBranchManager,
                              Facility currentFacility,
                              Runnable onSaveSuccess) {
        this.controller = controller;
        this.facilityService = facilityService;
        this.isBranchManager = isBranchManager;
        this.currentFacility = currentFacility;
        this.onSaveSuccess = onSaveSuccess;

        this.employee = employeeToEdit != null ? employeeToEdit : new Employee();

        initUI();
        bindForm();
        configurePermissions();
        populateForm();
    }

    private void initUI() {
        setHeaderTitle(employee.getId() == null ? "Neuen Mitarbeiter anlegen" : "Mitarbeiter bearbeiten");
        setWidth("600px");

        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("dd.MM.yyyy");
        birthday.setI18n(i18n);
        birthday.setPlaceholder("TT.MM.JJJJ");

        FormLayout formLayout = new FormLayout();
        formLayout.add(name, lastname, birthday, email, phoneNumber, loginName, passwordHash, role, facility);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        formLayout.setColspan(email, 2);
        formLayout.setColspan(loginName, 2);
        formLayout.setColspan(passwordHash, 2);
        formLayout.setColspan(facility, 2);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveClicked());

        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> this.close());

        deactivateButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deactivateButton.addClickListener(e -> deactivateClicked());
        deactivateButton.setVisible(employee.getId() != null && employee.isActive());

        HorizontalLayout footer = new HorizontalLayout(cancelButton, deactivateButton, saveButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.getStyle().set("margin-top", "20px");

        VerticalLayout content = new VerticalLayout(formLayout, footer);
        content.setPadding(false);
        add(content);
    }

    private void configurePermissions() {
        role.setItems(RoleType.values());
        role.setItemLabelGenerator(this::formatRole);

        if (isBranchManager) {
            facility.setItems(currentFacility);
            facility.setValue(currentFacility);
            facility.setEnabled(false);
        } else {
            List<Facility> facilities = facilityService.getAllFacilities();
            facility.setItems(facilities);
        }
        facility.setItemLabelGenerator(Facility::getAddress);

        if (employee.getId() != null) {
            passwordHash.setPlaceholder("Nur ausfüllen zum Ändern");
        }
    }

    private void bindForm() {
        binder.forField(name).asRequired("Vorname ist Pflicht").bind(Employee::getName, Employee::setName);
        binder.forField(lastname).asRequired("Nachname ist Pflicht").bind(Employee::getLastname, Employee::setLastname);
        binder.forField(email).asRequired("E-Mail ist Pflicht").bind(Employee::getEmail, Employee::setEmail);
        binder.forField(role).asRequired("Rolle ist Pflicht").bind(Employee::getRole, Employee::setRole);

        // [UPDATE] Login-Name ist jetzt Pflicht!
        binder.forField(loginName).asRequired("Login-Name ist Pflicht").bind(Employee::getLoginName, Employee::setLoginName);

        binder.forField(birthday)
                .asRequired("Geburtsdatum ist Pflicht")
                .withConverter(
                        localDate -> localDate == null ? null : localDate.format(dateFormatter),
                        string -> string == null || string.isEmpty() ? null : LocalDate.parse(string, dateFormatter),
                        "Bitte gültiges Datum (TT.MM.JJJJ) eingeben"
                )
                .bind(Employee::getBirthday, Employee::setBirthday);

        binder.bind(phoneNumber, Employee::getPhoneNumber, Employee::setPhoneNumber);
    }

    private void populateForm() {
        binder.readBean(employee);

        if (employee.getId() != null) {
            passwordHash.clear();
        }

        if (isBranchManager) {
            facility.setValue(currentFacility);
        } else {
            facility.setValue(employee.getFacility());
        }
    }

    private void saveClicked() {
        try {
            // 1. Binder Validierung (Name, Login, Email etc.)
            binder.writeBean(employee);

            // 2. Passwort Validierung
            // Wenn der User NEU ist (ID == null) UND das Passwortfeld leer ist -> FEHLER
            if (employee.getId() == null && passwordHash.isEmpty()) {
                Notification.show("Für neue Mitarbeiter muss ein Passwort vergeben werden.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                // Fokus ins Passwortfeld setzen, damit der User es sieht
                passwordHash.focus();
                return;
            }

            // Wenn Passwort eingegeben wurde, setzen
            if (!passwordHash.isEmpty()) {
                employee.setPasswordHash(passwordHash.getValue());
            }

            // 3. Facility (Standort) setzen
            if (isBranchManager) {
                employee.setFacility(currentFacility);
            } else {
                employee.setFacility(facility.getValue());
            }

            if (employee.getFacility() == null) {
                Notification.show("Bitte einen Standort auswählen.").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // 4. Speichern
            if (employee.getId() == null) {
                controller.createEmployee(employee);
                Notification.show("Mitarbeiter erfolgreich angelegt.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                controller.updateEmployee(employee.getId(), employee);
                Notification.show("Mitarbeiter erfolgreich aktualisiert.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            this.close();
            if (onSaveSuccess != null) {
                onSaveSuccess.run();
            }

        } catch (ValidationException e) {
            Notification.show("Bitte füllen Sie alle Pflichtfelder aus.").addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            // Hier fangen wir ab, falls doch noch was schief geht (z.B. Login-Name schon vergeben)
            String msg = e.getMessage();
            if (msg.contains("ConstraintViolation") || msg.contains("Duplicate entry")) {
                Notification.show("Fehler: Dieser Login-Name oder E-Mail existiert bereits.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                Notification.show("Fehler beim Speichern: " + msg).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void deactivateClicked() {
        if (employee.getId() == null) return;
        try {
            boolean success = controller.deactivateEmployee(employee.getId());
            if (success) {
                Notification.show("Mitarbeiter deaktiviert.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                this.close();
                if (onSaveSuccess != null) {
                    onSaveSuccess.run();
                }
            } else {
                Notification.show("Fehler beim Deaktivieren.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String formatRole(RoleType role) {
        return switch (role) {
            case MANAGING_DIRECTOR -> "Geschäftsführer";
            case GENERAL_MANAGER -> "Standortleiter";
            case EMPLOYEE -> "Mitarbeiter";
        };
    }
}