package reason.domain.customer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import reason.domain.Money;
import reason.domain.order.Order;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@NoArgsConstructor
@Getter
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String customerNumber;
    private String name;
    private String address;
    private Money limitPrice;

    public Customer(String customerNumber, String name, String address,
                    long limitPrice) {
        this.customerNumber = customerNumber;
        this.name = name;
        this.address = address;
        this.limitPrice = new Money(limitPrice);
    }

    public Order newOrder(String orderId) {
        return Order.order(orderId, this);
    }

    public boolean isExceedLimitPrice(Money money) {
        return money.isGreaterThan(limitPrice);
    }
}