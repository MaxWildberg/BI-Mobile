package bimobile.model.customer;

import bimobile.model.Invoice;
import bimobile.model.Rental;
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

    @Embedded
    private PersonalData personalData;

    @Embedded
    private Address address;

    @Embedded
    private ContactInfo contactInfo;

    @Embedded
    private Identification identification;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Rental> rents = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Invoice> invoices = new ArrayList<>();


    protected Customer() {
        // JPA requires empty constructor
    }

    public Customer(
            PersonalData personalData,
            Address address,
            ContactInfo contactInfo,
            Identification identification
    ) {
        this.personalData = personalData;
        this.address = address;
        this.contactInfo = contactInfo;
        this.identification = identification;
    }

    // ----------------------
    // Getter & Setter
    // ----------------------

    public Long getCustomerId() {
        return customerId;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }
    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }

    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }
    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Identification getIdentification() {
        return identification;
    }
    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    // ----------------------
    // Helper methods
    // ----------------------

    @Transient
    public int getAge() {
        return Period.between(personalData.getBirthday(), LocalDate.now()).getYears();
    }

    @Transient
    public String getFullName() {
        return personalData.getFirstname() + " " + personalData.getLastname();
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

    public List<Rental> getRents(){
        return rents;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }
}
