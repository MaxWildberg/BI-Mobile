package bimobile.model.customer;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Embeddable
public class PersonalData {

    @NotNull(message = "Titel darf nicht null sein")
    private String title;

    @NotNull(message = "Vorname darf nicht null sein")
    private String firstname;

    @NotNull(message = "Nachname darf nicht null sein")
    private String lastname;

    @NotNull(message = "Geburtsdatum darf nicht null sein")
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
