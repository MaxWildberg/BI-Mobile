package bimobile.service;

import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    String registerCustomer(Customer customer);

    String updateCustomer(Customer customer);

    List<Customer> findAllCustomers();

    String deleteCustomer(Long id);

    Customer getCustomerByID(Long id);

    Optional<Customer> getCustomerByEmail(String email);

    List<Company> getAllCompanies();

    Company getCompanyById(Long companyId);

    boolean existsByContactInfoEmail(String email);

    Company saveCompany(Company newCompany);
}