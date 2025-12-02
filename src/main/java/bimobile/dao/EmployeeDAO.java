package bimobile.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import bimobile.model.Employee;

import java.util.List;

public class EmployeeDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("carrentalPU");
    private EntityManager em = emf.createEntityManager();

    public void addEmployee(Employee employee) {
        em.getTransaction().begin();
        em.persist(employee); // Save to database
        em.getTransaction().commit();
    }

    public List<Employee> getAllEmployees() {
        return em.createQuery("SELECT c FROM Employee c", Employee.class).getResultList();
    }

    public Employee getEmployeeById(Long id) {
        try {
            return em.createQuery("SELECT e FROM Employee e WHERE e.id = :id", Employee.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void close() {
        em.close();
        emf.close();
    }

}