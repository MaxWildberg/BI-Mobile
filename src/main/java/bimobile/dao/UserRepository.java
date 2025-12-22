package bimobile.dao;

import bimobile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring-Data-JPA-Repository für Benutzer inkl. Suche per E-Mail und Prüfung, ob eine E-Mail bereits existiert.
 *
 * @author Jannick Braun
 */


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}