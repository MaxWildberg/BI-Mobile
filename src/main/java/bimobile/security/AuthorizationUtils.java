package bimobile.security;

import bimobile.dao.UserRepository;
import bimobile.model.Facility;
import bimobile.model.RoleType;
import bimobile.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Author: Lasse
 * Description: Role and facility checks.
 */
@Component
public class AuthorizationUtils {

    private static UserRepository userRepository;

    public AuthorizationUtils(UserRepository repo) {
        userRepository = repo;
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails u) {
            return u.getUsername();
        }
        return principal.toString();
    }

    public static RoleType getCurrentRoleType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        for (GrantedAuthority a : auth.getAuthorities()) {
            String role = a.getAuthority().replace("ROLE_", "");
            try {
                return RoleType.valueOf(role);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // MANAGEMENT
    public static boolean isManagement() {
        return getCurrentRoleType() == RoleType.MANAGING_DIRECTOR;
    }

    // BRANCH_MANAGER
    public static boolean isBranchManager() {
        return getCurrentRoleType() == RoleType.GENERAL_MANAGER;
    }

    // EMPLOYEE
    public static boolean isEmployee() {
        return getCurrentRoleType() == RoleType.EMPLOYEE;
    }

    // --- NEU: BERECHTIGUNGSPRÜFUNGEN GEMÄSS TABELLE ---

    /**
     * Prüft, ob der User auf die Standortverwaltung zugreifen darf.
     * Employees dürfen KEINE Standorte sehen.
     */
    public static boolean canAccessLocations() {
        // Jeder darf Standorte sehen, AUSSER Mitarbeiter (Employee)
        return !isEmployee();
    }

    /**
     * Prüft, ob der User auf die Mitarbeiterverwaltung zugreifen darf.
     * Employees dürfen KEINE Mitarbeiter sehen/verwalten.
     */
    public static boolean canAccessEmployees() {
        // Jeder darf Mitarbeiter sehen, AUSSER Mitarbeiter (Employee)
        return !isEmployee();
    }

    /**
     * Prüft, ob der User Kunden löschen darf.
     * Employees dürfen KEINE Kunden löschen.
     */
    public static boolean canDeleteCustomers() {
        return !isEmployee();
    }

    // Current User loaded from DB
    public static Optional<User> getCurrentUser() {
        if (userRepository == null) return Optional.empty();
        String email = getCurrentUsername();
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email);
    }

    public static Facility getCurrentUserFacility() {
        return getCurrentUser()
                .map(User::getFacility)
                .orElse(null);
    }

    public static boolean isSameFacility(Facility facility) {
        Facility current = getCurrentUserFacility();
        return current != null && facility != null &&
                current.getId().equals(facility.getId());
    }
}