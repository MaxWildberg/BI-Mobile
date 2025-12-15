package bimobile.service.customer;

import bimobile.dao.CompanyRepository;
import bimobile.dao.RentalRepository;
import bimobile.dao.CustomerRepository;

import bimobile.enums.RentalStatus;
import bimobile.model.Rental;
import bimobile.model.customer.*;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    public CustomerService(CustomerRepository customerRepository,
                           RentalRepository rentalRepository) {
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
    }

    public Customer registerCustomer(@Valid Customer customer) {
        validateCustomer(customer);

        String email = customer.getContactInfo().getMail();
        if (customerRepository.existsByContactInfo_Email(email)) {
            throw new DuplicateCustomerException(email);
        }

        return customerRepository.save(customer);
    }


    public void updateCustomer(@Valid Customer updated) {
        if (updated == null) {
            throw new InvalidCustomerDataException("Zu aktualisierender Kunde darf nicht null sein");
        }
        if (updated.getCustomerId() == null) {
            throw new InvalidCustomerDataException("Kunde-ID fehlt für update");
        }
        if (updated.getPersonalData().getBirthday() != null) {
            int age = Period.between(updated.getPersonalData().getBirthday(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new CustomerTooYoungException("Kunde muss mindestens 18 Jahre alt sein. Aktuelles Alter: " + age);
            }
        }
        Customer existing = customerRepository.findById(updated.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(updated.getCustomerId()));

        // Value-Objects (Personendaten, Adresse, Kontaktinfo etc.)
        existing.setPersonalData(updated.getPersonalData());
        existing.setAddress(updated.getAddress());
        existing.setContactInfo(updated.getContactInfo());
        existing.setIdentification(updated.getIdentification());

        // Wenn BusinessCustomer → Firma aktualisieren
        if (updated instanceof BusinessCustomer ub &&
                existing instanceof BusinessCustomer eb) {
            if (ub.getCompany() == null) {
                throw new InvalidCustomerDataException("Firma fehlt für update");
            }
            eb.setCompany(ub.getCompany());
        }

        customerRepository.save(existing);
    }

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public void deleteCustomer(Long id) {
        Customer customer = getCustomerByID(id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Ungültige Kunden-ID");
        }

        boolean hasOpenRents = customer.getRents().stream()
                .filter(r -> r.getStatus() == RentalStatus.ACTIVE)
                .count() > 0;

        if (hasOpenRents) {
            throw new IllegalStateException("Kunde kann nicht gelöscht werden: offene Mieten vorhanden");
        }
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.delete(existing);
    }

    public Customer getCustomerByID(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Ungültige Kunden-ID");
        }

        Optional<Customer> optionalCustomer =
                customerRepository.findByIdWithRentsAndVehicle(id);

        if (!optionalCustomer.isPresent()) {
            throw new CustomerNotFoundException(id);
        }

        return optionalCustomer.get();
    }

    public List<Rental> findAllWithCustomerAndVehicle() {
        return rentalRepository.findAllWithCustomerAndVehicle();
    }

    /**
     *
     * @param customer Kunde der vor speichern validiert werden soll
     */
    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidCustomerDataException("Customer unvollständig");
        }

        // --- PersonalData ---
        PersonalData pd = customer.getPersonalData();
        if (pd == null
                || pd.getFirstname() == null
                || pd.getLastname() == null
                || pd.getBirthday() == null) {
            throw new InvalidCustomerDataException("PersonalData unvollständig");
        }

        // Alter prüfen
        int age = Period.between(pd.getBirthday(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new CustomerTooYoungException("Kunde muss mindestens 18 Jahre alt sein. Aktuelles Alter: " + age);
        }

        // --- Address ---
        Address address = customer.getAddress();
        if (address == null
                || address.getStreet() == null
                || address.getCity() == null) {
            throw new InvalidCustomerDataException("Address unvollständig");
        }

        // --- ContactInfo ---
        ContactInfo ci = customer.getContactInfo();
        if (ci == null
                || ci.getMail() == null) {
            throw new InvalidCustomerDataException("ContactInfo unvollständig");
        }

        // --- Identification ---
        Identification id = customer.getIdentification();
        if (id == null
                || id.getIdcard() == null) {
            throw new InvalidCustomerDataException("Identification unvollständig");
        }

        // Optional: BusinessCustomer-Check
        if (customer instanceof BusinessCustomer bc && bc.getCompany() == null) {
            throw new InvalidCustomerDataException("Company unvollständig");
        }
    }


}