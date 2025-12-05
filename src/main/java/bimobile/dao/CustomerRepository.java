package bimobile.dao;

import bimobile.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository-Interface für Customer.
 * Spring Data JPA generiert automatisch die Implementierung.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Prüft, ob ein Kunde mit dieser E-Mail existiert
    boolean existsByEmail(String email);

    // Findet einen Kunden nach E-Mail
    Optional<Customer> findByEmail(String email);

    // Optional: Nach Name und Nachname suchen
    Optional<Customer> findByNameAndLastname(String name, String lastname);
}
