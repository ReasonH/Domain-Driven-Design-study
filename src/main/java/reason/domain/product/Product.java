package reason.domain.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import reason.domain.Money;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Money price;

    private String name;

    public Product(String name, long price) {
        this.name = name;
        this.price = new Money(price);
    }

    public Product(String name, Money price) {
        this.name = name;
        this.price = price;
    }
}