package bimobile.dao;

import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByContactInfo_Email(String email);

    // Findet einen Kunden nach E-Mail
    Optional<Customer> findByContactInfo_Email(String email);

    // Optional: Nach Name und Nachname suchen
    Optional<Customer> findByPersonalDataLastname(String lastname);

    @Query("""
        SELECT c FROM Customer c
        LEFT JOIN FETCH c.rents r
        LEFT JOIN FETCH r.vehicle
        WHERE c.customerId = :id
    """)
    Optional findByIdWithRentsAndVehicle(@Param("id") Long id);


}