package bimobile.model.customer;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Identification {

    @NotNull(message = "Führerscheinnummer darf nicht null sein")
    private String driverslicense;

    @NotNull(message = "Ausweisnummer darf nicht null sein")
    private String idcard;

    public Identification() {}

    public Identification(String driverslicense, String idcard) {
        this.driverslicense = driverslicense;
        this.idcard = idcard;
    }

    public String getDriverslicense() {
        return driverslicense;
    }

    public void setDriverslicense(String driverslicense) {
        this.driverslicense = driverslicense;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }
}
