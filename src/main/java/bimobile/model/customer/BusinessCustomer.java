package bimobile.model.customer;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Repräsentiert einen Firmen-Kunden.
 * Erbt von {@link Customer} und speichert zusätzlich zu den typischen Kundendaten noch die Firma.
 * Wird in der Datenbank mit Wert "BUSINESS" gespeichert.
 *
 * @author Max Wildberg
 */
@Entity
@DiscriminatorValue("BUSINESS")
public class BusinessCustomer extends Customer {

    @ManyToOne(optional = true)
    private Company company;

    public BusinessCustomer(){}

    public BusinessCustomer(PersonalData personalData,
                            Address address,
                            ContactInfo contactInfo,
                            Identification identification,
                            Company company) {
        super(personalData, address, contactInfo, identification);
        this.company = company;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}