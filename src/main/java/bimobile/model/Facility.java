package bimobile.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String mail;

    @Column(name = "telephone_nr", nullable = false)
    private String telephoneNr;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
    private List<Employee> employees = new ArrayList<>();

    public Facility() {}

    public Facility(String address, String mail, String telephoneNr) {
        this.address = address;
        this.mail = mail;
        this.telephoneNr = telephoneNr;
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setFacility(this);
    }

    public Long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephoneNr() {
        return telephoneNr;
    }

    public void setTelephoneNr(String telephoneNr) {
        this.telephoneNr = telephoneNr;
    }
}