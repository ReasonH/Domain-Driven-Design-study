import domain.*;
import junit.framework.TestCase;
import org.junit.Before;

import static org.junit.Assert.fail;

public class OrderTest extends TestCase {
    private Customer customer;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        Registrar.init();
        orderRepository = new OrderRepository();
        productRepository = new ProductRepository();
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
    }

    public void testOrderPrice() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 10)
                .with("상품2", 20);
        orderRepository.save(order);
        assertEquals(new Money(110000), order.getPrice());
    }

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

    public void testOrderWithEqualProductsPrice() throws Exception{
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5);
        orderRepository.save(order);
        assertEquals(new Money(110000), order.getPrice());
    }

    public void testOrderLineItems() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5);
        orderRepository.save(order);

        assertEquals(2,order.getOrderLineItemSize());
    }

    public void testOrderIdentical() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 10)
                .with("상품2", 20);
        orderRepository.save(order);

        Order anotherOrder = orderRepository.find("CUST-01-ORDER-01");
        assertEquals(order, anotherOrder);
    }
}


