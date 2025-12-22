package bimobile.service.customer;

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

/**
 * Service zur Verwaltung von Kundenobjekten.
 * Bietet Funktionen zum Erstellen, Aktualisieren, Löschen und Abrufen von Kunden.
 * Validiert Daten und prüft auf Duplikate, Mindestalter und offene Mieten.
 *
 * @author Max Wildberg
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    public CustomerService(CustomerRepository customerRepository,
                           RentalRepository rentalRepository) {
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
    }

    /**
     * Legt einen neuen Kunden an.
     * Prüft auf Duplikate anhand der E-Mail und auf Mindestalter.
     * @param customer Zu speichernder Kunde
     * @return Gespeicherter Kunde
     * @throws DuplicateCustomerException wenn E-Mail bereits existiert
     * @throws InvalidCustomerDataException wenn Daten unvollständig sind
     * @throws CustomerTooYoungException wenn Kunde jünger als 18 Jahre ist
     */
    public Customer registerCustomer(@Valid Customer customer) {
        validateCustomer(customer);

        String email = customer.getContactInfo().getMail();
        if (customerRepository.existsByContactInfo_Email(email)) {
            throw new DuplicateCustomerException(email);
        }

        return customerRepository.save(customer);
    }

    /**
     * Aktualisiert einen bestehenden Kunden.
     * Prüft auf Mindestalter und BusinessCustomer-Firma.
     * @param updated Kunde mit aktualisierten Daten
     * @throws InvalidCustomerDataException wenn Daten unvollständig oder null
     * @throws CustomerTooYoungException wenn Kunde jünger als 18 Jahre ist
     * @throws CustomerNotFoundException wenn Kunde nicht existiert
     */
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

        existing.setPersonalData(updated.getPersonalData());
        existing.setAddress(updated.getAddress());
        existing.setContactInfo(updated.getContactInfo());
        existing.setIdentification(updated.getIdentification());

        if (updated instanceof BusinessCustomer ub &&
                existing instanceof BusinessCustomer eb) {
            if (ub.getCompany() == null) {
                throw new InvalidCustomerDataException("Firma fehlt für update");
            }
            eb.setCompany(ub.getCompany());
        }

        customerRepository.save(existing);
    }

    /**
     * Liefert alle Kunden zurück.
     * @return Liste aller Kunden
     */
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Löscht einen Kunden anhand der ID.
     * Prüft auf offene Mieten.
     * @param id ID des zu löschenden Kunden
     * @throws IllegalArgumentException bei null oder ungültiger ID
     * @throws IllegalStateException wenn offene Mieten existieren
     * @throws CustomerNotFoundException wenn Kunde nicht existiert
     */
    public void deleteCustomer(Long id) {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("Ungültige Kunden-ID");
        }

        Customer customer = getCustomerByID(id);

        boolean hasOpenRents = customer.getRents().stream()
                .filter(r -> r.getStatus() == RentalStatus.ACTIVE)
                .count() > 0;

        if (hasOpenRents) {
            throw new IllegalStateException("Kunde kann nicht gelöscht werden: offene Mieten vorhanden");
        }



        customerRepository.delete(customer);
    }

    /**
     * Liefert einen Kunden anhand der ID.
     * @param id ID des Kunden
     * @return Gefundener Kunde
     * @throws IllegalArgumentException bei null oder ungültiger ID
     * @throws CustomerNotFoundException wenn Kunde nicht existiert
     */
    public Customer getCustomerByID(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Ungültige Kunden-ID");
        }

        Optional<Customer> optionalCustomer =
                customerRepository.findByIdWithRentsAndVehicle(id);

        return optionalCustomer.orElseThrow(() -> new CustomerNotFoundException(id));
    }

    /**
     * Liefert alle Mieten inklusive zugehöriger Kunden- und Fahrzeuginformationen.
     * @return Liste aller Mieten
     */
    public List<Rental> findAllWithCustomerAndVehicle() {
        return rentalRepository.findAllWithCustomerAndVehicle();
    }

    /**
     * Validiert ein Kundenobjekt vor dem Speichern.
     * Prüft auf Pflichtfelder, Mindestalter und bei {@link BusinessCustomer} auf Firma.
     * @param customer Kunde zur Validierung
     * @throws InvalidCustomerDataException bei unvollständigen Daten
     * @throws CustomerTooYoungException wenn Kunde jünger als 18 Jahre ist
     */
    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidCustomerDataException("Customer unvollständig");
        }

        PersonalData pd = customer.getPersonalData();
        if (pd == null
                || pd.getFirstname() == null
                || pd.getLastname() == null
                || pd.getBirthday() == null) {
            throw new InvalidCustomerDataException("PersonalData unvollständig");
        }

        int age = Period.between(pd.getBirthday(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new CustomerTooYoungException("Kunde muss mindestens 18 Jahre alt sein. Aktuelles Alter: " + age);
        }

        Address address = customer.getAddress();
        if (address == null || address.getStreet() == null || address.getCity() == null) {
            throw new InvalidCustomerDataException("Address unvollständig");
        }

        ContactInfo ci = customer.getContactInfo();
        if (ci == null || ci.getMail() == null) {
            throw new InvalidCustomerDataException("ContactInfo unvollständig");
        }

        Identification id = customer.getIdentification();
        if (id == null || id.getIdcard() == null) {
            throw new InvalidCustomerDataException("Identification unvollständig");
        }

        if (customer instanceof BusinessCustomer bc && bc.getCompany() == null) {
            throw new InvalidCustomerDataException("Company unvollständig");
        }
    }
}
