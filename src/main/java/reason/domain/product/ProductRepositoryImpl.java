package reason.domain.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reason.domain.Registrar;

@Component("productRepository")
public class ProductRepositoryImpl implements ProductRepository{

    @Autowired
    private Registrar registrar;

    public void save(Product product) {
        registrar.add(Product.class, product);
    }

    public Product find(String productName) {
        return (Product)registrar.get(Product.class, productName);
    }
}
