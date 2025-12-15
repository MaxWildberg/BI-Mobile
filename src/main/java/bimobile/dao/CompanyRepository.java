package bimobile.dao;

import bimobile.model.customer.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository für {@link Company}-Entities.
 * Bietet CRUD-Funktionalitäten über {@link JpaRepository} hinaus:
 * Prüfen, ob ein Unternehmen mit einem bestimmten Namen bereits existiert
 *
 * @author Max Wildberg
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Prüft, ob eine Firma mit dem gegebenen Namen existiert.
     *
     * @param name Name des Unternehmens
     * @return true, falls mindestens eine Firma mit diesem Namen existiert, sonst false
     */
    boolean existsByName(String name);
}
