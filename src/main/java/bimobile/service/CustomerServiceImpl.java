package bimobile.service;

import bimobile.dao.CustomerRepository;
import bimobile.model.Customer;
import bimobile.model.CustomerInterface;
import bimobile.model.Rental;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service-Schicht zur Verwaltung der Kunden.
 * Steht zwischen Model und Datenbank.
 *
 * @author Max Wildberg
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Registriert einen neuen Kunden. Prüft vorher, ob ein Kunde mit derselben E-Mail existiert.
     */
    @Override
    public String registerCustomer(CustomerInterface customer) {
        try {
            if (customerRepository.existsByEmail(customer.getEmail())) {
                return "Fehler: Ein Kunde mit dieser E-Mail existiert bereits";
            }

            Customer newCustomer = new Customer(
                    customer.getSalutation().trim(),
                    customer.getFirstName().trim(),
                    customer.getLastName().trim(),
                    customer.getBirthday(),
                    customer.getAddress().trim(),
                    customer.getZip().trim(),
                    customer.getResidence().trim(),
                    customer.getCountry().trim(),
                    customer.getEmail().trim(),
                    customer.getTelephone().trim(),
                    customer.getDriversLicenseID().trim(),
                    customer.getIdCardNumber().trim()
            );

            customerRepository.save(newCustomer);
            return "Erfolg: Kunde '" + newCustomer.getFullName() + "' wurde erfolgreich angelegt";
        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht gespeichert werden - " + e.getMessage();
        }
    }

    /**
     * Aktualisiert einen bestehenden Kunden.
     */
    @Override
    public String updateCustomer(CustomerInterface customer) {
        if (customer.getCustomerId() == null) {
            return "Fehler: Kunde-ID fehlt für das Update";
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(customer.getCustomerId());
        if (optionalCustomer.isEmpty()) {
            return "Fehler: Kunde mit ID " + customer.getCustomerId() + " wurde nicht gefunden";
        }

        try {
            Customer existing = optionalCustomer.get();
            existing.setFirstName(customer.getFirstName().trim());
            existing.setLastName(customer.getLastName().trim());
            existing.setBirthday(customer.getBirthday());
            existing.setAddress(customer.getAddress().trim());
            existing.setZip(customer.getZip().trim());
            existing.setResidence(customer.getResidence().trim());
            existing.setCountry(customer.getCountry().trim());
            existing.setEmail(customer.getEmail().trim());
            existing.setTelephone(customer.getTelephone().trim());
            existing.setDriverslicenseID(customer.getDriversLicenseID().trim());
            existing.setIdCardNumber(customer.getIdCardNumber().trim());

            customerRepository.save(existing);
            return "Erfolg: Kunde wurde aktualisiert";
        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht aktualisiert werden - " + e.getMessage();
        }
    }

    /**
     * Liefert alle Kunden zurück.
     */

    @Override
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Löscht einen Kunden nach ID.
     */
    @Override
    public String deleteCustomer(Long id) {
        if (id == null || id <= 0) {
            return "Fehler: Ungültige Kunden-ID";
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            return "Fehler: Kunde mit ID " + id + " wurde nicht gefunden";
        }

        try {
            customerRepository.deleteById(id);
            return "Erfolg: Kunde wurde gelöscht";
        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht gelöscht werden - " + e.getMessage();
        }
    }

    /**
     * Liefert einen Kunden nach ID oder null, wenn nicht vorhanden.
     */
    @Override
    public Customer getCustomerByID(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return customerRepository.findById(id).orElse(null);
    }

    /**
     * Liefert einen Kunden nach E-Mail zurück.
     */
    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

}