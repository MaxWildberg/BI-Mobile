package bimobile.sevice;

import bimobile.dao.CustomerRepository;
import bimobile.service.CustomerService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceEquivalenceTest {

    @Mock
    private CustomerRepository repo;

    @InjectMocks
    private CustomerService service;


}
