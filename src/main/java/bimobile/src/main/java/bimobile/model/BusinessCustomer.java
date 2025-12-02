package bimobile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "business_customer") // optional: use underscore naming for DB
public class BusinessCustomer extends Customer {

    // Zusätzlich Felder für Business Customer
    @Column(nullable = true) // allow null for normal Customer rows
    private String company;

    @Column(nullable = true) // allow null
    private String companyAddress;

    // Default constructor for JPA
    public BusinessCustomer() {}

    // Optional convenience constructor
    public BusinessCustomer(String company, String companyAddress) {
        this.company = company;
        this.companyAddress = companyAddress;
    }

    // Getter/Setter
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }
}