import lifecycle2.Customer;
import lifecycle2.CustomerRepository;
import lifecycle2.Registrar;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class CustomerTest2 {
    @Before
    public void setUp() {
        Registrar.init();
    }

    @Test
    public void testCustomerIdentical() {
        CustomerRepository customerRepository = new CustomerRepository();
        Customer customer = new Customer("CUST-01", "홍길동", "경기도 안양시");
        customerRepository.save(customer);
        Customer anotherCustomer = customerRepository.find("CUST-01");
        assertSame(customer, anotherCustomer);
    }
}
