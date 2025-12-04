package bimobile.service;

import bimobile.model.Vehicle;
import bimobile.model.VehicleStatus;

import java.util.List;

/**
 * Service-Schnittstelle für die Fahrzeugverwaltung.
 */
public interface VehicleService {

    Vehicle createVehicle(Vehicle vehicle);

    Vehicle updateVehicle(Vehicle vehicle);

    Vehicle changeStatus(Long vehicleId, VehicleStatus newStatus, String reason);

    List<Vehicle> findAllVehicles();

    Vehicle findById(Long id);
}