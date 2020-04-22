package aggregate;

public class ProductRepository {
    public void save(Product product) {
        Registrar.add(Product.class, product);
    }
    public Product find(String identity) {
        return (Product) Registrar.get(Product.class, identity);
    }
}
