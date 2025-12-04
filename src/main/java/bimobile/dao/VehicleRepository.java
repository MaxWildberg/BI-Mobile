package bimobile.dao;

import bimobile.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository für Fahrzeuge (Datenbankzugriff).
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByLicensePlateIgnoreCase(String licensePlate);

    Optional<Vehicle> findByLicensePlateIgnoreCase(String licensePlate);
}