import lifecycle.Customer;
import lifecycle.Registrar;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class CustomerTest {
    @Before
    public void setUp() {
        Registrar.init();
    }

    @Test
    public void testCustomerIdentical() {
        Customer customer = new Customer("CUST-01", "홍길동", "경기도 안양시").persist();
        Customer anotherCustomer = Customer.find("CUST-01");
        assertSame(customer, anotherCustomer);
    }
}
