package bimobile.model.customer;

import jakarta.persistence.Embeddable;

@Embeddable
public class ContactInfo {
    private String email;
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
