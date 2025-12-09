package bimobile.service;

import bimobile.dao.RentalChangeLogRepository;
import bimobile.model.Rental;
import bimobile.model.RentalChangeLog;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentalChangeLogService {

    private final RentalChangeLogRepository repository;

    public RentalChangeLogService(RentalChangeLogRepository repository) {
        this.repository = repository;
    }

    public RentalChangeLog logChange(Rental rental, String userIdentifier, String action, String details) {
        RentalChangeLog entry = new RentalChangeLog(rental, userIdentifier, action, details);
        return repository.save(entry);
    }

    public List<RentalChangeLog> getAllEntries() {
        return repository.findAllByOrderByTimestampDesc();
    }
    /**
     * Entfernt die Foreign-Key-Verknüpfung zu einer gelöschten Ausleihe,
     * damit das Änderungsprotokoll bestehen bleibt und keine Löschfehler auftreten.
     *
     * @param rental Ausleihe, die gelöscht werden soll
     */
    @Transactional
    public void detachRental(Rental rental) {
        Optional.ofNullable(rental)
                .filter(r -> r.getId() != null)
                .ifPresent(existing -> repository.findByRental(existing)
                        .forEach(entry -> {
                            entry.detachRental();
                            repository.save(entry);
                        }));
    }
}
