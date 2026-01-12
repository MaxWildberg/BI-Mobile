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

/**
 * Service-Schicht für die Mitarbeiterverwaltung.
 *
 * Beinhaltet die Logik zum Erstellen, Aktualisieren und Löschen von Mitarbeitern.
 * Setzt Berechtigungsprüfungen um
 *
 * @author Jan Lasse Stegmann
 */
@Service
public class EmployeeService implements EmployeeManagement {

    private final EmployeeDAO employeeDAO;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeChangeLogService changeLogService;

    public EmployeeService(EmployeeDAO employeeDAO,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmployeeChangeLogService changeLogService) {
        this.employeeDAO = employeeDAO;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.changeLogService = changeLogService;
    }

    //Lädt den aktuell eingeloggten Benutzer als Employee-Objekt
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

    private String getCurrentUserIdentifier() {
        Employee actor = getActingUser();
        return actor != null ? actor.getEmail() : "System/Admin";
    }

    @Transactional
    @Override
    public Employee createEmployee(Employee employee) {
        Employee actingUser = getActingUser();

        //Berechtigungsprüfung
        if (actingUser != null) {
            // Normale Mitarbeiter dürfen nichts anlegen
            if (actingUser.getRole() == RoleType.EMPLOYEE) {
                return null;
            }

            // Standortleiter dürfen nur im eigenen Standort und nur "Employees" anlegen
            if (actingUser.getRole() == RoleType.GENERAL_MANAGER) {
                if (employee.getFacility() == null ||
                        actingUser.getFacility() == null ||
                        !actingUser.getFacility().getId().equals(employee.getFacility().getId())) {
                    throw new IllegalArgumentException("Keine Berechtigung für fremden Standort.");
                }

                if (employee.getRole() != RoleType.EMPLOYEE) {
                    throw new IllegalArgumentException("Standortleiter dürfen nur Mitarbeiter (Employees) anlegen, keine Vorgesetzten.");
                }
            }
        }

        //Sicherstellen, dass pro Standort nur ein Manager existiert
        if (employee.getRole() == RoleType.GENERAL_MANAGER && employee.getFacility() != null) {
            validateUniqueManager(employee.getFacility().getId(), null);
        }

        employeeDAO.addEmployee(employee);
        createUserForEmployee(employee);

        changeLogService.logChange(employee, getCurrentUserIdentifier(), "Erstellt",
                "Mitarbeiter angelegt als " + employee.getRole());

        return employee;
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
                // Darf nur eigenen Standort bearbeiten
                if (existing.getFacility() == null ||
                        actingUser.getFacility() == null ||
                        !existing.getFacility().getId().equals(actingUser.getFacility().getId())) {
                    return null;
                }

                // Darf keine Vorgesetzten bearbeiten oder jemanden befördern
                if (existing.getRole() != RoleType.EMPLOYEE) {
                    throw new IllegalArgumentException("Sie dürfen nur Datensätze von Mitarbeitern (Employees) bearbeiten.");
                }
                if (updatedEmployee.getRole() != RoleType.EMPLOYEE) {
                    throw new IllegalArgumentException("Sie können keine Manager oder Geschäftsführer ernennen.");
                }
            }
        }

        //Unique Manager Check auch beim Update
        if (updatedEmployee.getRole() == RoleType.GENERAL_MANAGER && updatedEmployee.getFacility() != null) {
            validateUniqueManager(updatedEmployee.getFacility().getId(), existing.getId());
        }

        // Datenübernahme
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

        boolean statusChanged = existing.isActive() != updatedEmployee.isActive();
        existing.setActive(updatedEmployee.isActive());

        employeeDAO.updateEmployee(existing);
        updateUserForEmployee(existing);

        String details = "Daten aktualisiert.";
        if (statusChanged) details += " Status geändert.";
        changeLogService.logChange(existing, getCurrentUserIdentifier(), "Bearbeitet", details);

        return existing;
    }

    // Prüft, ob es im Standort schon einen aktiven Manager gibt
    private void validateUniqueManager(Long facilityId, Long excludeEmployeeId) {
        List<Employee> facilityEmployees = employeeDAO.getEmployeesByFacility(facilityId);
        for (Employee e : facilityEmployees) {
            if (e.isActive() && e.getRole() == RoleType.GENERAL_MANAGER) {
                if (excludeEmployeeId == null || !e.getId().equals(excludeEmployeeId)) {
                    throw new IllegalStateException("Dieser Standort hat bereits einen Standortleiter (General Manager).");
                }
            }
        }
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

    @Transactional
    public boolean toggleEmployeeStatus(Long id) {
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) return false;

        Employee actingUser = getActingUser();

        // Manager dürfen keine Vorgesetzten deaktivieren
        if (actingUser != null && actingUser.getRole() == RoleType.GENERAL_MANAGER) {
            if (existing.getFacility() == null || !existing.getFacility().getId().equals(actingUser.getFacility().getId())) return false;
            if (existing.getRole() != RoleType.EMPLOYEE) {
                return false;
            }
        }

        boolean newStatus = !existing.isActive();
        existing.setActive(newStatus);

        // Beim Reaktivieren prüfen, ob dadurch ein zweiter Manager entsteht
        if (newStatus && existing.getRole() == RoleType.GENERAL_MANAGER) {
            try {
                validateUniqueManager(existing.getFacility().getId(), existing.getId());
            } catch (IllegalStateException e) {
                return false;
            }
        }

        employeeDAO.updateEmployee(existing);

        // Login-User synchronisieren
        Optional<User> userOpt = userRepository.findByEmail(existing.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(newStatus);
            userRepository.save(user);
        }

        changeLogService.logChange(existing, getCurrentUserIdentifier(),
                newStatus ? "Aktiviert" : "Deaktiviert", "Status geändert");

        return true;
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) return false;

        Employee actingUser = getActingUser();
        if (actingUser != null && actingUser.getRole() == RoleType.GENERAL_MANAGER) {
            if (existing.getRole() != RoleType.EMPLOYEE) return false;
            if (existing.getFacility() == null || !existing.getFacility().getId().equals(actingUser.getFacility().getId())) return false;
        }

        // Protokoll-Referenz lösen, damit Löschung möglich ist
        changeLogService.logChange(existing, getCurrentUserIdentifier(), "Gelöscht", "Endgültig entfernt");
        changeLogService.detachEmployee(existing);

        Optional<User> userOpt = userRepository.findByEmail(existing.getEmail());
        userOpt.ifPresent(userRepository::delete);

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
        toggleEmployeeStatus(id);
    }

    @Transactional
    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        return Optional.ofNullable(employeeDAO.getEmployeeById(id));
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