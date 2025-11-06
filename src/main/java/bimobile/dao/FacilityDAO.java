package bimobile.dao;

import jakarta.persistence.*;
import bimobile.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class FacilityDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("carrentalPU");
    private EntityManager em = emf.createEntityManager();

    public void addFacility(Facility facility) {
        em.getTransaction().begin();
        em.persist(facility);
        em.getTransaction().commit();
    }

    public List<Facility> getAllFacilities() {
        return em.createQuery("SELECT c FROM Facility c", Facility.class).getResultList();
    }

    public Facility getFacilityById(Long id) {
        try {
            return em.createQuery("SELECT f FROM Facility f WHERE f.id = :id", Facility.class)
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

    public void updateFacility(Facility facility) {
        em.getTransaction().begin();
        em.merge(facility);
        em.getTransaction().commit();
    }

    public void deleteFacility(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();

            Facility facility = em.find(Facility.class, id);
            if (facility != null) {
                em.remove(facility);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
