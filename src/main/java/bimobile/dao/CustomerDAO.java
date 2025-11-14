package bimobile.dao;

import bimobile.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerDAO {

    @PersistenceContext
    private EntityManager em;

    @jakarta.transaction.Transactional
    public void addCustomer(Customer customer) {
        em.persist(customer);
    }

    public List<Customer> getAllCustomers() {
        return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }

    public Customer getCustomerById(Long id) {
        try {
            return em.createQuery("SELECT f FROM Customer f WHERE f.id = :id", Customer.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @jakarta.transaction.Transactional
    public void updateCustomer(Customer customer) {
        em.merge(customer);
    }

    @jakarta.transaction.Transactional
    public void deleteCustomer(Long id) {
        Customer customer = em.find(Customer.class, id);
        if (customer != null) {
            em.remove(customer);
        }
    }
}
