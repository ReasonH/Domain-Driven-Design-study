package reason;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import reason.App;
import reason.domain.*;
import reason.domain.customer.Customer;
import reason.domain.customer.CustomerRepository;
import reason.domain.order.Order;
import reason.domain.order.OrderRepository;
import reason.domain.product.Product;
import reason.domain.product.ProductRepository;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class OrderRepositoryTest {
    private Customer customer;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() throws Exception {
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));
        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
        customerRepository.save(customer);
    }

    @Test
    public void testOrderCount() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5));

        orderRepository.save(customer.newOrder("CUST-01-ORDER-02")
                .with("상품1", 20)
                .with("상품2", 5));

        assertEquals(2, Objects.requireNonNull(orderRepository.findAllByCustomer(customer).orElse(null)).size());
    }

    @Test
    public void testDeleteOrder() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20));
        Order order = orderRepository.findByOrderId("CUST-01-ORDER-01").orElse(null);

        orderRepository.deleteByOrderId("CUST-01-ORDER-01");

        assertEquals(orderRepository.findByOrderId("CUST-01-ORDER-01"), Optional.empty());
        assertNotNull(order);
    }
}