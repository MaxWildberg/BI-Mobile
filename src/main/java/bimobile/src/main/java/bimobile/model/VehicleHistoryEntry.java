package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Repräsentiert einen einzelnen Eintrag im Fahrzeug-Lebenslauf.
 * Beispiel: Fahrzeug angelegt, Status geändert, Wartung geplant, verkauft usw.
 */
@Entity
public class VehicleHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Zeitstempel, wann der Eintrag erzeugt wurde
    private LocalDateTime timestamp;

    // Kurze Beschreibung, was passiert ist (z.B. "Status geändert von AVAILABLE auf RENTED")
    private String description;

    // Beziehung zum Fahrzeug: Viele Einträge gehören zu genau einem Fahrzeug
    @ManyToOne(optional = false)
    private Vehicle vehicle;

    // --- Konstruktoren ---

    public VehicleHistoryEntry() {
    }

    public VehicleHistoryEntry(Vehicle vehicle, String description) {
        this.vehicle = vehicle;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // --- Getter & Setter ---

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}