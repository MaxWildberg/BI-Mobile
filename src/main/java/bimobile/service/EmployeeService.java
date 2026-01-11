package bimobile.service;

import bimobile.dao.EmployeeDAO;
import bimobile.dao.UserRepository;
import bimobile.model.Employee;
import bimobile.model.RoleType;
import bimobile.model.User;
import bimobile.security.AuthorizationUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements EmployeeManagement {

    private final EmployeeDAO employeeDAO;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeDAO employeeDAO, UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.employeeDAO = employeeDAO;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Employee getActingUser() {
        Optional<User> userOpt = AuthorizationUtils.getCurrentUser();
        if (userOpt.isPresent()) {
            return employeeDAO.getAllEmployees().stream()
                    .filter(e -> e.getEmail().equals(userOpt.get().getEmail()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Transactional
    @Override
    public Employee createEmployee(Employee employee) {
        Employee actingUser = getActingUser();

        if (actingUser != null) {
            if (actingUser.getRole() == RoleType.EMPLOYEE) return null;
            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (employee.getFacility() == null ||
                        actingUser.getFacility() == null ||
                        !actingUser.getFacility().getId().equals(employee.getFacility().getId())) {
                    return null;
                }
                if (employee.getRole() == RoleType.GENERAL_MANAGER){
                    return null;
                }
            }
        }

        if (employee.getRole() == RoleType.GENERAL_MANAGER && employee.getFacility() != null) {
            List<Employee> facilityEmployees =
                    employeeDAO.getEmployeesByFacility(employee.getFacility().getId());
            for (Employee e : facilityEmployees) {
                if (e.isActive() && e.getRole() == RoleType.GENERAL_MANAGER) return null;
            }
        }

        employeeDAO.addEmployee(employee);
        createUserForEmployee(employee);
        return employee;
    }

    private void createUserForEmployee(Employee employee) {
        if (userRepository.existsByEmail(employee.getEmail())) return;
        User user = new User(
                employee.getName(),
                employee.getLastname(),
                employee.getEmail(),
                passwordEncoder.encode(employee.getPasswordHash()),
                employee.getRole()
        );
        user.setFacility(employee.getFacility());
        user.setEnabled(employee.isActive());
        userRepository.save(user);
    }

    @Transactional
    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        return Optional.ofNullable(employeeDAO.getEmployeeById(id));
    }

    @Transactional
    @Override
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) return null;

        Employee actingUser = getActingUser();
        if (actingUser != null) {
            if (actingUser.getRole() == RoleType.EMPLOYEE) return null;
            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (existing.getFacility() == null ||
                        actingUser.getFacility() == null ||
                        !existing.getFacility().getId().equals(actingUser.getFacility().getId())) {
                    return null;
                }
                if (updatedEmployee.getRole() == RoleType.GENERAL_MANAGER &&
                        !existing.getId().equals(actingUser.getId())) {
                    return null;
                }
            }
        }

        existing.setName(updatedEmployee.getName());
        existing.setLastname(updatedEmployee.getLastname());
        existing.setBirthday(updatedEmployee.getBirthday());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setPhoneNumber(updatedEmployee.getPhoneNumber());
        existing.setLoginName(updatedEmployee.getLoginName());

        if (updatedEmployee.getPasswordHash() != null && !updatedEmployee.getPasswordHash().isEmpty()) {
            existing.setPasswordHash(updatedEmployee.getPasswordHash());
        }

        existing.setRole(updatedEmployee.getRole());
        existing.setFacility(updatedEmployee.getFacility());
        existing.setActive(updatedEmployee.isActive());

        employeeDAO.updateEmployee(existing);
        updateUserForEmployee(existing);
        return existing;
    }

    private void updateUserForEmployee(Employee employee) {
        Optional<User> userOpt = userRepository.findByEmail(employee.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(employee.getName());
            user.setLastName(employee.getLastname());
            user.setRole(employee.getRole());
            user.setFacility(employee.getFacility());
            user.setEnabled(employee.isActive());
            if (employee.getPasswordHash() != null && !employee.getPasswordHash().isEmpty()) {
                user.setPassword(passwordEncoder.encode(employee.getPasswordHash()));
            }
            userRepository.save(user);
        } else {
            createUserForEmployee(employee);
        }
    }

    /**
     * Schaltet den Status um (Aktiv <-> Inaktiv).
     * Gibt true zurück, wenn erfolgreich.
     */
    @Transactional
    public boolean toggleEmployeeStatus(Long id) {
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) return false;

        Employee actingUser = getActingUser();
        if (actingUser != null) {
            if (actingUser.getRole() == RoleType.EMPLOYEE) return false;
            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (existing.getRole() == RoleType.GENERAL_MANAGER && !existing.getId().equals(actingUser.getId())) return false;
                if (existing.getFacility() == null || !existing.getFacility().getId().equals(actingUser.getFacility().getId())) return false;
            }
        }

        // Status umkehren
        boolean newStatus = !existing.isActive();
        existing.setActive(newStatus);
        employeeDAO.updateEmployee(existing);

        // Auch User Login sperren/entsperren
        Optional<User> userOpt = userRepository.findByEmail(existing.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(newStatus);
            userRepository.save(user);
        }
        return true;
    }

    /**
     * Löscht den Mitarbeiter endgültig.
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) return false;

        Employee actingUser = getActingUser();
        if (actingUser != null) {
            if (actingUser.getRole() == RoleType.EMPLOYEE) return false;
            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (existing.getRole() == RoleType.GENERAL_MANAGER) return false;
                if (existing.getFacility() == null || !existing.getFacility().getId().equals(actingUser.getFacility().getId())) return false;
            }
        }

        // 1. User Login löschen
        Optional<User> userOpt = userRepository.findByEmail(existing.getEmail());
        userOpt.ifPresent(userRepository::delete);

        // 2. Mitarbeiter löschen
        try {
            employeeDAO.deleteEmployee(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void deactivateEmployee(Long id) {
        // Fallback für alte Aufrufe: Wir nutzen toggle
        toggleEmployeeStatus(id);
    }

    @Transactional
    @Override
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    @Transactional
    @Override
    public List<Employee> getEmployeesByFacility(Long facilityId) {
        return employeeDAO.getEmployeesByFacility(facilityId);
    }
}