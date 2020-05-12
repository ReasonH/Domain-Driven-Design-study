package reason.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import reason.domain.product.Product;
import reason.domain.product.ProductRepository;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Getter
@Configurable(autowire = Autowire.BY_TYPE, value = "orderLineItem", preConstruction = true)
public class OrderLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Autowired
    private transient ProductRepository productRepository;

    public OrderLineItem(String productName, int quantity) {
        this.product = productRepository.findByName(productName).orElse(null);
        this.quantity = quantity;
    }

    public boolean isProductEqual(OrderLineItem lineItem) {
        return product == lineItem.product;
    }

    public OrderLineItem merge(OrderLineItem lineItem) {
        quantity += lineItem.quantity;
        return this;
    }

    public Money getPrice() {
        return product.getPrice().multiply(quantity);
    }

    public boolean equals(Object object){
        if (object == this){
            return true;
        }

        if (!(object instanceof OrderLineItem)){
            return false;
        }

        final OrderLineItem other = (OrderLineItem)object;
        return this.product.equals(other.getProduct()) && this.quantity == other.getQuantity();
    }

    public int hashCode(){
        int result = 17;
        result = 37*result + product.hashCode();
        result = 37*result + quantity;
        return result;
    }
}
