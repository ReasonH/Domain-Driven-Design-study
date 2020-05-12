package reason.domain.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import reason.domain.Money;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@NoArgsConstructor
@Getter
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
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