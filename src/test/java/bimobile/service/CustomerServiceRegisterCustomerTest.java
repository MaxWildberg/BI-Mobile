package bimobile.service;

import bimobile.dao.CustomerRepository;
import bimobile.model.customer.Customer;
import bimobile.model.customer.ContactInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Äquivalenzklassentests + zugehörige JUnit-Tests
 * für die {@link bimobile.service.CustomerService#registerCustomer} Methode.
 * <p>
 * Die Tests dokumentieren die wichtigsten validierungsrelevanten Randfälle, sodass
 * nachvollziehbar bleibt, welche Eingaben akzeptiert oder abgelehnt werden. Sie dienen
 * damit gleichzeitig als lebendige Spezifikation für die Studienarbeit.
 *
 * Autor: Max Wildberg
 */
@ExtendWith(MockitoExtension.class)
public class CustomerServiceRegisterCustomerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // ÄQUIVALENZKLASSE 1: Kunde mit bereits vorhandener E-Mail → DuplicateCustomerException
    @Test
    void ek_duplicateEmail_throwsDuplicateCustomerException() {
        Customer customer = mock(Customer.class);
        ContactInfo contactInfo = mock(ContactInfo.class);
        when(customer.getContactInfo()).thenReturn(contactInfo);
        when(contactInfo.getMail()).thenReturn("test@mail.de");

        when(customerRepository.existsByContactInfo_Email("test@mail.de")).thenReturn(true);

        assertThrows(DuplicateCustomerException.class,
                () -> customerService.registerCustomer(customer));

        verify(customerRepository, never()).save(any());
    }

    // ÄQUIVALENZKLASSE 2: Kunde mit null ContactInfo → InvalidCustomerDataException
    @Test
    void ek_nullContactInfo_throwsInvalidCustomerDataException() {
        Customer customer = mock(Customer.class);
        when(customer.getContactInfo()).thenReturn(null);

        assertThrows(InvalidCustomerDataException.class,
                () -> customerService.registerCustomer(customer));

        verify(customerRepository, never()).save(any());
    }

    // JUNIT-TEST 1: Gültiger Kunde → Registrierung erfolgreich
    @Test
    void validCustomer_registersSuccessfully() {
        Customer customer = mock(Customer.class);
        ContactInfo contactInfo = mock(ContactInfo.class);
        when(customer.getContactInfo()).thenReturn(contactInfo);
        when(contactInfo.getMail()).thenReturn("test@mail.de");

        when(customerRepository.existsByContactInfo_Email("test@mail.de")).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.registerCustomer(customer);

        assertNotNull(result);
        verify(customerRepository).save(customer);
    }

    // JUNIT-TEST 2: Fehlende PersonalData → InvalidCustomerDataException
    @Test
    void missingPersonalData_throwsInvalidCustomerDataException() {
        Customer customer = mock(Customer.class);
        ContactInfo contactInfo = mock(ContactInfo.class);
        when(customer.getContactInfo()).thenReturn(contactInfo);
        when(contactInfo.getMail()).thenReturn("test@mail.de");
        when(customer.getPersonalData()).thenReturn(null);

        when(customerRepository.existsByContactInfo_Email("test@mail.de")).thenReturn(false);

        assertThrows(InvalidCustomerDataException.class,
                () -> customerService.registerCustomer(customer));

        verify(customerRepository, never()).save(any());
    }
}
