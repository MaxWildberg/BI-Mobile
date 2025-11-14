package bimobile.controller;

import bimobile.dao.CustomerDAO;
import bimobile.model.Customer;
import bimobile.service.CustomerService;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CustomerController {

    private CustomerService customerService;
    private CustomerDAO customerDAO;

    public CustomerController(CustomerService customerService, CustomerDAO customerDAO) {
        this.customerService = customerService;
        this.customerDAO = customerDAO;
    }

    // String name, String lastname, LocalDate birthday, String address, String zip,  String residence, String driverslicenseID, String idCardNumber, String email, String telephone
    public String registerCustomer(Customer customer) {
        try {
            // Customer customer = new Customer(name.trim(), lastname.trim(), birthday, zip.trim(), address.trim(), residence.trim(), email.trim(), telephone.trim(), driverslicenseID.trim(), idCardNumber.trim());
            customerService.addCustomer(customer);
            return "Erfolg: Kunde '" + customer.getName() + " " + customer.getLastname() + "' wurde erfolgreich angelegt";
        } catch (Exception exception) {
            return "Fehler: Kunde konnte nicht gespeichert werden - " + exception.getMessage();
        }
    }

    //String name, String lastname, LocalDate birthday, String address, String zip,  String residence, String driverslicenseID, String idCardNumber, String email, String telephone

    public String updateCustomer(Customer customer) {
        try {
            //Customer customer = new Customer(name.trim(), lastname.trim(), birthday, zip.trim(), address.trim(), residence.trim(), email.trim(), telephone.trim(), driverslicenseID.trim(), idCardNumber.trim());
            customerService.updateCustomer(customer);
            return "Erfolg: Kunde wurde aktualisiert";
        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht aktualisiert werden - " + e.getMessage();
        }
    }

    public List<Customer> getAllCustomers() {
        try {
            return customerService.getAllCustomers();
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Kunden: " + e.getMessage());
            return List.of();
        }
    }

    public String deaktivateCustomer(Long id) {
        if (id == null || id <= 0) {
            return "Fehler: Ungültige Kunden-ID";
        }

        Customer customer = customerDAO.getCustomerById(id);
        if (customer == null) {
            return "Fehler: Kunde mit ID " + id + " wurde nicht gefunden";
        }

        try {
            customerDAO.deleteCustomer(id);
            return "Erfolg: Kunde wurde gelöscht";
        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht gelöscht werden - " + e.getMessage();
        }
    }

    public Customer getCustomerByID(Long id){
        if (id == null || id <= 0) {
            return null;
        } else {
            try {
                customerDAO.getCustomerById(id);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }




}
