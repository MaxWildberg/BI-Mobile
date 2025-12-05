package bimobile.views;

import bimobile.controller.EmployeeController;
import bimobile.dao.FacilityDAO;
import bimobile.model.Employee;
import bimobile.model.Facility;
import bimobile.model.RoleType;
import bimobile.service.FacilityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;


import java.util.List;

@Route(value = "employees", layout = MainLayout.class)
@PermitAll
public class EmployeeView extends VerticalLayout {

    private final EmployeeController controller;
    private final FacilityService facilityService;

    private final Grid<Employee> grid = new Grid<>(Employee.class, false);

    private final TextField name = new TextField("Name");
    private final TextField lastname = new TextField("Lastname");
    private final TextField birthday = new TextField("Birthday");
    private final TextField email = new TextField("Email");
    private final TextField phone = new TextField("Phone");
    private final TextField loginName = new TextField("Login name");
    private final TextField password = new TextField("Password");

    private final ComboBox<RoleType> role = new ComboBox<>("Role");
    private final ComboBox<Facility> facility = new ComboBox<>("Facility");

    private Employee selectedEmployee;

    public EmployeeView(EmployeeController controller,
                        FacilityService facilityService) {
        this.controller = controller;
        this.facilityService = facilityService;

        add("Employee Management");

        configureGrid();
        configureForm();
        add(grid, createFormLayout());
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Employee::getName).setHeader("Name");
        grid.addColumn(Employee::getLastname).setHeader("Lastname");
        grid.addColumn(Employee::getBirthday).setHeader("Birthday");
        grid.addColumn(Employee::getEmail).setHeader("Email");
        grid.addColumn(Employee::getPhoneNumber).setHeader("Phone");
        grid.addColumn(e -> e.getRole() != null ? e.getRole().name() : "-")
                .setHeader("Role");

        grid.addColumn(e -> {
            Facility f = e.getFacility();
            return f != null ? f.getAddress() : "-";
        }).setHeader("Facility");

        grid.addColumn(e -> e.isActive() ? "Active" : "Inactive")
                .setHeader("Status");

        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedEmployee = event.getValue();
            populateForm(selectedEmployee);
        });
    }

    private HorizontalLayout createFormLayout() {
        Button saveButton = new Button("Save", e -> saveEmployee());
        Button deactivateButton = new Button("Deactivate", e -> deactivateSelected());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, deactivateButton);

        FormLayout form = new FormLayout();
        form.add(name, lastname, birthday, email, phone, loginName, password, role, facility);

        return new HorizontalLayout(form, buttons);
    }

    private void configureForm() {
        role.setItems(RoleType.values());

        List<Facility> facilities = facilityService.getAllFacilities();
        facility.setItems(facilities);
        facility.setItemLabelGenerator(Facility::getAddress);

        name.setRequired(true);
        lastname.setRequired(true);
        loginName.setRequired(true);
        password.setRequired(true);
    }

    private boolean validateForm() {
        if (name.getValue() == null || name.getValue().trim().isEmpty()) {
            Notification.show("Name is required");
            return false;
        }

        if (lastname.getValue() == null || lastname.getValue().trim().isEmpty()) {
            Notification.show("Lastname is required");
            return false;
        }

        if (loginName.getValue() == null || loginName.getValue().trim().isEmpty()) {
            Notification.show("Login name is required");
            return false;
        }

        if (password.getValue() == null || password.getValue().trim().isEmpty()) {
            Notification.show("Password is required");
            return false;
        }

        String emailValue = email.getValue();
        if (emailValue != null && !emailValue.trim().isEmpty() && !emailValue.contains("@")) {
            Notification.show("Email is not valid");
            return false;
        }

        if (role.getValue() == null) {
            Notification.show("Role is required");
            return false;
        }

        if (role.getValue() == RoleType.BRANCH_MANAGER && facility.getValue() == null) {
            Notification.show("Branch manager must be assigned to a facility");
            return false;
        }

        return true;
    }

    private void refreshGrid() {
        grid.setItems(controller.getAllEmployees());
    }

    private void populateForm(Employee employee) {
        if (employee == null) {
            name.clear();
            lastname.clear();
            birthday.clear();
            email.clear();
            phone.clear();
            loginName.clear();
            password.clear();
            role.clear();
            facility.clear();
            return;
        }

        name.setValue(employee.getName());
        lastname.setValue(employee.getLastname());
        birthday.setValue(employee.getBirthday());
        email.setValue(employee.getEmail());
        phone.setValue(employee.getPhoneNumber());
        loginName.setValue(employee.getLoginName());
        password.setValue(employee.getPasswordHash());
        role.setValue(employee.getRole());
        facility.setValue(employee.getFacility());
    }

    private void saveEmployee() {
        if (!validateForm()) {
            return;
        }

        try {
            if (selectedEmployee == null) {
                Employee employee = new Employee(
                        name.getValue(),
                        lastname.getValue(),
                        birthday.getValue()
                );

                employee.setEmail(email.getValue());
                employee.setPhoneNumber(phone.getValue());
                employee.setLoginName(loginName.getValue());
                employee.setPasswordHash(password.getValue());
                employee.setRole(role.getValue());
                employee.setFacility(facility.getValue());

                controller.createEmployee(employee);
                Notification.show("Employee created");
            } else {
                selectedEmployee.setName(name.getValue());
                selectedEmployee.setLastname(lastname.getValue());
                selectedEmployee.setBirthday(birthday.getValue());
                selectedEmployee.setEmail(email.getValue());
                selectedEmployee.setPhoneNumber(phone.getValue());
                selectedEmployee.setLoginName(loginName.getValue());
                selectedEmployee.setPasswordHash(password.getValue());
                selectedEmployee.setRole(role.getValue());
                selectedEmployee.setFacility(facility.getValue());

                controller.updateEmployee(selectedEmployee.getId(), selectedEmployee);
                Notification.show("Employee updated");
            }

            selectedEmployee = null;
            populateForm(null);
            refreshGrid();

        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deactivateSelected() {
        if (selectedEmployee == null) {
            Notification.show("Please select an employee first.");
            return;
        }

        boolean success = controller.deactivateEmployee(selectedEmployee.getId());
        if (success) {
            Notification.show("Employee deactivated");
            refreshGrid();
        } else {
            Notification.show("Could not deactivate employee");
        }
    }
}
