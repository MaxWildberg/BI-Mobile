package bimobile.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;


/**
 * Beschreibung:
 * Entity Klasse, welche einen Kunden mit grundsätzlichen Attributen darstellt.
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

    public Customer(){}

    public Customer(String name, String lastname, LocalDate birthday, String address, String zip, String residence, String country, String email, String telephone, String driverslicenseID, String idCardNumber) {
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
    public String getDriverslicenseID() {
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
    public void addRent(Rental rental) {
        this.rents.add(rental);
    }

    @Override
    public String getRentCount() {
        return String.valueOf(rents.size());
    }

    @Override
    public String getTotalRevenue() {
        int totalRevenue = 0;
        for (Rental rent : rents){
            totalRevenue += rent.getTotalPrice();
        }
        return String.valueOf(totalRevenue);
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
}
