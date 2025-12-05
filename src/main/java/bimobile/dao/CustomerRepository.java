package bimobile.dao;

import bimobile.model.Customer;
import bimobile.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer getCustomerByCustomerId(Long id);

    boolean existsByEmail(String email);

    // Findet einen Kunden nach E-Mail
    Optional<Customer> findByEmail(String email);

    // Optional: Nach Name und Nachname suchen
    Optional<Customer> findByFirstnameAndLastname(String firstname, String lastname);

}