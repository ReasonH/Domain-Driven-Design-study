package reason.domain;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import reason.domain.product.Product;
import reason.domain.product.ProductRepository;

@Configurable(autowire = Autowire.BY_TYPE, value = "orderLineItem", preConstruction = true)
public class OrderLineItem {
    private Product product;
    private int quantity;

    @Autowired
    private ProductRepository productRepository;

    public OrderLineItem() {
    }

    public OrderLineItem(String productName, int quantity) {
        this.product = productRepository.find(productName);
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

    public Product getProduct() {
        return product;
    }
}
