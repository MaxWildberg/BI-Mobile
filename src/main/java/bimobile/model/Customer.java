package bimobile.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;


/**
 * Entity Klasse, welche einen Kunden mit grundsätzlichen Attributen darstellt.
 *
 * Grundklasse für weitere Kundenklassen, die darauf aufbauen.
 *
 * Ein Kunde hat eine Verknüpfung zu Ausleihe (Rental)
 * Jedes Objekt kennt alle seine Ausleihen.
 *
 *
 * @author Max Wildberg
 */
@Entity
@Table(name = "customers")
public class Customer implements CustomerInterface {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto ID
    private Long customerId;

    @Column(nullable = false)
    private String salutation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String zip;

    @Column(nullable = false)
    private String residence;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String driverslicenseID;

    @Column(nullable = false)
    private String idCardNumber;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Rental> rents = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Invoice> invoices = new ArrayList<>();

    /**
     * Parameterloser Standardkonstruktor für JPA.
     */
    public Customer(){

    }

    /**
     * Konstruktor zum Anlegen eines neuen Kunden
     * @param salutation Anrede
     * @param name Name
     * @param lastname Nachname
     * @param birthday Geburtstag
     * @param address Adresse
     * @param zip Postleitzahl
     * @param residence Wohnort
     * @param email E-Mail Adresse
     * @param telephone Telefonnummer
     * @param driverslicenseID Führerscheinnummer
     * @param idCardNumber Ausweisnummer
     */

    public Customer(String salutation, String name, String lastname, LocalDate birthday, String address, String zip, String residence, String country, String email, String telephone, String driverslicenseID, String idCardNumber) {
        this.salutation = salutation;
        this.name = name;
        this.lastname = lastname;
        this.birthday = birthday;
        this.address = address;
        this.zip = zip;
        this.residence = residence;
        this.country = country;
        this.email = email;
        this.telephone = telephone;
        this.driverslicenseID = driverslicenseID;
        this.idCardNumber = idCardNumber;
    }

    @Override
    public Long getCustomerId() {
        return customerId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    @Override
    public LocalDate getBirthday() {
        return birthday;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getZip() {
        return zip;
    }

    @Override
    public String getResidence() {
        return residence;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getTelephone() {
        return telephone;
    }

    @Override
    public String getDriversLicenseID() {
        return driverslicenseID;
    }

    @Override
    public String getIdCardNumber() {
        return idCardNumber;
    }

    @Override
    public int getAge() {
        return Period.between(getBirthday(), LocalDate.now()).getYears();
    }

    @Override
    public List<Rental> getRents() {
        return rents;
    }

    @Override
    public String getFullName(){
        return getName() + " " + getLastname();
    }

    @Override
    public List<Invoice> getInvoices() {
        return invoices;
    }

    @Override
    public void addRent(Rental rental) {
        this.rents.add(rental);
    }

    @Override
    public String getRentCount() {
        return String.valueOf(rents.size());
    }

    @Override
    public double getTotalRevenue() {
        double totalRevenue = 0;
        for (Rental rent : rents){
            totalRevenue += rent.getTotalPrice();
        }
        return totalRevenue;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public void setResidence(String residence) {
        this.residence = residence;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public void setDriverslicenseID(String driverslicenseID) {
        this.driverslicenseID = driverslicenseID;
    }

    @Override
    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    @Override
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    @Override
    public String getSalutation() {
        return salutation;
    }

    @Override
    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }
}
