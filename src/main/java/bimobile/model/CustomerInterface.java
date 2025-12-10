package bimobile.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Beschreibung:
 * Interface, welches von allen Kunden-Klassen implementiert wird.
 * Verwendung überall wo auf ein Kunden-Objekt zugegriffen werden soll
 * -> Ermöglicht einfaches Hinzufügen von weiteren Kunden-Arten
 *
 * @author Max Wildberg
 */

public interface CustomerInterface {
    Long getCustomerId();

    String getFirstName();

    String getLastName();

    LocalDate getBirthday();

    String getAddress();

    String getZip();

    String getResidence();

    String getCountry();

    String getEmail();

    String getTelephone();

    String getDriversLicenseID();

    String getIdCardNumber();

    int getAge();

    List<Rental> getRents();

    void addRent(Rental rental);

    String getRentCount();

    double getTotalRevenue();

    String getFullName();

    List<Invoice> getInvoices();

    String getSalutation();

    void setFirstName(String name);

    void setLastName(String lastname);

    void setAddress(String address);

    void setZip(String zip);

    void setResidence(String residence);

    void setCountry(String country);

    void setEmail(String email);

    void setTelephone(String telephone);

    void setDriverslicenseID(String driverslicenseID);

    void setIdCardNumber(String idCardNumber);

    void setBirthday(LocalDate birthday);

    void setSalutation(String salutation);

}