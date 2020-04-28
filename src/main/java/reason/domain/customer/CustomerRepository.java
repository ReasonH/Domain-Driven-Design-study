package reason.domain.customer;

public interface CustomerRepository {
    public void save(Customer customer);
    public Customer find(String identity);
}
