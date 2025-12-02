package bimobile.service;

import bimobile.dao.EmployeeDAO;
import bimobile.model.Employee;
import java.util.List;

public class EmployeeService {

    private EmployeeDAO employeeDAO = new EmployeeDAO();

    public void addEmployee(Employee employee) {
        employeeDAO.addEmployee(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public Employee getEmployeeById(Long id) {
        return employeeDAO.getEmployeeById(id);
    }
}