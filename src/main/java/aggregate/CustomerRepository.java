package aggregate;

public class CustomerRepository {
    public void save(Customer customer) {
        Registrar.add(Customer.class, customer);
    }
    public Customer find(String identity) {
        return (Customer) Registrar.get(Customer.class, identity);
    }
}