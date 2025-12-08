package bimobile.model;

import java.time.*;

import bimobile.model.customer.Customer;
import jakarta.persistence.*;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime invoiceDate;
    private double netAmount;
    private double grossAmount;
    private double taxAmount;

    private int kilometersBefore;
    private int kilometersAfter;

    private String pdfLink;
    private boolean sent;

    @OneToOne
    private Rental rental;

    @ManyToOne
    private Facility facility;

    @OneToOne
    private Vehicle vehicle;

    @ManyToOne
    private Customer customer;

    public Invoice() {
        this.invoiceDate = LocalDateTime.now();
        this.sent = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public double getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(double grossAmount) {
        this.grossAmount = grossAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
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

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

}
