package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity für Eintrag im Fahrzeuglebenslauf.
 * @author Halil Sentürk
 */

@Entity
@Table(name = "vehicle_history_entry")
public class VehicleHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    /**
     * Datum des Ereignisses.
     */
    private LocalDate date;

    /**
     * Art des Ereignisses.
     */
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    /**
     * Beschreibungstext, der im Lebenslauf angezeigt werden kann.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Verkaufspreis bei Verkauf.
     */
    private Double salePrice;

    protected VehicleHistoryEntry() {
        // für JPA
    }

    public VehicleHistoryEntry(Vehicle vehicle, LocalDate date, EventType eventType, String description) {
        this.vehicle = vehicle;
        this.date = date;
        this.eventType = eventType;
        this.description = description;
    }

    // Getter & Setter

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }
}