package bimobile.dao;

import jakarta.persistence.*;
import bimobile.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeDAO {

    @PersistenceContext
    private EntityManager em;

    public EmployeeDAO() {
    }

    public void addEmployee(Employee employee) {
        em.persist(employee);
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
        em.merge(employee);
    }

    public List<Employee> getEmployeesByFacility(Long facilityId) {
        return em.createQuery(
                        "SELECT e FROM Employee e WHERE e.facility.id = :facilityId",
                        Employee.class
                )
                .setParameter("facilityId", facilityId)
                .getResultList();
    }
}
