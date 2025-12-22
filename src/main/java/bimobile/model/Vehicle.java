package bimobile.model;

import bimobile.dao.VehicleRepository;
import bimobile.service.VehicleService;
import bimobile.enums.FuelType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity für ein Fahrzeug.
 * @author Halil Sentürk
 */
@Entity
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Nummernschild (muss eindeutig sein)
	@Column(nullable = false, unique = true)
	private String licensePlate;

	private String brand;
	private String model;

	// Preisklassen
	@Enumerated(EnumType.STRING)
	private PriceCategory priceCategory;

	private int mileage;

	private double dailyRate;

	private LocalDate purchaseDate;

	@Enumerated(EnumType.STRING)
	private VehicleStatus status = VehicleStatus.AVAILABLE;

	private LocalDate nextInspectionDate;

	private LocalDate nextServiceDate;

	private boolean maintenanceActive;

	private Double acquisitionPrice;

	private FuelType fuelType;

	private boolean smokingAllowed;

	private boolean hasNavigationSystem;

	private boolean hasAirCondition;

	private boolean hasWinterTires;

	// Ein Fahrzeug kann mehrere Wartungstermine haben
	@OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MaintenanceAppointment> maintenanceAppointments = new ArrayList<>();

	// Ein Fahrzeug kann viele History-Einträge haben (Lebenslauf)
	@OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<VehicleHistoryEntry> historyEntries = new ArrayList<>();

	// --- Konstruktoren ---

	public Vehicle() {
	}

	public Vehicle(String licensePlate, String brand, String model, PriceCategory priceCategory) {
		this.licensePlate = licensePlate;
		this.brand = brand;
		this.model = model;
		this.priceCategory = priceCategory;
		this.status = VehicleStatus.AVAILABLE;
	}

	// --- Methoden ---

	/**
	 * Aktualisiert den Status des Fahrzeugs.
	 * Die eigentliche Fachlogik (z.B. ob der Statuswechsel erlaubt ist)
	 * wird im Service geprüft, hier wird nur gesetzt.
	 */
	public void setStatus(VehicleStatus status) {
		this.status = status;
	}

	/**
	 * Fügt dem Fahrzeug einen Wartungstermin hinzu.
	 * Die Beziehung wird von beiden Seiten korrekt gesetzt.
	 */
	public void addMaintenanceAppointment(MaintenanceAppointment appointment) {
		appointment.setVehicle(this);
		this.maintenanceAppointments.add(appointment);
	}

	/**
	 * Fügt einen neuen History-Eintrag (Lebenslauf) hinzu.
	 */
	public void addHistoryEntry(VehicleHistoryEntry entry) {
		entry.setVehicle(this);
		this.historyEntries.add(entry);
	}

	// --- Getter & Setter ---

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

	public PriceCategory getPriceCategory() {
		return priceCategory;
	}

	public void setPriceCategory(PriceCategory priceCategory) {
		this.priceCategory = priceCategory;
	}

	public int getMileage() {
		return mileage;
	}

	public void setMileage(int mileage) {
		this.mileage = mileage;
	}

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public VehicleStatus getStatus() {
		return status;
	}

	public List<MaintenanceAppointment> getMaintenanceAppointments() {
		return maintenanceAppointments;
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

	public void setNextServiceDate(LocalDate nextServiceDate) {
		this.nextServiceDate = nextServiceDate;
	}

	public double getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(double dailyRate) {
		this.dailyRate = dailyRate;
	}

	public boolean isInspectionOverdue() {
		return nextInspectionDate != null && !nextInspectionDate.isAfter(LocalDate.now());
	}

	public boolean isAvailable() {
		return status == VehicleStatus.AVAILABLE;
	}

	public boolean isMaintenanceActive() {
		return maintenanceActive;
	}

	public void setMaintenanceActive(boolean maintenanceActive) {
		this.maintenanceActive = maintenanceActive;
	}

	public List<VehicleHistoryEntry> getHistoryEntries() {
		return historyEntries;
	}

	public FuelType getFuelType() {
		return fuelType;
	}

	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	public Double getAcquisitionPrice() {
		return acquisitionPrice;
	}

	public void setAcquisitionPrice(Double acquisitionPrice) {
		this.acquisitionPrice = acquisitionPrice;
	}

	public boolean isSmokingAllowed() {
		return smokingAllowed;
	}

	public void setSmokingAllowed(boolean smokingAllowed) {
		this.smokingAllowed = smokingAllowed;
	}

	public boolean isHasNavigationSystem() {
		return hasNavigationSystem;
	}

	public void setHasNavigationSystem(boolean hasNavigationSystem) {
		this.hasNavigationSystem = hasNavigationSystem;
	}

	public boolean isHasAirCondition() {
		return hasAirCondition;
	}

	public void setHasAirCondition(boolean hasAirCondition) {
		this.hasAirCondition = hasAirCondition;
	}

	public boolean isHasWinterTires() {
		return hasWinterTires;
	}

	public void setHasWinterTires(boolean hasWinterTires) {
		this.hasWinterTires = hasWinterTires;
	}

}
