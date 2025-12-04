package bimobile.dao;

import bimobile.model.Vehicle;
import bimobile.model.VehicleHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository für Fahrzeug-Lebenslaufeinträge.
 */
public interface VehicleHistoryRepository extends JpaRepository<VehicleHistoryEntry, Long> {

    List<VehicleHistoryEntry> findByVehicleOrderByDateDesc(Vehicle vehicle);
}