package bimobile.repository;

import bimobile.model.Rental;
import bimobile.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository für Ausleihen (Rental).
 * Diese Schnittstelle stellt CRUD-Funktionen bereit und
 * ergänzt suchspezifische Methoden.
 *
 * Erstellt im Projekt BI-Mobile von Ben Berlin.
 */
@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    /**
     * Überprüft, ob eine Ausleihe mit einem Fahrzeug kollidiert.
     */
    List<Rental> findByVehicleAndEndDateAfterAndStartDateBefore(
            Vehicle vehicle,
            LocalDate start,
            LocalDate end
    );
}
