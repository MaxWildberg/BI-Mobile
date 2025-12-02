package bimobile.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {


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

    public Long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }

    public String getZip() {
        return zip;
    }

    public String getResidence() {
        return residence;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getDriverslicenseID() {
        return driverslicenseID;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public int getAge() {
        return Period.between(getBirthday(), LocalDate.now()).getYears();
    }

    public List<Rental> getRents() {
        return rents;
    }

    public void addRent(Rental rental) {
        this.rents.add(rental);
    }

    public  String getRentCount() {
        return String.valueOf(rents.size());
    }

    public String getTotalRevenue() {
        int totalRevenue = 0;
        for (Rental rent : rents){
            totalRevenue += rent.getTotalPrice();
        }
        return String.valueOf(totalRevenue);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setDriverslicenseID(String driverslicenseID) {
        this.driverslicenseID = driverslicenseID;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getSalutation () {
        return this.name + " " + this.lastname;
    }

}