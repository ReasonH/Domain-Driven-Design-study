package reason.domain.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import reason.domain.Money;
import reason.domain.OrderLimitExceededException;
import reason.domain.OrderLineItem;
import reason.domain.customer.Customer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderLineItem> lineItems = new HashSet<OrderLineItem>();

    @ManyToOne
    private Customer customer;

    public static Order order(String orderId, Customer customer) {
        return new Order(orderId, customer);
    }

    Order(String orderId, Customer customer) {
        this.orderId = orderId;
        this.customer = customer;
    }


    public boolean equals(Object object){
        if (object == this){
            return true;
        }

        // object가 참조하는 인스턴스의 실제 타입 검사
        if (!(object instanceof Order)){
            return false;
        }

        final Order other = (Order)object;
        return this.orderId.equals(other.getOrderId());
    }

    public int hashCode(){
        return this.orderId.hashCode();
    }

    public Order with(String productName, int quantity) throws OrderLimitExceededException {
        return with(new OrderLineItem(productName, quantity));
    }

    private Order with(OrderLineItem lineItem) throws OrderLimitExceededException {
        if (isExceedLimit(customer, lineItem)) {
            throw new OrderLimitExceededException();
        }

        for(OrderLineItem item : lineItems) {
            if (item.isProductEqual(lineItem)) {
                item.merge(lineItem);
                return this;
            }
        }

        lineItems.add(lineItem);
        return this;
    }

    private boolean isExceedLimit(Customer customer, OrderLineItem lineItem) {
        return customer.isExceedLimitPrice(getPrice().add(lineItem.getPrice()));
    }

    public Money getPrice() {
        Money result = new Money(0);

        for(OrderLineItem item : lineItems) {
            result = result.add(item.getPrice());
        }
        return result;
    }

    public int getOrderLineItemSize() {
        return lineItems.size();
    }

    public boolean isOrderedBy(Customer customer) {
        return this.customer == customer;
    }
}