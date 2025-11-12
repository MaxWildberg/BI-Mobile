package bimobile.dao;

import jakarta.persistence.*;
import bimobile.model.Facility;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class FacilityDAO {

    @PersistenceContext
    private EntityManager em;

    @jakarta.transaction.Transactional
    public void addFacility(Facility facility) {
        em.persist(facility);
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

    @jakarta.transaction.Transactional
    public void updateFacility(Facility facility) {
        em.merge(facility);
    }

    @jakarta.transaction.Transactional
    public void deleteFacility(Long id) {
        Facility facility = em.find(Facility.class, id);
        if (facility != null) {
            em.remove(facility);
        }
    }
}