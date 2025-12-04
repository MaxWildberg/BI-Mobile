package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity für ein Fahrzeug.
 */
@Entity
@Table(
        name = "vehicle",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_license_plate", columnNames = "license_plate")
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Eindeutiges Kennzeichen.
     */
    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    /**
     * Marke (z.B. VW, Mercedes).
     */
    @Column(nullable = false)
    private String brand;

    /**
     * Modellbezeichnung (z.B. Golf, A-Klasse).
     */
    @Column(nullable = false)
    private String model;

    /**
     * Preisklasse (z.B. A, B, C).
     */
    private String priceClass;

    /**
     * Kilometerstand.
     */
    private int mileage;

    /**
     * Aktueller Status des Fahrzeugs.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    /**
     * Datum der nächsten HU/Inspektion.
     * Wird für die Regel "Status Verfügbar nur, wenn HU nicht fällig" verwendet.
     */
    private LocalDate nextInspectionDate;

	/**
	 * Datum des nächsten Service-Termins.
	 */
	private LocalDate nextServiceDate;
    /**
     * True, wenn aktuell eine Wartung läuft/geplant ist.
     */
    private boolean maintenanceActive;

	/**
	 * Tagespreis, der für Mietberechnungen genutzt wird.
	 */
	private double dailyRate;

	/**
	 * Flag, ob das Fahrzeug grundsätzlich verfügbar ist.
	 */
	private boolean available = true;

    /**
     * Historie-Einträge (Lebenslauf).
     */
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleHistoryEntry> history = new ArrayList<>();

    protected Vehicle() {
        // für JPA
    }

    public Vehicle(String licensePlate, String brand, String model, String priceClass) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.priceClass = priceClass;
        this.status = VehicleStatus.AVAILABLE;
    }

    /**
     * Prüft, ob HU/Inspektion fällig oder überfällig ist.
     */
    public boolean isInspectionOverdue() {
	    return nextInspectionDate != null && !nextInspectionDate.isAfter(LocalDate.now());
    }

    /**
     * Eintrag zum Lebenslauf hinzufügen.
     */
    public void addHistoryEntry(VehicleHistoryEntry entry) {
        history.add(entry);
        entry.setVehicle(this);
    }

    // Getter/Setter

    public Long getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPriceClass() {
        return priceClass;
    }

    public void setPriceClass(String priceClass) {
        this.priceClass = priceClass;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

	public LocalDate getNextInspectionDate() {
		return nextInspectionDate;
	}

	public void setNextInspectionDate(LocalDate nextInspectionDate) {
		this.nextInspectionDate = nextInspectionDate;
	}

	public LocalDate getNextServiceDate() {
		return nextServiceDate;
	}
	public double getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(double dailyRate) {
		this.dailyRate = dailyRate;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public void setNextServiceDate(LocalDate nextServiceDate) {
		this.nextServiceDate = nextServiceDate;
	}
    public boolean isMaintenanceActive() {
        return maintenanceActive;
    }

    public void setMaintenanceActive(boolean maintenanceActive) {
        this.maintenanceActive = maintenanceActive;
    }

    public List<VehicleHistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<VehicleHistoryEntry> history) {
        this.history = history;
    }
}