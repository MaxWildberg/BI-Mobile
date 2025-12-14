package bimobile.model.customer;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Address {

    @NotNull(message = "Straße darf nicht null sein")
    private String street;

    @NotNull(message = "Postleitzahl darf nicht null sein")
    private String zip;

    @NotNull(message = "Wohnort darf nicht null sein")
    private String city;

    @NotNull(message = "Land darf nicht null sein")
    private String country;

    public Address(){}

    public Address(String street, String zip, String city, String country) {
        this.street = street;
        this.zip = zip;
        this.city = city;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
}
