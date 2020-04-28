package reason;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reason.lifecycle2.Customer;
import reason.lifecycle2.CustomerRepository;
import reason.lifecycle2.Registrar;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CustomerTest2 {
    @BeforeAll
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
