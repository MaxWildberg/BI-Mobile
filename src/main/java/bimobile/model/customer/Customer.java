package bimobile.model.customer;

import bimobile.model.Invoice;
import bimobile.model.Rental;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstrakte Basisklasse für Kunden.
 *
 * Repräsentiert einen Kunden in der Anwendung, entweder Privat- oder Business-Kunde.
 * Enthält persönliche Daten, Adresse, Kontaktinformationen und Identifikationsdaten.
 * Verknüpft außerdem alle Mieten ({@link Rental}) und Rechnungen ({@link Invoice}) des Kunden.
 * Bettet Value-Objekte ein.
 *
 * Helfermethoden liefern Altersangabe, vollständigen Namen, Anzahl Mieten und Gesamterlöse.
 *
 * @author Max Wildberg
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "customer_type")
public abstract class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Embedded
    @Valid
    @NotNull
    private PersonalData personalData;

    @Embedded
    @Valid
    @NotNull
    private Address address;

    @Embedded
    @Valid
    @NotNull
    private ContactInfo contactInfo;

    @Embedded
    @Valid
    @NotNull
    private Identification identification;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Rental> rents = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Invoice> invoices = new ArrayList<>();

    protected Customer() {}

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


    // Getter und Setter

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

    public List<Rental> getRents(){
        return rents;
    }
    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
    }


    // Helfermethoden

    /**
     * Liefert das Alter des Kunden in Jahren basierend auf dem Geburtsdatum.
     * @return Alter des Kunden
     */
    @Transient
    public int getAge() {
        return Period.between(personalData.getBirthday(), LocalDate.now()).getYears();
    }

    /**
     * Liefert den vollständigen Namen des Kunden (Vorname + Nachname).
     * @return vollständiger Name
     */
    @Transient
    public String getFullName() {
        return personalData.getFirstname() + " " + personalData.getLastname();
    }

    /**
     * Liefert die Anzahl der Mieten des Kunden.
     * @return Anzahl Mieten
     */
    @Transient
    public int getRentCount() {
        return rents.size();
    }

    /**
     * Berechnet den Gesamtumsatz aus allen Mieten des Kunden.
     * @return Gesamterlös in Euro
     */
    @Transient
    public double getTotalRevenue() {
        return rents.stream()
                .mapToDouble(Rental::calculateTotalPrice)
                .sum();
    }
}
