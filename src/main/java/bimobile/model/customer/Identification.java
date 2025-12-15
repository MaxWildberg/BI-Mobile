package bimobile.model.customer;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

/**
 * Eingebettetes Value-Objekt zur Speicherung von Identifikationsinformationen eines Kunden.
 * Enthält die Führerscheinnummer und die Ausweisnummer.
 *
 * Durch die Verwendung von {@link Embeddable} werden die Felder direkt in der Tabelle
 * der übergeordneten Entity gespeichert, es entsteht keine eigene Tabelle.
 *
 * @author Max Wildberg
 */
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
