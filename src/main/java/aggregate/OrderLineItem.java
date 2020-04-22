package aggregate;

import java.math.BigDecimal;

public class OrderLineItem {
    private Product product;
    private int quantity;

    private ProductRepository productRepository = new ProductRepository();

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
