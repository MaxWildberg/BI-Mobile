package bimobile.dao;

import jakarta.persistence.*;
import bimobile.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO für die Employee-Entität.
 *
 * @author Jan Lasse Stegmann
 */
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

    // Sucht einen Mitarbeiter per ID und fängt den Fall ab, dass kein Treffer existiert (gibt null zurück)
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

    // Lädt die Entität zuerst, um sicherzustellen, dass sie im Persistence Context ist, bevor gelöscht wird
    public void deleteEmployee(Long id) {
        Employee employee = em.find(Employee.class, id);
        if (employee != null) {
            em.remove(employee);
        }
    }

    // Filtert Mitarbeiter, die einem spezifischen Standort (Facility) zugeordnet sind
    public List<Employee> getEmployeesByFacility(Long facilityId) {
        return em.createQuery(
                        "SELECT e FROM Employee e WHERE e.facility.id = :facilityId",
                        Employee.class
                )
                .setParameter("facilityId", facilityId)
                .getResultList();
    }
}