package bimobile.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import bimobile.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeDAO {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("carrentalPU");
    private final EntityManager em = emf.createEntityManager();

    public void addEmployee(Employee employee) {
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
    }

    public List<Employee> getAllEmployees() {
        return em.createQuery("SELECT e FROM Employee e", Employee.class)
                .getResultList();
    }

    public Employee getEmployeeById(Long id) {
        try {
            return em.createQuery(
                            "SELECT e FROM Employee e WHERE e.id = :id",
                            Employee.class
                    )
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void updateEmployee(Employee employee) {
        em.getTransaction().begin();
        em.merge(employee);
        em.getTransaction().commit();
    }

    public List<Employee> getEmployeesByFacility(Long facilityId) {
        return em.createQuery(
                        "SELECT e FROM Employee e WHERE e.facility.id = :facilityId",
                        Employee.class
                )
                .setParameter("facilityId", facilityId)
                .getResultList();
    }

    public void close() {
        em.close();
        emf.close();
    }
}
