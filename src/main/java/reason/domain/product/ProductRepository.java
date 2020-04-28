package reason.domain.product;

public interface ProductRepository {
    public void save(Product product1);
    public Product find(String productName);
}
