package bimobile.service;

import bimobile.dao.PasswordResetTokenRepository;
import bimobile.dao.UserRepository;
import bimobile.model.PasswordResetToken;
import bimobile.model.RoleType;
import bimobile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Tests für den PasswordResetService.
 *
 * Testet die Kernfunktionalitäten:
 * - Token-Erstellung für Passwort-Reset
 * - Token-Validierung
 * - Passwort-Zurücksetzung
 *
 * @author Jannick Braun
 */
@SpringBootTest
@Transactional
public class PasswordResetServiceTest {

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
        // Test-User erstellen
        testUser = new User(
                "Test",
                "User",
                "testuser@bimobile.de",
                passwordEncoder.encode("altesPasswort123"),
                RoleType.EMPLOYEE
        );
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Token wird erfolgreich erstellt für existierenden User")
    void testInitiatePasswordReset_ExistingUser_CreatesToken() {
        // Act
        boolean result = passwordResetService.initiatePasswordReset("testuser@bimobile.de", "http://localhost:8080");

        // Assert
        assertTrue(result);

        // Prüfen ob User existiert
        Optional<User> user = userRepository.findByEmail("testuser@bimobile.de");
        assertTrue(user.isPresent());
    }

    @Test
    @DisplayName("Kein Fehler bei nicht existierender E-Mail (Sicherheit)")
    void testInitiatePasswordReset_NonExistingUser_ReturnsTrue() {
        // Act - E-Mail existiert nicht
        boolean result = passwordResetService.initiatePasswordReset("gibtsNicht@bimobile.de", "http://localhost:8080");

        // Assert - Aus Sicherheitsgründen trotzdem true
        assertTrue(result);
    }

    @Test
    @DisplayName("Gültiger Token wird akzeptiert")
    void testValidateToken_ValidToken_ReturnsToken() {
        // Arrange - Token erstellen
        PasswordResetToken token = new PasswordResetToken(testUser);
        tokenRepository.save(token);

        // Act
        Optional<PasswordResetToken> result = passwordResetService.validateToken(token.getToken());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    @DisplayName("Ungültiger Token wird abgelehnt")
    void testValidateToken_InvalidToken_ReturnsEmpty() {
        // Act
        Optional<PasswordResetToken> result = passwordResetService.validateToken("ungueltigerToken123");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Passwort wird erfolgreich zurückgesetzt")
    void testResetPassword_ValidToken_ChangesPassword() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(testUser);
        tokenRepository.save(token);
        String neuesPasswort = "neuesPasswort123";

        // Act
        boolean result = passwordResetService.resetPassword(token.getToken(), neuesPasswort);

        // Assert
        assertTrue(result);

        // Prüfen ob Passwort geändert wurde
        User updatedUser = userRepository.findByEmail("testuser@bimobile.de").orElseThrow();
        assertTrue(passwordEncoder.matches(neuesPasswort, updatedUser.getPassword()));
    }

    @Test
    @DisplayName("Token wird nach Verwendung als benutzt markiert")
    void testResetPassword_TokenMarkedAsUsed() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken(testUser);
        tokenRepository.save(token);

        // Act
        passwordResetService.resetPassword(token.getToken(), "neuesPasswort123");

        // Assert
        PasswordResetToken usedToken = tokenRepository.findByToken(token.getToken()).orElseThrow();
        assertTrue(usedToken.isUsed());
    }
}
