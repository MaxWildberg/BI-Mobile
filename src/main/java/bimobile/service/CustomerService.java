package bimobile.service;

import bimobile.dao.CompanyRepository;
import bimobile.dao.RentalRepository;
import bimobile.dao.CustomerRepository;

import bimobile.model.Rental;
import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import bimobile.model.customer.BusinessCustomer;

import org.springframework.stereotype.Service;

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

    public Customer registerCustomer(Customer customer) {

        String email = customer.getContactInfo() != null
                ? customer.getContactInfo().getMail()
                : null;

        if (email != null && customerRepository.existsByContactInfo_Email(email)) {
            throw new DuplicateCustomerException(email);
        }

        return customerRepository.save(customer);
    }

    public void updateCustomer(Customer updated) {

        if (updated.getCustomerId() == null) {
            throw new IllegalArgumentException("Kunde-ID fehlt für das Update");
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
        return companyRepositoriy.getCompanyByCompanyId(companyId);
    }

    public boolean existsByContactInfoEmail(String email) {
        return customerRepository.existsByContactInfo_Email(email);
    }

    public Company saveCompany(Company company) {

        String name = company.getName();

        if (name != null && companyRepositoriy.existsByName(name)) {
            throw new CompanyNameExistsException(name);
        }

        return companyRepositoriy.save(company);
    }

    public List<Rental> findAllWithCustomerAndVehicle() {
        return rentalRepository.findAllWithCustomerAndVehicle();
    }
}