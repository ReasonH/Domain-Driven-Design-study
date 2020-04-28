package reason.domain.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reason.domain.Registrar;

@Component("customerRepository")
public class CustomerRepositoryImpl implements CustomerRepository{
    @Autowired
    Registrar registrar;

    public void save(Customer customer) {
        registrar.add(Customer.class, customer);
    }
    public Customer find(String identity) {
        return (Customer) registrar.get(Customer.class, identity);
    }
}