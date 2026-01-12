package bimobile.controller;

import bimobile.model.Employee;
import bimobile.service.EmployeeService;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

/**
 * Controller-Schicht für die Mitarbeiterverwaltung.
 *
 * Dient als Schnittstelle zwischen den Vaadin-Views und dem EmployeeService.
 * Die Klasse hält die Views sauber, indem sie Datenbank- und Logik-Aufrufe
 * direkt an den Service delegiert.
 *
 * @author Jan Lasse Stegmann
 */
@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    public Employee createEmployee(Employee employee) {
        return employeeService.createEmployee(employee);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        return employeeService.updateEmployee(id, updatedEmployee);
    }

    // Schaltet den Status eines Mitarbeiters um (aktiv <-> inaktiv)
    public boolean toggleEmployeeStatus(Long id) {
        return employeeService.toggleEmployeeStatus(id);
    }

    // Löscht den Datensatz permanent (wird über den Mülleimer-Button ausgelöst)
    public boolean deleteEmployee(Long id) {
        return employeeService.deleteEmployee(id);
    }

    public boolean deactivateEmployee(Long id) {
        return employeeService.toggleEmployeeStatus(id);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeService.getEmployeeById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    public List<Employee> getEmployeesByFacility(Long facilityId) {
        return employeeService.getEmployeesByFacility(facilityId);
    }
}