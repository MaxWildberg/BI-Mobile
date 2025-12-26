package bimobile.dao;

import bimobile.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Fahrzeuge (Datenbankzugriff).
 * @author Halil Sentürk
 */

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Liefert ein Fahrzeug zu einem bestimmten Kennzeichen (falls vorhanden)
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlateIgnoreCase(String licensePlate);

    // [NEU] Findet alle Fahrzeuge eines bestimmten Standorts
    List<Vehicle> findByFacilityId(Long facilityId);
}