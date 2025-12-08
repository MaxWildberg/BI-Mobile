package bimobile.dao;

import bimobile.model.customer.Company;
import bimobile.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByContactInfo_Email(String email);

    // Findet einen Kunden nach E-Mail
    Optional<Customer> findByContactInfo_Email(String email);

    // Optional: Nach Name und Nachname suchen
    Optional<Customer> findByPersonalDataLastname(String lastname);

}