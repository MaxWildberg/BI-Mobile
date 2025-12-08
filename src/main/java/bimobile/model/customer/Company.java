package bimobile.model.customer;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "company")
    private List<BusinessCustomer> employees = new ArrayList<>();

    public Company() {}

    public Company(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public List<BusinessCustomer> getEmployees() {
        return employees;
    }
}
