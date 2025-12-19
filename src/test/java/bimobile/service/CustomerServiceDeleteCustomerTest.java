package bimobile.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Collections;

import bimobile.dao.CustomerRepository;
import bimobile.enums.RentalStatus;
import bimobile.model.Rental;
import bimobile.model.customer.Customer;
import bimobile.service.customer.CustomerNotFoundException;
import bimobile.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * Testklasse zum Testen der Methode {@link CustomerService#deleteCustomer(Long)}
 *
 * @author Max Wildberg
 */
public class CustomerServiceDeleteCustomerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // uÄK1: id = null -> IllegalArgumentException
    @Test
    void testDeleteCustomer_idIsNull_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.deleteCustomer(null)
        );
        assertEquals("Ungültige Kunden-ID", exception.getMessage());
    }

    // uÄK2: id < 0 -> IllegalArgumentException
    @Test
    void testDeleteCustomer_idIsNegative_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.deleteCustomer(-1L)
        );
        assertEquals("Ungültige Kunden-ID", exception.getMessage());
    }

    // gÄK1: Valider Kunde ohne offene Mieten -> Kunde wird gelöscht, keine Exception
    @Test
    void testDeleteCustomer_validId_noOpenRents_deletesCustomer() {
        Customer customer = mock(Customer.class);
        when(customer.getRents()).thenReturn(Collections.emptyList());
        when(customerRepository.findByIdWithRentsAndVehicle(1L)).thenReturn(Optional.of(customer));

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));
        verify(customerRepository, times(1)).delete(customer);
    }

    // uÄK3: Valider Kunde mit offenen Mieten -> IllegalStateException
    @Test
    void testDeleteCustomer_validId_withOpenRents_throwsIllegalStateException() {
        Customer customer = mock(Customer.class);
        Rental rent = mock(Rental.class);
        when(rent.getStatus()).thenReturn(RentalStatus.ACTIVE);
        when(customer.getRents()).thenReturn(Collections.singletonList(rent));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> {
                    when(customerRepository.findByIdWithRentsAndVehicle(2L)).thenReturn(Optional.of(customer));
                    customerService.deleteCustomer(2L);
                }
        );
        assertEquals("Kunde kann nicht gelöscht werden: offene Mieten vorhanden", exception.getMessage());
    }

    // uÄK4: Valide id aber Kunde existiert nicht -> CustomerNotFoundException
    @Test
    void testDeleteCustomer_validId_customerNotFound_throwsCustomerNotFoundException() {
        when(customerRepository.findByIdWithRentsAndVehicle(999L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(999L)
        );
        assertTrue(exception.getMessage().contains("999"));

    }
}
