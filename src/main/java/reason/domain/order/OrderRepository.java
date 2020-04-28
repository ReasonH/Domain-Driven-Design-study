package reason.domain.order;

import reason.domain.customer.Customer;

import java.util.Set;

public interface OrderRepository {
    public void save(Order order);
    public Order find(String identity);
    public Set<Order> findByCustomer(Customer customer);
    public Set<Order> findAll();
    public Order delete(String identity);
 }
