package bimobile.model.customer;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class PersonalData {
    private String title;
    private String firstname;
    private String lastname;
    private LocalDate birthday;

    public PersonalData(){}

    public PersonalData(String title, String firstname, String lastname, LocalDate birthday) {
        this.title = title;
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = birthday;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
