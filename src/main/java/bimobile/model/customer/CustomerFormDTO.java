package bimobile.model.customer;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) für Customer Forms in der UI.
 * Flacht alle Value Objects eines {@link Customer} ab, damit sie direkt
 * mit Vaadin Binder oder anderen Formularen gebunden werden können.
 *
 * Enthält Felder für:
 * Persönliche Daten (Titel, Vorname, Nachname, Geburtstag)
 * Adresse (Straße, PLZ, Stadt, Land)
 * Kontaktinformationen (E-Mail, Telefon)
 * Dokumente (Führerscheinnummer, Ausweisnummer)
 * Firmenzuordnung bei BusinessCustomer
 *
 * Die statische Methode {@link #fromCustomer(Customer)} erlaubt das einfache
 * Erstellen eines DTOs aus einer existierenden Customer-Entity.
 *
 * @author Max Wildberg
 */
public class CustomerFormDTO {

    private String title;
    private String firstname;
    private String lastname;
    private LocalDate birthday;

    private String street;
    private String zip;
    private String city;
    private String country;

    private String email;
    private String telephone;

    private String driversLicense;
    private String idCardNum;

    private Company company;

    public CustomerFormDTO() {}

    /**
     * Erstellt ein Customer DataTransferObject aus einer bestehenden Customer-Entity.
     * Alle relevanten Felder werden kopiert, inklusive Company bei BusinessCustomer.
     *
     * @param customer Customer-Entity
     * @return DTO mit den Werten des Kunden
     */
    public static CustomerFormDTO fromCustomer(bimobile.model.customer.Customer customer) {
        CustomerFormDTO dto = new CustomerFormDTO();

        // PersonalData
        if (customer.getPersonalData() != null) {
            dto.setTitle(customer.getPersonalData().getTitle());
            dto.setFirstname(customer.getPersonalData().getFirstname());
            dto.setLastname(customer.getPersonalData().getLastname());
            dto.setBirthday(customer.getPersonalData().getBirthday());
        }

        // Address
        if (customer.getAddress() != null) {
            dto.setStreet(customer.getAddress().getStreet());
            dto.setZip(customer.getAddress().getZip());
            dto.setCity(customer.getAddress().getCity());
            dto.setCountry(customer.getAddress().getCountry());
        }

        // ContactInfo
        if (customer.getContactInfo() != null) {
            dto.setEmail(customer.getContactInfo().getMail());
            dto.setTelephone(customer.getContactInfo().getTelephone());
        }

        // Documents
        if (customer.getIdentification() != null) {
            dto.setDriversLicense(customer.getIdentification().getDriverslicense());
            dto.setIdCardNum(customer.getIdentification().getIdcard());
        }

        // BusinessCustomer
        if (customer instanceof bimobile.model.customer.BusinessCustomer bc && bc.getCompany() != null) {
            dto.setCompany(bc.getCompany());
        }

        return dto;
    }

    // Getter und Setter

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getDriversLicense() { return driversLicense; }
    public void setDriversLicense(String driversLicense) { this.driversLicense = driversLicense; }

    public String getIdCardNum() { return idCardNum; }
    public void setIdCardNum(String idCardNum) { this.idCardNum = idCardNum; }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getCompanyId() {
        return getCompany().getCompanyId();
    }
}

