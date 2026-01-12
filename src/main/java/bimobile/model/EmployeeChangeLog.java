package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Speichert Änderungen an Mitarbeiter-Datensätzen.
 * Orientiert sich an der Struktur von RentalChangeLog.
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

    // Falls der Mitarbeiter gelöscht wird, behalten wir die ID hier
    private Long employeeIdSnapshot;

    private LocalDateTime timestamp;

    private String userIdentifier; // Wer hat es gemacht? (E-Mail oder Name)
    private String action;         // Was wurde gemacht? (Erstellt, Update, Status)
    private String details;        // Details (z.B. "Rolle geändert")

    public EmployeeChangeLog() {}

    public EmployeeChangeLog(Employee employee, String userIdentifier, String action, String details) {
        this.employee = employee;
        if (employee != null) {
            this.employeeIdSnapshot = employee.getId();
        }
        this.userIdentifier = userIdentifier;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public void detachEmployee() {
        if (this.employee != null) {
            this.employeeIdSnapshot = this.employee.getId();
            this.employee = null;
        }
    }

    // Getter
    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public Long getEmployeeIdSnapshot() { return employeeIdSnapshot; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUserIdentifier() { return userIdentifier; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}