package bimobile.model.customer;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class ContactInfo {

    @NotNull(message = "E-Mail darf nicht null sein")
    private String email;

    @NotNull(message = "Telefonnummer darf nicht null sein")
    private String telephone;

    public ContactInfo(){}

    public ContactInfo(String mail, String telephone) {
        this.email = mail;
        this.telephone = telephone;
    }

    public String getMail() {
        return email;
    }

    public void setMail(String mail) {
        this.email = mail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
