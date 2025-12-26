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
 * @author Halil Sentürk
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

        updateStatusBasedOnCondition(vehicle);


        Vehicle saved = vehicleRepository.save(vehicle);

        // Anforderung 1: Kauf mit Preis und Start KM
        String info = String.format("Fahrzeug angelegt. Start-KM: %d, Beschaffungspreis: %.2f €",
                saved.getMileage(),
                (saved.getAcquisitionPrice() != null ? saved.getAcquisitionPrice() : 0.0));

        createHistoryEntry(saved, EventType.CREATED, info, null);

        return saved;
    }

    @Override
    public Vehicle updateVehicle(Vehicle vehicle) {
        Vehicle existing = vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug wurde nicht gefunden."));

        // --- ANFORDERUNG 2: Wartung/HU nur bei Änderungen protokollieren ---

        // Check: Hat sich das HU-Datum geändert?
        if (hasDateChanged(existing.getNextInspectionDate(), vehicle.getNextInspectionDate())) {
            createHistoryEntry(existing, EventType.MAINTENANCE,
                    "HU-Termin aktualisiert auf: " + vehicle.getNextInspectionDate(), null);
        }

        // Check: Hat sich das Service-Datum geändert?
        if (hasDateChanged(existing.getNextServiceDate(), vehicle.getNextServiceDate())) {
            createHistoryEntry(existing, EventType.MAINTENANCE,
                    "Service-Termin aktualisiert auf: " + vehicle.getNextServiceDate(), null);
        }

        // Check: Wurde Wartungsmodus aktiviert/deaktiviert?
        if (existing.isMaintenanceActive() != vehicle.isMaintenanceActive()) {
            String status = vehicle.isMaintenanceActive() ? "aktiviert" : "beendet";
            createHistoryEntry(existing, EventType.MAINTENANCE,
                    "Wartungsstatus wurde " + status, null);
        }

        if (vehicle.getId() == null) {
            throw new IllegalArgumentException("Fahrzeug-ID fehlt für die Aktualisierung.");
        }


        // Basisdaten übertragen
        existing.setLicensePlate(vehicle.getLicensePlate());
        existing.setBrand(vehicle.getBrand());
        existing.setModel(vehicle.getModel());
        existing.setPriceCategory(vehicle.getPriceCategory());
        existing.setMileage(vehicle.getMileage());

        // [NEU] Standort-Update
        existing.setFacility(vehicle.getFacility());

        // HU / Inspektion / Wartung
        existing.setNextServiceDate(vehicle.getNextServiceDate());
        existing.setMaintenanceActive(vehicle.isMaintenanceActive());
        existing.setNextInspectionDate(vehicle.getNextInspectionDate());

        // Beschaffungspreis & Antriebsart:
        existing.setAcquisitionPrice(vehicle.getAcquisitionPrice());
        existing.setFuelType(vehicle.getFuelType());

        // Ausstattung:
        existing.setSmokingAllowed(vehicle.isSmokingAllowed());
        existing.setHasNavigationSystem(vehicle.isHasNavigationSystem());
        existing.setHasAirCondition(vehicle.isHasAirCondition());
        existing.setHasWinterTires(vehicle.isHasWinterTires());

        updateStatusBasedOnCondition(existing);


        Vehicle saved = vehicleRepository.save(existing);


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

        String desc = "Status: " + oldStatus + " -> " + newStatus;
        if (reason != null && !reason.isBlank()) desc += " (" + reason + ")";

        createHistoryEntry(saved, EventType.STATUS_CHANGED, desc, null);

        return saved;
    }

    @Override
    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    // [NEU] Implementierung
    @Override
    public List<Vehicle> getVehiclesByFacility(Long facilityId) {
        return vehicleRepository.findByFacilityId(facilityId);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fahrzeug wurde nicht gefunden."));
    }

    // --- ANFORDERUNG 5: Verkauf mit Preis und KM ---
    @Override
    public Vehicle sellVehicle(Long vehicleId, double salePrice, int finalMileage, String buyerName) {
        Vehicle vehicle = findById(vehicleId);

        // Status ändern
        vehicle.setStatus(VehicleStatus.SOLD);
        vehicle.setMileage(finalMileage);

        Vehicle saved = vehicleRepository.save(vehicle);

        String info = "Fahrzeug verkauft an: " + buyerName + ". End-KM: " + finalMileage;
        createHistoryEntry(saved, EventType.SOLD, info, salePrice);

        return saved;
    }

    private void createHistoryEntry(Vehicle v, EventType type, String desc, Double salePrice) {
        VehicleHistoryEntry entry = new VehicleHistoryEntry(v, LocalDate.now(), type, desc);
        if (salePrice != null) entry.setSalePrice(salePrice);
        historyRepository.save(entry);
    }
    private boolean hasDateChanged(LocalDate d1, LocalDate d2) {
        if (d1 == null && d2 == null) return false;
        if (d1 == null || d2 == null) return true;
        return !d1.equals(d2);
    }

    @Override
    public List<VehicleHistoryEntry> getHistoryForVehicle(Long vehicleId) {
        Vehicle vehicle = findById(vehicleId); // Prüft, ob Fahrzeug existiert
        return historyRepository.findByVehicleOrderByDateDesc(vehicle);
    }

    private void updateStatusBasedOnCondition(Vehicle vehicle) {
        LocalDate today = LocalDate.now();

        boolean needsMaintenance = vehicle.isMaintenanceActive() ||
                (vehicle.getNextInspectionDate() != null && vehicle.getNextInspectionDate().isBefore(today)) ||
                (vehicle.getNextServiceDate() != null && vehicle.getNextServiceDate().isBefore(today));

        // Sicherheits-Check: Status NICHT ändern, wenn Auto vermietet/verkauft/ausgemustert ist
        if (vehicle.getStatus() != VehicleStatus.RENTED &&
                vehicle.getStatus() != VehicleStatus.SOLD &&
                vehicle.getStatus() != VehicleStatus.SCRAPPED) {

            if (needsMaintenance) {
                vehicle.setStatus(VehicleStatus.IN_MAINTENANCE);
            } else if (vehicle.getStatus() == VehicleStatus.IN_MAINTENANCE) {
                // Nur freigeben, wenn es vorher "In Wartung" war
                vehicle.setStatus(VehicleStatus.AVAILABLE);
            }
        }
    }


}