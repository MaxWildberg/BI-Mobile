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
 * Hilfsklasse für Sicherheits- und Berechtigungsprüfungen.
 *
 * Stellt Methoden bereit, um den aktuell eingeloggten Benutzer,
 * dessen Rolle und Zugriffsrechte abzufragen.
 *
 * @author Jan Lasse Stegmann
 */
@Component
public class AuthorizationUtils {

    private static UserRepository userRepository;

    // Ermöglicht den Zugriff auf das Repository auch in statischen Methoden
    public AuthorizationUtils(UserRepository repo) {
        userRepository = repo;
    }

    /**
     * Liest den Benutzernamen (E-Mail) aus dem Spring Security Context.
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails u) {
            return u.getUsername();
        }
        return principal.toString();
    }

    /**
     * Ermittelt die Rolle (RoleType) des aktuellen Benutzers.
     */
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

    // Rollen-Checks

    public static boolean isManagement() {
        return getCurrentRoleType() == RoleType.MANAGING_DIRECTOR;
    }

    public static boolean isBranchManager() {
        return getCurrentRoleType() == RoleType.GENERAL_MANAGER;
    }

    public static boolean isEmployee() {
        return getCurrentRoleType() == RoleType.EMPLOYEE;
    }

    // Berechtigungsprüfungen (Business-Logik)

    /**
     * Prüft, ob der User auf die Standortverwaltung zugreifen darf.
     * Regel: Employees dürfen keine Standorte sehen.
     */
    public static boolean canAccessLocations() {
        return !isEmployee();
    }

    /**
     * Prüft, ob der User auf die Mitarbeiterverwaltung zugreifen darf.
     * Regel: Employees dürfen keine Mitarbeiter sehen/verwalten.
     */
    public static boolean canAccessEmployees() {
        return !isEmployee();
    }

    /**
     * Prüft, ob der User Kunden löschen darf.
     * Regel: Employees dürfen keine Kunden löschen.
     */
    public static boolean canDeleteCustomers() {
        return !isEmployee();
    }

    // Datenbank-Zugriff auf aktuellen User

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

    // Prüft, ob ein übergebener Standort dem des Users entspricht
    public static boolean isSameFacility(Facility facility) {
        Facility current = getCurrentUserFacility();
        return current != null && facility != null &&
                current.getId().equals(facility.getId());
    }
}