package bimobile.service;

import bimobile.dao.EmployeeDAO;
import bimobile.dao.UserRepository;
import bimobile.model.Employee;
import bimobile.model.RoleType;
import bimobile.model.User;
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

    // Platzhalter für späteren LoginService
    private Employee getActingUser() {
        return null;
    }

    @Transactional
    @Override
    public Employee createEmployee(Employee employee) {

        Employee actingUser = getActingUser();

        // Rollenprüfung (später durch LoginService ersetzt)
        if (actingUser != null) {
            if (actingUser.getRole() == RoleType.EMPLOYEE) {
                return null;
            }

            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (employee.getFacility() == null ||
                        !actingUser.getFacility().getId().equals(employee.getFacility().getId())) {
                    return null;
                }

                if (employee.getRole() == RoleType.GENERAL_MANAGER){
                    return null;
                }
            }
        }

        // Regel: pro Facility nur ein aktiver BRANCH_MANAGER
        if (employee.getRole() == RoleType.GENERAL_MANAGER && employee.getFacility() != null) {
            List<Employee> facilityEmployees =
                    employeeDAO.getEmployeesByFacility(employee.getFacility().getId());

            for (Employee e : facilityEmployees) {
                if (e.isActive() &&
                        e.getRole() == RoleType.GENERAL_MANAGER) {
                    return null;
                }
            }
        }

        employeeDAO.addEmployee(employee);

        // User für Login erstellen
        createUserForEmployee(employee);

        return employee;
    }

    /**
     * Erstellt einen User-Eintrag für den Mitarbeiter (für Login).
     * Die E-Mail des Mitarbeiters wird als Login verwendet.
     */
    private void createUserForEmployee(Employee employee) {
        // Prüfen ob User mit dieser E-Mail schon existiert
        if (userRepository.existsByEmail(employee.getEmail())) {
            return;
        }

        User user = new User(
                employee.getName(),
                employee.getLastname(),
                employee.getEmail(),
                passwordEncoder.encode(employee.getPasswordHash()),  // Passwort hashen
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

        // Fachliche Regel: pro Facility nur ein Manager
        if (updatedEmployee.getRole() == RoleType.GENERAL_MANAGER &&
                updatedEmployee.getFacility() != null) {

            List<Employee> facilityEmployees =
                    employeeDAO.getEmployeesByFacility(updatedEmployee.getFacility().getId());

            for (Employee e : facilityEmployees) {
                if (!e.getId().equals(existing.getId()) &&
                        e.isActive() &&
                        e.getRole() == RoleType.GENERAL_MANAGER) {
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

        // User synchronisieren
        updateUserForEmployee(existing);

        return existing;
    }

    /**
     * Aktualisiert den User-Eintrag wenn ein Mitarbeiter geändert wird.
     */
    private void updateUserForEmployee(Employee employee) {
        Optional<User> userOpt = userRepository.findByEmail(employee.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(employee.getName());
            user.setLastName(employee.getLastname());
            user.setRole(employee.getRole());
            user.setFacility(employee.getFacility());
            user.setEnabled(employee.isActive());

            // Passwort nur updaten wenn es geändert wurde (nicht leer)
            if (employee.getPasswordHash() != null && !employee.getPasswordHash().isEmpty()) {
                user.setPassword(passwordEncoder.encode(employee.getPasswordHash()));
            }

            userRepository.save(user);
        } else {
            // Falls User noch nicht existiert, erstellen
            createUserForEmployee(employee);
        }
    }

    @Transactional
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

            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (existing.getRole() == RoleType.GENERAL_MANAGER) {
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

        // User auch deaktivieren
        Optional<User> userOpt = userRepository.findByEmail(existing.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(false);
            userRepository.save(user);
        }
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