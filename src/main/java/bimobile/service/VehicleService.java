package bimobile.service;

import bimobile.model.Vehicle;
import bimobile.model.VehicleHistoryEntry;
import bimobile.model.VehicleStatus;

import java.util.List;

/**
 * Service-Schnittstelle für die Fahrzeugverwaltung.
 * @author Halil Sentürk
 */

public interface VehicleService {

	Vehicle createVehicle(Vehicle vehicle);

	Vehicle updateVehicle(Vehicle vehicle);

	Vehicle changeStatus(Long vehicleId, VehicleStatus newStatus, String reason);

	List <Vehicle> findAllVehicles();

	Vehicle save (Vehicle vehicle);

	Vehicle findById(Long id);

	Vehicle sellVehicle(Long vehicleId, double salePrice, int finalMileage, String buyerName);

	List<VehicleHistoryEntry> getHistoryForVehicle(Long vehicleId);
}