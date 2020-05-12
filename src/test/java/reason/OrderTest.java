package reason;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import reason.domain.*;
import reason.domain.customer.Customer;
import reason.domain.order.Order;
import reason.domain.order.OrderRepository;
import reason.domain.product.Product;
import reason.domain.product.ProductRepository;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class OrderTest {
    private Customer customer;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() throws Exception {
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
    }

    @Test
    public void testOrderPrice() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 10)
                .with("상품2", 20);
        orderRepository.save(order);
        assertEquals(new Money(110000), order.getPrice());
    }

    @Test
    public void testOrderLimitExceed() {
        try {
            customer.newOrder("CUST-01-ORDER-01")
                    .with("상품1", 20)
                    .with("상품2", 50);
            fail();
        } catch(OrderLimitExceededException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testOrderWithEqualProductsPrice() throws Exception{
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5);
        orderRepository.save(order);
        assertEquals(new Money(110000), order.getPrice());
    }

    @Test
    public void testOrderLineItems() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5);
        orderRepository.save(order);

        assertEquals(2,order.getOrderLineItemSize());
    }

    @Test
    public void testOrderIdentical() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 10)
                .with("상품2", 20);
        orderRepository.save(order);

        Order anotherOrder = orderRepository.findByOrderId("CUST-01-ORDER-01").orElse(null);
        assertEquals(order, anotherOrder);
    }
}


