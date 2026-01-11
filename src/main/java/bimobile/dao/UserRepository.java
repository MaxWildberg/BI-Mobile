package bimobile.dao;

import bimobile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Zugriff auf die Benutzer in der Datenbank.
 * Wird für Login und Registrierung verwendet.
 *
 * @author Jannick Braun
 */


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}