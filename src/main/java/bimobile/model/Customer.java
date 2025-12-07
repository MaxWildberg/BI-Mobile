package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "customer_type")
public abstract class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String salutation;

    @Column(nullable = false)
    private String firstname;

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

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Rental> rents = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Invoice> invoices = new ArrayList<>();


    protected Customer() {
        // JPA requires empty constructor
    }

    public Customer(
            String salutation,
            String firstname,
            String lastname,
            LocalDate birthday,
            String address,
            String zip,
            String residence,
            String country,
            String email,
            String telephone,
            String driverslicenseID,
            String idCardNumber
    ) {
        this.salutation = salutation;
        this.firstname = firstname;
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

    // ----------------------
    // Getter & Setter
    // ----------------------

    public Long getCustomerId() {
        return customerId;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDriverslicenseID() {
        return driverslicenseID;
    }

    public void setDriverslicenseID(String driverslicenseID) {
        this.driverslicenseID = driverslicenseID;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public List<Rental> getRents() {
        return rents;
    }

    public void addRent(Rental rental) {
        this.rents.add(rental);
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void addInvoice(Invoice invoice) {
        this.invoices.add(invoice);
    }

    // ----------------------
    // Helper methods
    // ----------------------

    @Transient
    public int getAge() {
        return Period.between(getBirthday(), LocalDate.now()).getYears();
    }

    @Transient
    public String getFullName() {
        return firstname + " " + lastname;
    }

    @Transient
    public int getRentCount() {
        return rents.size();
    }

    @Transient
    public double getTotalRevenue() {
        return rents.stream()
                .mapToDouble(Rental::getTotalPrice)
                .sum();
    }
}
