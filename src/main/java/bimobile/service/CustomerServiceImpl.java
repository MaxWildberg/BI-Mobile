package bimobile.service;

import bimobile.dao.RentalRepository;
import bimobile.dao.companyRepositoriy;
import bimobile.dao.CustomerRepository;
import bimobile.model.Rental;
import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final companyRepositoriy companyRepositoriy;
    private final RentalRepository rentalRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               companyRepositoriy companyRepositoriy,
                               RentalRepository rentalRepository) {
        this.customerRepository = customerRepository;
        this.companyRepositoriy = companyRepositoriy;
        this.rentalRepository = rentalRepository;
    }

    /**
     * Registriert einen neuen Kunden. Prüft vorher, ob ein Kunde mit derselben E-Mail existiert.
     */
    @Override
    public String registerCustomer(Customer customer) {
        try {
            String email = customer.getContactInfo() != null
                    ? customer.getContactInfo().getMail()
                    : null;

            if (email != null && customerRepository.existsByContactInfo_Email(email)) {
                return "Fehler: Ein Kunde mit dieser E-Mail existiert bereits";
            }

            customerRepository.save(customer);
            return "Erfolg: Kunde '" + customer.getFullName() + "' wurde erfolgreich angelegt";

        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht gespeichert werden - " + e.getMessage();
        }
    }

    /**
     * Aktualisiert einen bestehenden Kunden.
     */
    @Override
    public String updateCustomer(Customer updated) {

        if (updated.getCustomerId() == null) {
            return "Fehler: Kunde-ID fehlt für das Update";
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(updated.getCustomerId());
        if (optionalCustomer.isEmpty()) {
            return "Fehler: Kunde mit ID " + updated.getCustomerId() + " wurde nicht gefunden";
        }

        try {
            Customer existing = optionalCustomer.get();

            // gesamte Value-Objects ersetzen (sauberste Methode)
            existing.setPersonalData(updated.getPersonalData());
            existing.setAddress(updated.getAddress());
            existing.setContactInfo(updated.getContactInfo());
            existing.setIdentification(updated.getIdentification());

            // Falls BusinessCustomer → Firma übernehmen
            if (updated instanceof bimobile.model.customer.BusinessCustomer ub &&
                    existing instanceof bimobile.model.customer.BusinessCustomer eb) {

                eb.setCompany(ub.getCompany());
            }

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
     * Liefert einen Kunden nach ID.
     */
    @Override
    public Customer getCustomerByID(Long id) {
        if (id == null || id <= 0) return null;
        return customerRepository.findById(id).orElse(null);
    }

    /**
     * Liefert kunden anhand der E-Mail zurück.
     */
    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        if (email == null) return Optional.empty();
        return customerRepository.findByContactInfo_Email(email);
    }

    /**
     * Alle Unternehmen
     */
    @Override
    public List<Company> getAllCompanies() {
        return companyRepositoriy.findAll();
    }

    @Override
    public Company getCompanyById(Long companyId) {
        return companyRepositoriy.getCompanyByCompanyId(companyId);
    }

    @Override
    public boolean existsByContactInfoEmail(String email) {
        return customerRepository.existsByContactInfo_Email(email);
    }

    @Override
    public Company saveCompany(Company company) {
        try {
            String name = company.getName() != null
                    ? company.getName()
                    : null;

            if (name != null && companyRepositoriy.existsByName(name)) {
                System.out.println("Fehler: Eine Firma mit diesem Namen existiert bereits");
                return null;
            }

            companyRepositoriy.save(company);
            System.out.println("Erfolg: Firma '" + company.getName() + "' wurde erfolgreich angelegt");
            return company;

        } catch (Exception e) {
            System.out.println("Fehler: Firma konnte nicht gespeichert werden - " + e.getMessage());
            return null;
        }
    }

    public List<Rental> findAllWithCustomerAndVehicle() {
        return rentalRepository.findAllWithCustomerAndVehicle();
    }


}
