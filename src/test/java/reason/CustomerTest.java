package reason;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reason.lifecycle.Customer;
import reason.lifecycle.Registrar;

import static org.junit.jupiter.api.Assertions.assertSame;

public class CustomerTest {
    @BeforeEach
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
