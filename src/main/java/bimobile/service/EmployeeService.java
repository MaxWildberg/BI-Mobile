package bimobile.service;

import bimobile.dao.EmployeeDAO;
import bimobile.model.Employee;
import bimobile.model.RoleType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements EmployeeManagement {

    private final EmployeeDAO employeeDAO;
    public EmployeeService(EmployeeDAO employeeDAO){
        this.employeeDAO = employeeDAO;
    }

    // Platzhalter für späteren LoginService
    private Employee getActingUser() {
        return null;
    }

    @Override
    public Employee createEmployee(Employee employee) {

        Employee actingUser = getActingUser();

        // Rollenprüfung (später durch LoginService ersetzt)
        if (actingUser != null) {
            if (actingUser.getRole() == RoleType.EMPLOYEE) {
                return null;
            }

            if (actingUser.getRole() == RoleType.BRANCH_MANAGER) {
                if (employee.getFacility() == null ||
                        !actingUser.getFacility().getId().equals(employee.getFacility().getId())) {
                    return null;
                }

                if (employee.getRole() == RoleType.BRANCH_MANAGER) {
                    return null;
                }
            }
        }

        // Regel: pro Facility nur ein aktiver BRANCH_MANAGER
        if (employee.getRole() == RoleType.BRANCH_MANAGER && employee.getFacility() != null) {
            List<Employee> facilityEmployees =
                    employeeDAO.getEmployeesByFacility(employee.getFacility().getId());

            for (Employee e : facilityEmployees) {
                if (e.isActive() &&
                        e.getRole() == RoleType.BRANCH_MANAGER) {
                    return null;
                }
            }
        }

        employeeDAO.addEmployee(employee);
        return employee;
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        return Optional.ofNullable(employeeDAO.getEmployeeById(id));
    }

    @Override
    public Employee updateEmployee(Long id, Employee updatedEmployee) {

        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) {
            return null;
        }

        Employee actingUser = getActingUser();

        // Rollenprüfung
        if (actingUser != null) {

            // EMPLOYEE darf nichts bearbeiten
            if (actingUser.getRole() == RoleType.EMPLOYEE) {
                return null;
            }

            // BRANCH_MANAGER: nur eigener Standort, keine Rollenänderung zu Manager
            if (actingUser.getRole() == RoleType.BRANCH_MANAGER) {

                if (existing.getFacility() == null ||
                        actingUser.getFacility() == null ||
                        !existing.getFacility().getId().equals(actingUser.getFacility().getId())) {
                    return null;
                }

                if (updatedEmployee.getRole() == RoleType.BRANCH_MANAGER &&
                        !existing.getId().equals(actingUser.getId())) {
                    return null;
                }
            }
        }

        // Fachliche Regel: pro Facility nur ein Manager
        if (updatedEmployee.getRole() == RoleType.BRANCH_MANAGER &&
                updatedEmployee.getFacility() != null) {

            List<Employee> facilityEmployees =
                    employeeDAO.getEmployeesByFacility(updatedEmployee.getFacility().getId());

            for (Employee e : facilityEmployees) {
                if (!e.getId().equals(existing.getId()) &&
                        e.isActive() &&
                        e.getRole() == RoleType.BRANCH_MANAGER) {
                    return null;
                }
            }
        }

        // Werte übernehmen
        existing.setName(updatedEmployee.getName());
        existing.setLastname(updatedEmployee.getLastname());
        existing.setBirthday(updatedEmployee.getBirthday());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setPhoneNumber(updatedEmployee.getPhoneNumber());
        existing.setLoginName(updatedEmployee.getLoginName());
        existing.setPasswordHash(updatedEmployee.getPasswordHash());
        existing.setRole(updatedEmployee.getRole());
        existing.setFacility(updatedEmployee.getFacility());
        existing.setActive(updatedEmployee.isActive());

        employeeDAO.updateEmployee(existing);
        return existing;
    }

    @Override
    public void deactivateEmployee(Long id) {

        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) {
            return;
        }

        Employee actingUser = getActingUser();

        if (actingUser != null) {

            if (actingUser.getRole() == RoleType.EMPLOYEE) {
                return;
            }

            if (actingUser.getRole() == RoleType.BRANCH_MANAGER) {
                if (existing.getRole() == RoleType.BRANCH_MANAGER) {
                    return;
                }

                if (existing.getFacility() == null ||
                        !existing.getFacility().getId().equals(actingUser.getFacility().getId())) {
                    return;
                }
            }
        }

        existing.setActive(false);
        employeeDAO.updateEmployee(existing);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    @Override
    public List<Employee> getEmployeesByFacility(Long facilityId) {
        return employeeDAO.getEmployeesByFacility(facilityId);
    }
}