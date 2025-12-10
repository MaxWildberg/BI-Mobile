package bimobile.views;

import bimobile.controller.EmployeeController;
import bimobile.model.Employee;
import bimobile.model.Facility;
import bimobile.model.RoleType;
import bimobile.security.AuthorizationUtils;
import bimobile.service.FacilityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Lasse
 * Description: Employee management with role and facility restrictions.
 */
@PermitAll
@Route(value = "employees", layout = MainLayout.class)
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

    // Never display password hash
    private final TextField password = new TextField("Password");

    private final ComboBox<RoleType> role = new ComboBox<>("Role");
    private final ComboBox<Facility> facility = new ComboBox<>("Facility");

    private Employee selectedEmployee;

    // Final roles: MANAGEMENT, BRANCH_MANAGER, EMPLOYEE
    private final boolean isManagement = AuthorizationUtils.isManagement();
    private final boolean isBranchManager = AuthorizationUtils.isBranchManager();

    private final Facility currentFacility = AuthorizationUtils.getCurrentUserFacility();

    public EmployeeView(EmployeeController controller,
                        FacilityService facilityService) {

        this.controller = controller;
        this.facilityService = facilityService;

        // Employees haben keinen Zugriff
        if (!isManagement && !isBranchManager) {
            removeAll();
            Span info = new Span("Sie haben keine Berechtigung für diesen Bereich.");
            add(info);
            return;
        }

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
        grid.addColumn(e -> e.getRole() != null ? e.getRole().name() : "-").setHeader("Role");

        grid.addColumn(e -> e.getFacility() != null ? e.getFacility().getAddress() : "-")
                .setHeader("Facility");

        grid.addColumn(e -> e.isActive() ? "Active" : "Inactive")
                .setHeader("Status");

        // Auswahl
        grid.asSingleSelect().addValueChangeListener(event -> {
            Employee emp = event.getValue();

            // Branch Manager darf NUR Mitarbeiter aus eigener Filiale bearbeiten
            if (emp != null && isBranchManager) {
                if (emp.getFacility() != null &&
                        !emp.getFacility().getId().equals(currentFacility.getId())) {

                    Notification.show("Sie können nur Mitarbeiter aus Ihrem eigenen Standort bearbeiten.");
                    grid.deselectAll();
                    return;
                }
            }

            selectedEmployee = emp;
            populateForm(emp);
        });
    }

    private HorizontalLayout createFormLayout() {
        Button saveButton = new Button("Save", e -> saveEmployee());
        Button deactivateButton = new Button("Deactivate", e -> deactivateSelected());

        // Branch Manager darf Facility NICHT ändern
        if (isBranchManager) {
            facility.setEnabled(false);
        }

        FormLayout form = new FormLayout(
                name, lastname, birthday, email, phone,
                loginName, password, role, facility
        );

        return new HorizontalLayout(form, new HorizontalLayout(saveButton, deactivateButton));
    }

    private void configureForm() {
        role.setItems(RoleType.values());

        List<Facility> facilities = facilityService.getAllFacilities();

        if (isBranchManager) {
            facility.setItems(currentFacility);
        } else {
            facility.setItems(facilities);
        }

        facility.setItemLabelGenerator(Facility::getAddress);
    }

    private void refreshGrid() {
        List<Employee> employees = controller.getAllEmployees();

        if (isBranchManager) {
            employees = employees.stream()
                    .filter(e -> e.getFacility() != null &&
                            e.getFacility().getId().equals(currentFacility.getId()))
                    .collect(Collectors.toList());
        }

        grid.setItems(employees);
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

        password.clear(); // wichtig: keinen Hash anzeigen

        role.setValue(employee.getRole());
        facility.setValue(employee.getFacility());
    }

    private void saveEmployee() {

        // Branch Manager darf nur eigenes Facility setzen
        if (isBranchManager && facility.getValue() != currentFacility) {
            Notification.show("Manager dürfen Mitarbeiter nur dem eigenen Standort zuordnen.");
            return;
        }

        try {
            if (selectedEmployee == null) {

                Employee emp = new Employee(
                        name.getValue(),
                        lastname.getValue(),
                        birthday.getValue()
                );

                emp.setEmail(email.getValue());
                emp.setPhoneNumber(phone.getValue());
                emp.setLoginName(loginName.getValue());
                emp.setPasswordHash(password.getValue());
                emp.setRole(role.getValue());
                emp.setFacility(facility.getValue());

                controller.createEmployee(emp);
                Notification.show("Employee created");

            } else {

                selectedEmployee.setName(name.getValue());
                selectedEmployee.setLastname(lastname.getValue());
                selectedEmployee.setBirthday(birthday.getValue());
                selectedEmployee.setEmail(email.getValue());
                selectedEmployee.setPhoneNumber(phone.getValue());
                selectedEmployee.setLoginName(loginName.getValue());

                if (!password.isEmpty()) {
                    selectedEmployee.setPasswordHash(password.getValue());
                }

                selectedEmployee.setRole(role.getValue());
                selectedEmployee.setFacility(facility.getValue());

                controller.updateEmployee(selectedEmployee.getId(), selectedEmployee);
                Notification.show("Employee updated");
            }

            selectedEmployee = null;
            populateForm(null);
            refreshGrid();

        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage());
        }
    }

    private void deactivateSelected() {
        if (selectedEmployee == null) {
            Notification.show("Please select an employee first.");
            return;
        }

        if (isBranchManager &&
                !selectedEmployee.getFacility().getId().equals(currentFacility.getId())) {

            Notification.show("Sie können nur Mitarbeiter aus Ihrem eigenen Standort deaktivieren.");
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
