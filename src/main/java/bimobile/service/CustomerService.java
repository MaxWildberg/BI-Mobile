package bimobile.service;

import bimobile.dao.CustomerRepository;
import bimobile.model.Customer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Ben Berlin
 */
@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	/**
	 * Liefert alle Kunden, sortiert wie vom Repository vorgegeben.
	 * Wird z.B. in RentalsOverviewView für die ComboBox verwendet.
	 */
	public List<Customer> findAll() {
		return customerRepository.findAll();
	}

	/**
	 * Einfacher Create/Update – praktisch, falls du später eine Kundenverwaltung einbaust.
	 */
	public Customer save(Customer customer) {
		return customerRepository.save(customer);
	}

	public Optional<Customer> findById(Long id) {
		return customerRepository.findById(id);
	}

	public void deleteById(Long id) {
		customerRepository.deleteById(id);
	}
}

