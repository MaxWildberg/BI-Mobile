package bimobile.service;

import bimobile.dao.VehicleHistoryRepository;
import bimobile.dao.VehicleRepository;
import bimobile.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementierung der Geschäftslogik für die Fahrzeugverwaltung.
 */
@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleHistoryRepository historyRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository,
                              VehicleHistoryRepository historyRepository) {
        this.vehicleRepository = vehicleRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        // Eindeutigkeit prüfen
        if (vehicleRepository.existsByLicensePlateIgnoreCase(vehicle.getLicensePlate())) {
            throw new IllegalArgumentException("Ein Fahrzeug mit diesem Kennzeichen existiert bereits.");
        }

        if (vehicle.getStatus() == null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }

        Vehicle saved = vehicleRepository.save(vehicle);

        // Lebenslauf: Fahrzeug angelegt
        VehicleHistoryEntry entry = new VehicleHistoryEntry(
                saved,
                LocalDate.now(),
                EventType.CREATED,
                "Fahrzeug angelegt (Kennzeichen: " + saved.getLicensePlate() + ")"
        );
        historyRepository.save(entry);

        return saved;
    }

    @Override
    public Vehicle updateVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            throw new IllegalArgumentException("Fahrzeug-ID fehlt für die Aktualisierung.");
        }

        Vehicle existing = vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug wurde nicht gefunden."));

        // Basisdaten übertragen
        existing.setLicensePlate(vehicle.getLicensePlate());
        existing.setBrand(vehicle.getBrand());
        existing.setModel(vehicle.getModel());
        existing.setPriceClass(vehicle.getPriceClass());
        existing.setMileage(vehicle.getMileage());
        existing.setNextInspectionDate(vehicle.getNextInspectionDate());
		existing.setNextServiceDate(vehicle.getNextServiceDate());
        existing.setMaintenanceActive(vehicle.isMaintenanceActive());

        Vehicle saved = vehicleRepository.save(existing);

        // Optionaler Lebenslauf-Eintrag
        VehicleHistoryEntry entry = new VehicleHistoryEntry(
                saved,
                LocalDate.now(),
                EventType.UPDATED,
                "Fahrzeugdaten aktualisiert."
        );
        historyRepository.save(entry);

        return saved;
    }

    @Override
    public Vehicle changeStatus(Long vehicleId, VehicleStatus newStatus, String reason) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug wurde nicht gefunden."));

        VehicleStatus oldStatus = vehicle.getStatus();

        // Regeln: Endzustand
        if (oldStatus == VehicleStatus.SCRAPPED || oldStatus == VehicleStatus.SOLD) {
            throw new IllegalStateException("Der Status eines ausgemusterten oder verkauften Fahrzeugs kann nicht mehr geändert werden.");
        }

        // Regel: Verfügbar und verleihbar nur, wenn keine HU fällig und keine Wartung aktiv
        if (newStatus == VehicleStatus.AVAILABLE || newStatus == VehicleStatus.RENTED) {
            if (vehicle.isInspectionOverdue()) {
                throw new IllegalStateException("Fahrzeug kann nicht auf '" + newStatus.getDisplayName() + "' gesetzt werden, da die HU fällig ist.");
            }
            if (vehicle.isMaintenanceActive()) {
                throw new IllegalStateException("Fahrzeug kann nicht auf '" + newStatus.getDisplayName() + "' gesetzt werden, da eine Wartung aktiv ist.");
            }
        }

        vehicle.setStatus(newStatus);
        Vehicle saved = vehicleRepository.save(vehicle);

        // Lebenslauf-Eintrag
        String description = "Status geändert von " +
                (oldStatus != null ? oldStatus.getDisplayName() : "unbekannt") +
                " zu " + newStatus.getDisplayName();

        if (reason != null && !reason.isBlank()) {
            description += " (Begründung: " + reason + ")";
        }

        VehicleHistoryEntry entry = new VehicleHistoryEntry(
                saved,
                LocalDate.now(),
                EventType.STATUS_CHANGED,
                description
        );
        historyRepository.save(entry);

        return saved;
    }

    @Override
    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug wurde nicht gefunden."));
    }
	@Override
	public Vehicle save(Vehicle vehicle) {
		return vehicleRepository.save(vehicle);
	}
}
