package bimobile.model.customer;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Description: Represents a business customer, extending the base
 * Customer entity with additional fields such as company name and address attributes.
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