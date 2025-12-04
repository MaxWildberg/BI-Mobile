package bimobile.controller;

import bimobile.dao.CustomerDAO;
import bimobile.model.Customer;
import bimobile.model.CustomerInterface;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Beschreibung:
 * Service Schicht zur Verwaltung der Kunden.
 * Steht zwischen Model und Datenbank.
 *
 * @author Max Wildberg
 */

@Service
public class CustomerManager {

    private CustomerDAO customerDAO;

    public CustomerManager(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public String registerCustomer(CustomerInterface customer) {
        try {
            // Customer customer = new Customer(name.trim(), lastname.trim(), birthday, zip.trim(), address.trim(), residence.trim(), email.trim(), telephone.trim(), driverslicenseID.trim(), idCardNumber.trim());
            customerDAO.addCustomer(customer);
            return "Erfolg: Kunde '" + customer.getName() + " " + customer.getLastname() + "' wurde erfolgreich angelegt";
        } catch (Exception exception) {
            return "Fehler: Kunde konnte nicht gespeichert werden - " + exception.getMessage();
        }
    }

    public String updateCustomer(CustomerInterface customer) {
        try {
            //Customer customer = new Customer(name.trim(), lastname.trim(), birthday, zip.trim(), address.trim(), residence.trim(), email.trim(), telephone.trim(), driverslicenseID.trim(), idCardNumber.trim());
            customerDAO.updateCustomer(customer);
            return "Erfolg: Kunde wurde aktualisiert";
        } catch (Exception e) {
            return "Fehler: Kunde konnte nicht aktualisiert werden - " + e.getMessage();
        }
    }

    public List<Customer> getAllCustomers() {
        try {
            return customerDAO.getAllCustomers();
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Kunden: " + e.getMessage());
            return List.of();
        }
    }

    public String deleteCustomer(Long id) {
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
                return customerDAO.getCustomerById(id);
            } catch (Exception e) {
                return null;
            }
        }
    }




}
