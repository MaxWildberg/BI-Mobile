package bimobile.model.customer;


import jakarta.persistence.Embeddable;

@Embeddable
public class Identification {
    private String driverslicense;
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
