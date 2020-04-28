package reason;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContext;
import reason.App;
import reason.domain.*;
import reason.domain.customer.Customer;
import reason.domain.order.Order;
import reason.domain.order.OrderRepository;
import reason.domain.product.Product;
import reason.domain.product.ProductRepository;

import static org.junit.jupiter.api.Assertions.*;

@TestComponent
public class OrderRepositoryTest {
    @Autowired
    private Customer customer;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Registrar registrar;

    public void setUp() throws Exception {
        registrar.init();
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);

    }

    public void testOrderCount() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5));

        orderRepository.save(customer.newOrder("CUST-01-ORDER-02")
                .with("상품1", 20)
                .with("상품2", 5));

        assertEquals(2, orderRepository.findByCustomer(customer).size());
    }

    public void testDeleteOrder() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20));
        Order order = orderRepository.find("CUST-01-ORDER-01");

        orderRepository.delete("CUST-01-ORDER-01");

        assertNull(orderRepository.find("CUST-01-ORDER-01"));
        assertNotNull(order);
    }
}