package bimobile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "businessCustomer")
public class BusinessCustomer extends Customer{


    // Zusätzlich Felder für Business Customer oder übergabe eines Company Obj. mit entsprechenden Feldern?
    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String companyAddress;

    public BusinessCustomer(String company) {
        this.company = company;
    }
}
