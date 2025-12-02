package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Lending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private double dailyRate;
    private int kilometersBefore;
    private int kilometersAfter;
    private boolean returned;

    @ManyToOne
    private Vehicle vehicle;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Facility facility;

    // Getter & Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public int getKilometersBefore() {
        return kilometersBefore;
    }

    public void setKilometersBefore(int kilometersBefore) {
        this.kilometersBefore = kilometersBefore;
    }

    public int getKilometersAfter() {
        return kilometersAfter;
    }

    public void setKilometersAfter(int kilometersAfter) {
        this.kilometersAfter = kilometersAfter;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Vehicle getVehicle() { return vehicle; }

    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

}