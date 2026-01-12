package bimobile.dao;

import bimobile.model.Employee;
import bimobile.model.EmployeeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für den Zugriff auf die Mitarbeiter-Historie (Audit Log).
 *
 * Stellt Methoden bereit, um Änderungen entweder für einen spezifischen
 * Mitarbeiter oder als globale, chronologische Liste abzurufen.
 *
 * @author Jan Lasse Stegmann
 */
@Repository
public interface EmployeeChangeLogRepository extends JpaRepository<EmployeeChangeLog, Long> {

    // Filtert die Historie nach einem bestimmten Mitarbeiter
    List<EmployeeChangeLog> findByEmployee(Employee employee);

    // Lädt alle Einträge sortiert nach Aktualität (neueste zuerst)
    List<EmployeeChangeLog> findAllByOrderByTimestampDesc();
}