package bimobile.service;

import bimobile.dao.CustomerDAO;
import bimobile.model.Customer;
import bimobile.model.Facility;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO){
        this.customerDAO = customerDAO;
    }

    public void addCustomer(Customer customer){
        customerDAO.addCustomer(customer);
    }

    public List<Customer> getAllCustomers(){
        return customerDAO.getAllCustomers();
    }

    public  void updateCustomer(Customer customer) {
        customerDAO.updateCustomer(customer);
    }

    public Customer getCustomerByID(Long id){
        return customerDAO.getCustomerById(id);
    }

    /*public Customer getExampleCustomer() {
        customerDAO.addCustomer();
    }*/
}
