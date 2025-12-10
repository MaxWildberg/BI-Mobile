package bimobile.model.customer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
