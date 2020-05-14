package reason.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import reason.domain.customer.Customer;

import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    Optional<Set<Order>> findAllByCustomer(Customer customer);
    void deleteByOrderId(String orderId);
}
