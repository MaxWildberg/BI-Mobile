package bimobile.dao;

import bimobile.model.Customer;
import bimobile.model.CustomerInterface;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Beschreibung:
 * Direkter Datenbank Zugriff
 * Speichert, updated und liest Kunden anhand von SQL Kommandos.
 * Erhält Anweisung von CustomerManager bzw. gibt Objekte aus Datenbank daran weiter.
 *
 * @author Max Wildberg
 */

@Repository
public class CustomerDAO {

    @PersistenceContext
    private EntityManager em;

    @jakarta.transaction.Transactional
    public void addCustomer(CustomerInterface customer) {
        em.persist(customer);
    }

    public List<Customer> getAllCustomers() {
        return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }

    public Customer getCustomerById(Long id) {
        try {
            return em.createQuery("SELECT c FROM Customer c WHERE c.id = :id", Customer.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @jakarta.transaction.Transactional
    public void updateCustomer(CustomerInterface customer) {
        em.merge(customer);
    }

    @jakarta.transaction.Transactional
    public boolean deleteCustomer(Long id) {
        Customer customer = em.find(Customer.class, id);
        if (customer != null) {
            em.remove(customer);
            return true;
        }
        return false;
    }
}
