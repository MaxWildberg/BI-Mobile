package bimobile.service;

import bimobile.dao.VehicleRepository;
import bimobile.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Fachlogik der Fahrzeugverwaltung.
 * Hier werden die Regeln aus der Fallstudie umgesetzt:
 *  - Fahrzeug anlegen / bearbeiten
 *  - Status ändern mit Prüfungen
 *  - Wartungstermine planen
 *  - Lebenslauf-Einträge erzeugen
 */
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Legt ein neues Fahrzeug an.
     * Prüft u.a. die Eindeutigkeit des Kennzeichens
     * und erzeugt einen ersten Lebenslauf-Eintrag.
     */
    public Vehicle createVehicle(Vehicle vehicle) {
        vehicleRepository.findByLicensePlate(vehicle.getLicensePlate())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("License plate already exists: " + existing.getLicensePlate());
                });

        // Lebenslauf: "Fahrzeug angelegt"
        VehicleHistoryEntry entry = new VehicleHistoryEntry(
                vehicle,
                "Vehicle created with license plate " + vehicle.getLicensePlate()
        );
        vehicle.addHistoryEntry(entry);

        return vehicleRepository.save(vehicle);
    }

    /**
     * Aktualisiert basisdaten eines Fahrzeugs (z.B. Marke, Modell, Preisklasse, km-Stand)
     */
    public Vehicle updateVehicle(Vehicle vehicle) {
        Vehicle existing = vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        existing.setBrand(vehicle.getBrand());
        existing.setModel(vehicle.getModel());
        existing.setPriceClass(vehicle.getPriceClass());
        existing.setMileage(vehicle.getMileage());

        existing.addHistoryEntry(
                new VehicleHistoryEntry(existing, "Vehicle data updated")
        );

        return vehicleRepository.save(existing);
    }

    /**
     * Ändert den Status eines Fahrzeugs unter Berücksichtigung der Regeln
     * aus der Fallstudie (z.B. Ausleihe nur wenn verfügbar, nicht freigeben
     * wenn HU/Wartung fällig).
     */
    public Vehicle changeStatus(Long vehicleId, VehicleStatus newStatus) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Beispiel-Regel: Ausleihe nur, wenn aktuell AVAILABLE
        if (newStatus == VehicleStatus.RENTED && vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle is not available for renting.");
        }

        // Beispiel-Regel: Zurück auf AVAILABLE nur, wenn keine akute Wartung/HU fällig
        if (newStatus == VehicleStatus.AVAILABLE && hasDueMaintenance(vehicle)) {
            throw new IllegalStateException("Vehicle cannot be set to AVAILABLE because maintenance is due.");
        }

        VehicleStatus oldStatus = vehicle.getStatus();
        vehicle.setStatus(newStatus);

        vehicle.addHistoryEntry(
                new VehicleHistoryEntry(
                        vehicle,
                        "Status changed from " + oldStatus + " to " + newStatus
                )
        );

        return vehicleRepository.save(vehicle);
    }

    /**
     * Plant eine Wartung (z.B. HU, Inspektion) für ein Fahrzeug
     * und erzeugt einen Lebenslauf-Eintrag.
     */
    public Vehicle planMaintenance(Long vehicleId, LocalDate date, String type, String note) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        MaintenanceAppointment appointment = new MaintenanceAppointment(vehicle, date, type, note);
        vehicle.addMaintenanceAppointment(appointment);

        vehicle.addHistoryEntry(
                new VehicleHistoryEntry(
                        vehicle,
                        "Maintenance planned: " + type + " on " + date
                )
        );

        return vehicleRepository.save(vehicle);
    }

    /**
     * Liefert alle Fahrzeuge (z.B. für die Übersicht im GUI).
     */
    @Transactional(readOnly = true)
    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Einfache Suche nach Kennzeichen (Teilstring).
     */
    @Transactional(readOnly = true)
    public List<Vehicle> searchByLicensePlate(String licensePlatePart) {
        return vehicleRepository.findByLicensePlateContainingIgnoreCase(licensePlatePart);
    }

    // --- Hilfsmethode nur intern verwendet ---

    /**
     * Prüft, ob für das Fahrzeug eine Wartung / HU fällig ist.
     * Vereinfachte Logik: wenn ein Termin in der Vergangenheit liegt.
     */
    private boolean hasDueMaintenance(Vehicle vehicle) {
        LocalDate today = LocalDate.now();
        return vehicle.getMaintenanceAppointments().stream()
                .anyMatch(appt -> appt.getDate().isBefore(today));
    }
}