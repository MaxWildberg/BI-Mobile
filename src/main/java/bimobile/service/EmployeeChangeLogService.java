package bimobile.service;

import bimobile.dao.EmployeeChangeLogRepository;
import bimobile.model.Employee;
import bimobile.model.EmployeeChangeLog;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service zur Verwaltung des Änderungsprotokolls für Mitarbeiter.
 *
 * @author Jan Lasse Stegmann
 */
@Service
public class EmployeeChangeLogService {

    private final EmployeeChangeLogRepository repository;

    public EmployeeChangeLogService(EmployeeChangeLogRepository repository) {
        this.repository = repository;
    }

    // Erstellt und speichert einen neuen Log-Eintrag
    public void logChange(Employee employee, String userIdentifier, String action, String details) {
        EmployeeChangeLog log = new EmployeeChangeLog(employee, userIdentifier, action, details);
        repository.save(log);
    }

    public List<EmployeeChangeLog> getAllEntries() {
        return repository.findAllByOrderByTimestampDesc();
    }

    /**
     * Löst die Verbindung zum Mitarbeiter-Objekt in den Logs.
     * Dies ist notwendig, damit der Mitarbeiter gelöscht werden kann,
     * ohne dass Foreign-Key-Constraints verletzt werden oder die Historie verschwindet.
     */
    @Transactional
    public void detachEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) return;
        List<EmployeeChangeLog> logs = repository.findByEmployee(employee);
        for (EmployeeChangeLog log : logs) {
            log.detachEmployee();
        }
        repository.saveAll(logs);
    }
}