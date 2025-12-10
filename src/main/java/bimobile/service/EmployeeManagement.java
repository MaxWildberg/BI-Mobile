package bimobile.service;

import bimobile.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeManagement {

    Employee createEmployee(Employee employee);

    Optional<Employee> getEmployeeById(Long id);

    Employee updateEmployee(Long id, Employee updatedEmployee);

    void deactivateEmployee(Long id);

    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByFacility(Long facilityId);
}