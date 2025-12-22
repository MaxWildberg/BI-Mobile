package bimobile.dao;

import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository für {@link Customer}-Entities.
 * Bietet CRUD-Funktionalitäten über {@link JpaRepository} hinaus:
 * - Prüfen, ob eine E-Mail bereits registriert ist.
 * - Kunden inklusive aller zugehörigen Mieten und Fahrzeuge abfragen.
 *
 * @author Max Wildberg
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Prüft, ob ein Kunde mit der gegebenen E-Mail bereits existiert.
     *
     * @param email E-Mail-Adresse des Kunden
     * @return true, falls ein Kunde mit der E-Mail existiert, sonst false
     */
    boolean existsByContactInfo_Email(String email);

    /**
     * Lädt einen Kunden nach ID inklusive aller zugehörigen Mieten und Fahrzeuge.
     * Nützlich, um LazyInitializationExceptions zu vermeiden, wenn Mieten/Fahrzeuge direkt benötigt werden.
     *
     * @param id ID des Kunden
     * @return Optional mit Kunde, falls vorhanden
     */
    @Query("""
        SELECT c FROM Customer c
        LEFT JOIN FETCH c.rents r
        LEFT JOIN FETCH r.vehicle
        WHERE c.customerId = :id
    """)
    Optional findByIdWithRentsAndVehicle(@Param("id") Long id);


}