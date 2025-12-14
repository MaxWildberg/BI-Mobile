package bimobile.service;

import bimobile.dao.CompanyRepository;
import bimobile.dao.RentalRepository;
import bimobile.dao.CustomerRepository;

import bimobile.model.Rental;
import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import bimobile.model.customer.BusinessCustomer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepositoriy;
    private final RentalRepository rentalRepository;

    public CustomerService(CustomerRepository customerRepository,
                           CompanyRepository companyRepositoriy,
                           RentalRepository rentalRepository) {
        this.customerRepository = customerRepository;
        this.companyRepositoriy = companyRepositoriy;
        this.rentalRepository = rentalRepository;
    }

    public Customer registerCustomer(@Valid Customer customer) {

        String email = customer.getContactInfo().getMail();
        if (customerRepository.existsByContactInfo_Email(email)) {
            throw new DuplicateCustomerException(email);
        }
        if (customer.getPersonalData().getBirthday() != null) {
            int age = Period.between(customer.getPersonalData().getBirthday(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new CustomerTooYoungException("Kunde muss mindestens 18 Jahre alt sein. Aktuelles Alter: " + age);
            }
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

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Ungültige Kunden-ID");
        }

        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customerRepository.delete(existing);
    }

    public Customer getCustomerByID(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Ungültige Kunden-ID");
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        if (email == null) return Optional.empty();
        return customerRepository.findByContactInfo_Email(email);
    }

    public List<Company> getAllCompanies() {
        return companyRepositoriy.findAll();
    }

    public Company getCompanyById(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Ungültige Firmen-ID");
        }
        return companyRepositoriy.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    public boolean existsByContactInfoEmail(String email) {
        return customerRepository.existsByContactInfo_Email(email);
    }

    public Company saveCompany(Company company) {

        String name = company.getName();

        if (name != null && companyRepositoriy.existsByName(name)) {
            throw new DuplicateCompanyException(name);
        }

        return companyRepositoriy.save(company);
    }

    public List<Rental> findAllWithCustomerAndVehicle() {
        return rentalRepository.findAllWithCustomerAndVehicle();
    }
}