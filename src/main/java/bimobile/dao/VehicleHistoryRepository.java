package bimobile.dao;

import bimobile.model.Vehicle;
import bimobile.model.VehicleHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für Fahrzeug Lebenslaufeinträge.
 * @author Halil Sentürk
 */
@Repository
public interface VehicleHistoryRepository extends JpaRepository<VehicleHistoryEntry, Long> {

    List<VehicleHistoryEntry> findByVehicleOrderByDateDesc(Vehicle vehicle);
}