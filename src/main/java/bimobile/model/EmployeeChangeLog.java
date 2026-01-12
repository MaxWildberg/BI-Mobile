package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity fuer das Mitarbeiter-Aenderungsprotokoll.
 * Speichert neben der Referenz auch Snapshots von ID und Name,
 * damit die Daten auch nach Loeschung des Mitarbeiters lesbar bleiben.
 *
 * @author Jan Lasse Stegmann
 */
@Entity
@Table(name = "employee_change_logs")
public class EmployeeChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Snapshot der ID fuer geloeschte User
    private Long employeeIdSnapshot;

    // Snapshot des Namens fuer geloeschte User
    private String employeeNameSnapshot;

    private LocalDateTime timestamp;

    private String userIdentifier;
    private String action;
    private String details;

    public EmployeeChangeLog() {}

    public EmployeeChangeLog(Employee employee, String userIdentifier, String action, String details) {
        this.employee = employee;
        if (employee != null) {
            this.employeeIdSnapshot = employee.getId();
            // Namen direkt mitspeichern, falls spaeter geloescht wird
            this.employeeNameSnapshot = employee.getLastname() + ", " + employee.getName();
        }
        this.userIdentifier = userIdentifier;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Entfernt die Verknuepfung zum Employee-Objekt (fuer Loeschvorgang).
     * Sichert vorher den Namen, falls noch nicht geschehen.
     */
    public void detachEmployee() {
        if (this.employee != null) {
            this.employeeIdSnapshot = this.employee.getId();

            if (this.employeeNameSnapshot == null) {
                this.employeeNameSnapshot = this.employee.getLastname() + ", " + this.employee.getName();
            }
            this.employee = null;
        }
    }

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public Long getEmployeeIdSnapshot() { return employeeIdSnapshot; }
    public String getEmployeeNameSnapshot() { return employeeNameSnapshot; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUserIdentifier() { return userIdentifier; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}