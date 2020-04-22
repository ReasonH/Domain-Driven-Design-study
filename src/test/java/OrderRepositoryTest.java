import aggregate.*;
import junit.framework.TestCase;

public class OrderRepositoryTest extends TestCase {
    private Customer customer;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    public void setUp() throws Exception {
        Registrar.init();
        orderRepository = new OrderRepository();
        productRepository = new ProductRepository();
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
    }

    public void testOrdreCount() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5));

        orderRepository.save(customer.newOrder("CUST-01-ORDER-02")
                .with("상품1", 20)
                .with("상품2", 5));

        assertEquals(2, orderRepository.findByCustomer(customer).size());
    }
}