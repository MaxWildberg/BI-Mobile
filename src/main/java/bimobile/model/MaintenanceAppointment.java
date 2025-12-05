package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Wartungstermin für ein Fahrzeug (z.B. HU, Inspektion, Werkstatt).
 * Diese Informationen werden genutzt, um Entscheidungen wie
 * "Fahrzeug darf nicht verfügbar gesetzt werden, weil HU fällig" zu treffen.
 */
@Entity
public class MaintenanceAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datum der Wartung bzw. fälligen HU
    private LocalDate date;

    // Art der Wartung, z.B. "HU", "Inspektion", "Ölwechsel"
    private String type;

    // Optionaler Kommentar
    private String note;

    // Viele Wartungstermine gehören zu genau einem Fahrzeug
    @ManyToOne(optional = false)
    private Vehicle vehicle;

    public MaintenanceAppointment() {
    }

    public MaintenanceAppointment(Vehicle vehicle, LocalDate date, String type, String note) {
        this.vehicle = vehicle;
        this.date = date;
        this.type = type;
        this.note = note;
    }

    // --- Getter & Setter ---

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}