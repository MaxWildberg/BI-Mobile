package bimobile.service;

import bimobile.dao.CustomerRepository;
import bimobile.model.customer.*;

import bimobile.service.customer.CustomerService;
import bimobile.service.customer.DuplicateCustomerException;
import bimobile.service.customer.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Äquivalenzklassentests + zugehörige JUnit-Tests
 * für die {@link CustomerService#registerCustomer} Methode.
 * Die Tests dokumentieren die wichtigsten validierungsrelevanten Randfälle, sodass
 * nachvollziehbar bleibt, welche Eingaben akzeptiert oder abgelehnt werden.
 *
 * Autor: Max Wildberg
 */
@ExtendWith(MockitoExtension.class)
public class CustomerServiceRegisterCustomerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // uÄK1: Kunde mit unvollständigem Datensatz -> InvalidCustomerDataException
    @Test
    void ek_nullContactInfo_throwsInvalidCustomerDataException() {
        Customer customer = createValidCustomer();
        customer.setContactInfo(null);

        assertThrows(InvalidDataException.class,
                () -> customerService.registerCustomer(customer)
        );

        verify(customerRepository, never()).save(any());
    }

    // uÄK2: Kunde mit bereits vorhandener E-Mail -> DuplicateCustomerException
    @Test
    void ek_duplicateEmail_throwsDuplicateCustomerException() {
        Customer customer = createValidCustomer();

        when(customerRepository.existsByContactInfo_Email("test@mail.de")).thenReturn(true);

        assertThrows(DuplicateCustomerException.class,
                () -> customerService.registerCustomer(customer));

        verify(customerRepository, never()).save(any());
    }


    // gÄK1: Valider Kunde + E-Mail neu -> Kunde wird gespeichert, keine Exception
    @Test
    void validCustomer_registersSuccessfully() {
        Customer customer = createValidCustomer();

        when(customerRepository.existsByContactInfo_Email("test@mail.de")).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.registerCustomer(customer);

        assertNotNull(result);
        verify(customerRepository).save(customer);
    }

    /**
     * @return Valides Kundenobjekt an alle Testmethoden
     */
    private Customer createValidCustomer() {
        Customer customer = new PrivateCustomer();

        PersonalData pd = new PersonalData();
        pd.setTitle("Herr");
        pd.setFirstname("Max");
        pd.setLastname("Mustermann");
        pd.setBirthday(LocalDate.now().minusYears(25));

        Address address = new Address();
        address.setStreet("Musterstraße 1");
        address.setCity("Berlin");
        address.setZip("12345");
        address.setCountry("Deutschland");

        ContactInfo ci = new ContactInfo();
        ci.setMail("test@mail.de");
        ci.setTelephone("12345");

        Identification id = new Identification();
        id.setIdcard("ID123");
        id.setDriverslicense("DL123");

        customer.setPersonalData(pd);
        customer.setAddress(address);
        customer.setContactInfo(ci);
        customer.setIdentification(id);

        return customer;
    }


}
