package bimobile.service;

import bimobile.dao.CustomerDAO;
import bimobile.model.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

// CustomerService ersetzt mit CustomerManager: View -> CustomerManager -> CustomerDAO anstatt View -> Controller -> Service -> DAO


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

    public Customer getCustomerById(Long id){
        return customerDAO.getCustomerById(id);
    }

    public boolean deleteCustomer(Long id){
        return customerDAO.deleteCustomer(id);
    }

    /*public Customer getExampleCustomer() {
        customerDAO.addCustomer();
    }*/
}
