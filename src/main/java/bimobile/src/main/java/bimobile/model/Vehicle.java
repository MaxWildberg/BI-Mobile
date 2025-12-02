package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert ein Fahrzeug im System.
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

    // Preisklasse A/B/C/D 
    private String priceClass;

    private int mileage;

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    private double dailyRate;

    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    // Ein Fahrzeug kann mehrere Wartungstermine haben
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceAppointment> maintenanceAppointments = new ArrayList<>();

    // Ein Fahrzeug kann viele History-Einträge haben (Lebenslauf)
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleHistoryEntry> historyEntries = new ArrayList<>();

    // --- Konstruktoren ---

    public Vehicle() {
    }

    public Vehicle(String licensePlate, String brand, String model, String priceClass) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.priceClass = priceClass;
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

    public List<VehicleHistoryEntry> getHistoryEntries() {
        return historyEntries;
    }
}