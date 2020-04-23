package domain;

public class CollectionProductRepository implements ProductRepository{
    public void save(Product product) {
        Registrar.add(Product.class, product);
    }

    public Product find(String productName) {
        return (Product)Registrar.get(Product.class, productName);
    }
}
