package bimobile.model;

import bimobile.enums.RentalStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity-Klasse zur Abbildung einer Fahrzeugausleihe im BI-Mobile System.
 *
 * Eine Ausleihe verknüpft:
 *  - einen Kunden
 *  - ein Fahrzeug
 *  - optional einen Standort
 *
 * Zusätzlich werden Preis, Zeitraum und Status gespeichert.
 * Die Klasse wurde im Rahmen der Projektarbeit BI-Mobile
 * von Ben Berlin entwickelt.
 */
@Entity
@Table(name = "rental")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private double dailyRate;

    @Column(nullable = false)
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.CREATED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Rental() {}

    public Rental(Customer customer,
                  Vehicle vehicle,
                  Facility facility,
                  LocalDate startDate,
                  LocalDate endDate,
                  double dailyRate,
                  double totalPrice) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.facility = facility;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyRate = dailyRate;
        this.totalPrice = totalPrice;
        this.status = RentalStatus.CREATED;
    }

    // ---- Getter/Setter ----
    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public Facility getFacility() { return facility; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getDailyRate() { return dailyRate; }
    public double getTotalPrice() { return totalPrice; }
    public RentalStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(RentalStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
