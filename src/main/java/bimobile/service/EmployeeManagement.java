package bimobile.service;

import bimobile.model.Employee;
import java.util.List;
import java.util.Optional;

/**
 * Definiert die Schnittstelle für die Mitarbeiterverwaltung.
 *
 * @author Jan Lasse Stegmann
 */
public interface EmployeeManagement {

    Employee createEmployee(Employee employee);

    Optional<Employee> getEmployeeById(Long id);

    Employee updateEmployee(Long id, Employee updatedEmployee);

    void deactivateEmployee(Long id);

    List<Employee> getAllEmployees();

    // Filtert Mitarbeiter basierend auf ihrem zugewiesenen Standort
    List<Employee> getEmployeesByFacility(Long facilityId);
}