package bimobile.model.customer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Repräsentiert einen privaten Kunden.
 * Erbt von {@link Customer} und speichert die typischen Kundendaten.
 * Wird in der Datenbank mit Wert "PRIVATE" gespeichert.
 *
 * @author Max Wildberg
 */
@Entity
@DiscriminatorValue("PRIVATE")
public class PrivateCustomer extends Customer {

    public PrivateCustomer() {}

    public PrivateCustomer(PersonalData personalData,
                           Address address,
                           ContactInfo contactInfo,
                           Identification identification) {
        super(personalData, address, contactInfo, identification);
    }
}
