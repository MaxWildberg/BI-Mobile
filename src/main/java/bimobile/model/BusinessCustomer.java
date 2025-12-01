package bimobile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "business_customer") // optional: use underscore naming for DB
public class BusinessCustomer extends Customer implements CustomerInterface {

    // Zusätzlich Felder für Business Customer
    @Column(nullable = true) // allow null for normal Customer rows
    private String company;

    @Column(nullable = true) // allow null
    private String companyAddress;

    // Default constructor for JPA
    public BusinessCustomer() {}

    // Optional convenience constructor
    public BusinessCustomer(String name,
                            String lastname,
                            LocalDate birthday,
                            String address,
                            String zip,
                            String residence,
                            String country,
                            String email,
                            String telephone,
                            String driverslicenseID,
                            String idCardNumber,
                            String company,
                            String companyAddress) {
        super(name, lastname, birthday, address, zip, residence, country, email, telephone, driverslicenseID, idCardNumber);

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
