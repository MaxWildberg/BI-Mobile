package bimobile.service;

import bimobile.model.Customer;
import bimobile.model.CustomerInterface;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    String registerCustomer(CustomerInterface customer);

    String updateCustomer(CustomerInterface customer);

    List<Customer> findAllCustomers();

    String deleteCustomer(Long id);

    Customer getCustomerByID(Long id);

    Optional<Customer> getCustomerByEmail(String email);
}
