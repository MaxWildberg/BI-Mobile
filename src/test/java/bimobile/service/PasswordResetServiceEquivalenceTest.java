package bimobile.service;

import bimobile.dao.PasswordResetTokenRepository;
import bimobile.dao.UserRepository;
import bimobile.model.PasswordResetToken;
import bimobile.model.RoleType;
import bimobile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Äquivalenzklassentests für PasswordResetService
 *
 * ============================================
 * ÄQUIVALENZKLASSEN-DEFINITION:
 * ============================================
 *
 * 1. E-Mail Eingabe:
 *    - Gültig (G1): E-Mail existiert in der Datenbank
 *    - Ungültig (U1): E-Mail existiert nicht in der Datenbank
 *    - Ungültig (U2): Leere E-Mail
 *
 * 2. Token:
 *    - Gültig (G2): Token existiert, nicht abgelaufen, nicht benutzt
 *    - Ungültig (U3): Token existiert nicht
 *    - Ungültig (U4): Token bereits benutzt
 *
 * 3. Passwort-Reset:
 *    - Gültig (G3): Gültiger Token + gültiges Passwort
 *    - Ungültig (U5): Ungültiger Token
 *
 * @author Jannick Braun
 */
@SpringBootTest
@Transactional
public class PasswordResetServiceEquivalenceTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "Test",
                "User",
                "test@bimobile.de",
                passwordEncoder.encode("testPasswort123"),
                RoleType.EMPLOYEE
        );
        userRepository.save(testUser);
    }

    // ==================== E-MAIL ÄQUIVALENZKLASSEN ====================

    @Test
    @DisplayName("E-Mail Äquivalenzklasse G1: Gültige E-Mail (existiert in DB)")
    void testEmail_G1_ValidExisting() {
        // Arrange & Act
        boolean result = passwordResetService.initiatePasswordReset("test@bimobile.de", "http://localhost");

        // Assert
        assertTrue(result, "Gültige E-Mail sollte akzeptiert werden");
    }

    @Test
    @DisplayName("E-Mail Äquivalenzklasse U1: Ungültige E-Mail (existiert nicht in DB)")
    void testEmail_U1_InvalidNotExisting() {
        // Arrange & Act
        boolean result = passwordResetService.initiatePasswordReset("gibtsNicht@xyz.de", "http://localhost");

        // Assert - Aus Sicherheitsgründen true (keine Info ob E-Mail existiert)
        assertTrue(result, "Nicht existierende E-Mail sollte keinen Fehler werfen (Sicherheit)");
    }

    @Test
    @DisplayName("E-Mail Äquivalenzklasse U2: Leere E-Mail")
    void testEmail_U2_Empty() {
        // Arrange & Act
        boolean result = passwordResetService.initiatePasswordReset("", "http://localhost");

        // Assert
        assertTrue(result, "Leere E-Mail sollte keinen Fehler werfen (Sicherheit)");
    }

    // ==================== TOKEN ÄQUIVALENZKLASSEN ====================

    @Test
    @DisplayName("Token Äquivalenzklasse G2: Gültiger Token (existiert, nicht abgelaufen, nicht benutzt)")
    void testToken_G2_Valid() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(testUser);
        tokenRepository.save(token);

        // Act
        Optional<PasswordResetToken> result = passwordResetService.validateToken(token.getToken());

        // Assert
        assertTrue(result.isPresent(), "Gültiger Token sollte akzeptiert werden");
        assertTrue(result.get().isValid(), "Token sollte als gültig markiert sein");
    }

    @Test
    @DisplayName("Token Äquivalenzklasse U3: Ungültiger Token (existiert nicht)")
    void testToken_U3_NotExisting() {
        // Arrange & Act
        Optional<PasswordResetToken> result = passwordResetService.validateToken("nichtExistierenderToken");

        // Assert
        assertTrue(result.isEmpty(), "Nicht existierender Token sollte abgelehnt werden");
    }

    @Test
    @DisplayName("Token Äquivalenzklasse U4: Ungültiger Token (bereits benutzt)")
    void testToken_U4_AlreadyUsed() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(testUser);
        token.setUsed(true);
        tokenRepository.save(token);

        // Act
        Optional<PasswordResetToken> result = passwordResetService.validateToken(token.getToken());

        // Assert
        assertTrue(result.isEmpty(), "Bereits benutzter Token sollte abgelehnt werden");
    }

    // ==================== PASSWORT-RESET ÄQUIVALENZKLASSEN ====================

    @Test
    @DisplayName("Passwort-Reset Äquivalenzklasse G3: Gültiger Token + gültiges Passwort")
    void testPasswordReset_G3_ValidTokenAndPassword() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(testUser);
        tokenRepository.save(token);

        // Act
        boolean result = passwordResetService.resetPassword(token.getToken(), "neuesGueltigesPasswort");

        // Assert
        assertTrue(result, "Gültiger Token und Passwort sollten akzeptiert werden");

        // Verifizieren dass Passwort geändert wurde
        User updatedUser = userRepository.findByEmail("test@bimobile.de").orElseThrow();
        assertTrue(passwordEncoder.matches("neuesGueltigesPasswort", updatedUser.getPassword()));
    }

    @Test
    @DisplayName("Passwort-Reset Äquivalenzklasse U5: Ungültiger Token")
    void testPasswordReset_U5_InvalidToken() {
        // Arrange & Act
        boolean result = passwordResetService.resetPassword("ungueltigerToken", "neuesPasswort123");

        // Assert
        assertFalse(result, "Reset mit ungültigem Token sollte fehlschlagen");
    }

    // ==================== GRENZWERT-TESTS (Ergänzung) ====================

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "12345678", "123456789"})
    @DisplayName("Grenzwert-Test: Passwortlänge um Grenzwert 8")
    void testPasswordLength_BoundaryValues(String password) {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(testUser);
        tokenRepository.save(token);

        // Act
        boolean result = passwordResetService.resetPassword(token.getToken(), password);

        // Assert - Service akzeptiert alle, Validierung ist in der View
        assertTrue(result, "Passwort '" + password + "' sollte vom Service verarbeitet werden");
    }
}
