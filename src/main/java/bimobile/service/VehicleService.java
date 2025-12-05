package bimobile.service;

import bimobile.dao.VehicleRepository;
import bimobile.model.Customer;
import bimobile.model.Vehicle;
import bimobile.model.VehicleStatus;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Service-Schnittstelle für die Fahrzeugverwaltung.
 */
public interface VehicleService {

    Vehicle createVehicle(Vehicle vehicle);

    Vehicle updateVehicle(Vehicle vehicle);

    Vehicle changeStatus(Long vehicleId, VehicleStatus newStatus, String reason);

    List <Vehicle> findAllVehicles();

    Vehicle save (Vehicle vehicle);

    Vehicle findById(Long id);
}