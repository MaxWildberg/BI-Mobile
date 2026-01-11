package bimobile.dao;

import bimobile.model.PasswordResetToken;
import bimobile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Zugriff auf die Reset-Tokens in der Datenbank.
 * Wird beim "Passwort vergessen"-Ablauf verwendet.
 *
 * @author Jannick Braun
 */


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}