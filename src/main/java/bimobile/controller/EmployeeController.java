package bimobile.controller;

import bimobile.model.Employee;
import bimobile.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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

    public boolean deactivateEmployee(Long id) {
        Employee employee = employeeService.getEmployeeById(id).orElse(null);
        if (employee == null) {
            return false;
        }

        employeeService.deactivateEmployee(id);
        return true;
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
