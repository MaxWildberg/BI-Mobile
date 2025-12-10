package bimobile.model;

import bimobile.enums.RentalStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity-Klasse für eine Fahrzeugausleihe (Rental) im BI-Mobile-System.
 *
 * Eine Ausleihe verknüpft:
 * - einen Kunden (Customer),
 * - ein Fahrzeug (Vehicle),
 * - optional einen Standort (Facility)
 * für einen bestimmten Zeitraum.
 *
 * Zusätzlich werden der Tagespreis, der Gesamtpreis sowie der Status der Ausleihe
 * gespeichert, damit die Geschäftshistorie nachvollziehbar bleibt.
 *
 * @author Ben
 */
@Entity
@Table(name = "rental")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Kunde, der das Fahrzeug ausleiht.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * Fahrzeug, das ausgeliehen wird.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    /**
     * Standort, an dem diese Ausleihe verwaltet wird.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    /**
     * Startdatum der Ausleihe.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Enddatum der Ausleihe.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Gesamtpreis der Ausleihe.
     */
    @Column(nullable = false)
    private double totalPrice;

    /**
     * Aktueller Status der Ausleihe.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalStatus status = RentalStatus.CREATED;

    /**
     * Zeitpunkt der Erstellung des Datensatzes.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Zeitpunkt der letzten Änderung des Datensatzes.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL)
    private Invoice invoice;

    /**
     * Parameterloser Standardkonstruktor für JPA.
     */
    protected Rental() {
        // Nur für JPA / Hibernate
    }

    /**
     * Konstruktor zum Anlegen einer neuen Ausleihe
     * @param customer Kunde, der ausleiht
     * @param vehicle Fahrzeug, das asugeliehen wird
     * @param facility Standort der Ausleihe
     * @param startDate Startdatum
     * @param endDate Enddatum
     * @param totalPrice Gesamtprei
     * @param status Status der Ausleihe
     */
    public Rental(Customer customer, Vehicle vehicle, Facility facility,
                  LocalDate startDate, LocalDate endDate, double totalPrice,
                  RentalStatus status) {

        this.customer = customer;
        this.vehicle = vehicle;
        this.facility = facility;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status != null ? status : RentalStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    //Getter und Setter

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        touch();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        touch();
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
        touch();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        touch();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        touch();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        touch();
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
        touch();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Aktualisierung des Änderungsdatums auf den aktuellen Zeitpunkt des Aufrufs.
     * Wird bei jeder Setter-Änderung aufgerufen.
     */
    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
	public double pullDailyRateFromVehicle() {
		return vehicle.getPriceCategory().getBaseRate();
	}

	public double calculateTotalPrice() {
		long days = startDate.until(endDate).getDays();
		double dailyRate = pullDailyRateFromVehicle();

		return dailyRate * days;
	}
}