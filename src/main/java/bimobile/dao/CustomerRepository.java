package bimobile.dao;

import bimobile.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	// Bei Bedarf später: Suchmethoden wie findByEmail(String email) etc.
}
