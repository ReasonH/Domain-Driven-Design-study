package reason.domain.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reason.domain.Registrar;
import reason.domain.customer.Customer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("orderRepository")
public class OrderRepositoryImpl implements OrderRepository{
    @Autowired
    Registrar registrar;
    public void save(Order order) {
        registrar.add(Order.class, order);
    }

    public Order find(String identity) {
        return (Order) registrar.get(Order.class, identity);
    }

    public Set<Order> findByCustomer(Customer customer) {
        Set<Order> results = new HashSet<Order>();

        for(Order order : findAll()) {
            if (order.isOrderedBy(customer)) {
                results.add(order);
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public Set<Order> findAll() {
        return new HashSet<Order>((Collection<Order>)registrar.getAll(Order.class));
    }

    public Order delete(String identity) {
        return (Order)registrar.delete(Order.class, identity);
    }
}
