package bimobile.service;

import bimobile.dao.RentalChangeLogRepository;
import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalChangeLogService {

    private final RentalChangeLogRepository repository;

    public RentalChangeLogService(RentalChangeLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Schreibt einen neuen Eintrag ins Änderungsprotokoll.
     *
     * Die Rental-ID wird im Konstruktor von RentalChangeLog zusätzlich
     * als Snapshot (rentalIdSnapshot) gespeichert.
     */
    public RentalChangeLog logChange(Rental rental,
                                     String userIdentifier,
                                     String action,
                                     String details) {

        RentalChangeLog entry = new RentalChangeLog(rental, userIdentifier, action, details);
        return repository.save(entry);
    }

    /**
     * Liefert alle Protokolleinträge in absteigender Reihenfolge
     * nach Zeitstempel.
     */
    public List<RentalChangeLog> getAllEntries() {
        return repository.findAllByOrderByTimestampDesc();
    }

    /**
     * Löst die Foreign-Key-Verknüpfung aller Log-Einträge zu der übergebenen Ausleihe.
     *
     * Ablauf:
     *  - Alle Log-Einträge zur gegebenen Rental werden geladen.
     *  - Für jeden Eintrag wird detachRental() aufgerufen:
     *        -> rentalIdSnapshot wird mit der alten ID befüllt (falls noch nicht gesetzt)
     *        -> rental-Referenz wird auf null gesetzt
     *  - Alle geänderten Einträge werden gespeichert.
     *
     * Dadurch kann die Ausleihe gelöscht werden, ohne dass FK-Constraints verletzt werden,
     * und das Protokoll bleibt mit der ehemaligen Rental-ID nachvollziehbar.
     */
    @Transactional
    public void detachRental(Rental rental) {
        if (rental == null || rental.getId() == null) {
            return;
        }

        List<RentalChangeLog> entries = repository.findByRental(rental);
        if (entries.isEmpty()) {
            return;
        }

        for (RentalChangeLog entry : entries) {
            entry.detachRental();
        }

        repository.saveAll(entries);
    }
}
