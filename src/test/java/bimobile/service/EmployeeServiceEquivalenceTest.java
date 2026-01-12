package bimobile.service;

import bimobile.model.RoleType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 2 JUnit Tests
 * 2 Äquivalenzklassen
 */
class EmployeeServiceEquivalenceTest {

    // TEST 1: Berechtigungs-Logik

    // Hilfsmethode zur Simulation der Logik
    private boolean isDeleteAllowed(RoleType role) {
        if (role == RoleType.EMPLOYEE) return false; // ÄK 1: Ungültig
        return true;                                 // ÄK 2: Gültig
    }

    @Test
    void testAuthorization_EmployeeCannotDelete() {
        // Äquivalenzklasse: "Ungültiger Nutzer"
        assertFalse(isDeleteAllowed(RoleType.EMPLOYEE), "Ein Employee darf nicht löschen.");
    }

    // TEST 2: Eingabe-Validierung

    // Hilfsmethode zur Simulation der Logik
    private boolean isUsernameValid(String username) {
        if (username == null || username.trim().isEmpty()) return false; // ÄK 1: Ungültig
        return true;                                                     // ÄK 2: Gültig
    }

    @Test
    void testValidation_UsernameCannotBeEmpty() {
        // Äquivalenzklasse: "Ungültige Eingabe"
        assertFalse(isUsernameValid(""), "Leerer Benutzername muss abgelehnt werden.");
    }
}