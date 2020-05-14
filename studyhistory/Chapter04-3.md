## ORM과 투명한 영속성 - 마무리
[전체 목차로](../README.md)

### 테스트

지금까지 처음에 구현했던 것과 달리 다양한 부분을 수정했다. 이 때 기능의 정상 작동을 보장하기 위해 의지할 수 있는 것은 회귀 테스트이다.

테스트를 실행하기 전 알아야 할 것은 단위 테스트는 가능한 DB에 의존하지 않아야 한다는 점이다. DB가 가동되지 않아도 각 클래스만을 고립시켜 테스트할 수 있도록 TC를 작성해야 한다. DB를 포함하는 테스트는 단위 테스트가 아니라 통합 테스트이다.

#### 통합 테스트에서의 문제점

DB와 연계해야 하는 통합 테스트에서 가장 어려운 문제는 테스트 데이터를 고립시키는 것이다. 우선 개발자들이 사용하는 테스트 데이터들이 충돌하지 않아야 한다. 테스트 데이터가 충돌하여 테스트가 실패한 경우 문제의 원인을 발견하기가 쉽지 않다. 

두 번째 문제는 테스트 데이터들이 DB에 입력되어 있어야 한다는 점이다. 테스트가 종료된 후에는 사용된 테스트 데이터들을 제거해야 한다. 테스트가 종료된 후에도 테스트 데이터들이 그대로 존재할 경우 다음 테스트 결과에 영향을 미칠 수 있기 때문에 테스트를 고립시키라는 기본 원칙을 위배하게 된다.

#### 해결 방안

이 문제들을 해결할 수 있는 가장 좋은 방법은 개발자 별로 개발용 DB를 가지는 것이다. 즉, 자신만의 고립된 개발환경에 샌드박스를 구성하고 개발을 진행하는 것이다. Hibernate와 같은 ORM을 사용하면 DB간 호환성 문제를 해결할 수 있기 때문에 아티클에서 사용하는 HSQLDB와 같은 가벼운 DB를 사용하여 개발자 환경에서 어플리케이션을 개발한 후 Oracle등을 사용하는 개발 서버에서도 별다른 수정 없이 어플리케이션을 실행시키는 것이 가능하다.**(역: 본 글에서는 HSQLDB를 사용하지 않습니다.)** 그러나 이것은 DBA에 의해 엄격하게 DB가 관리되는 프로젝트의 경우 적용하기 쉽지 않다.

개발자들 간 충돌을 막으면서도 테스트 데이터를 정리도 자동으로 수행하는 방법은 각 테스트 수행이 종료된 경우 트랜잭션을 롤백시키는 것이다. 즉, 테스트 실행이 시작되었을 때 자동으로 트랜잭션을 시작해서 DB 작업을 수행하고 결과를 검증한 후 트랜잭션을 롤백시킴으로써 테스트 데이터들이 자동적으로 제거되도록 하는 것이다. 각 개발자들의 테스트 로직은 트랜잭션에 의해 격리되기 때문에 상호간 영향을 미치지 않게 된다.

테스트 시작 시 트랜잭션을 자동 시작하고 종료시 자동으로 롤백시키기 위해서는 테스트 클래스가 Spring의 `AbstractTransactionalSpringContextTests`를 상속받기만 하면 된다. 

> Spring3.0+에서 Deprecated 되었으며 현재는 다양한 Annotation으로 대체되고 있습니다.

##### `변경된 OrderRepositoryTest.java`
~~~ java
@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest
public class OrderRepositoryTest {
    private Customer customer;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() throws Exception {
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
    }

    @Test
    public void testOrderCount() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5));

        orderRepository.save(customer.newOrder("CUST-01-ORDER-02")
                .with("상품1", 20)
                .with("상품2", 5));

        assertEquals(2, Objects.requireNonNull(orderRepository.findAllByCustomer(customer).orElse(null)).size());
    }

    @Test
    public void testDeleteOrder() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20));
        Order order = orderRepository.findByOrderId("CUST-01-ORDER-01").orElse(null);

        orderRepository.deleteByOrderId("CUST-01-ORDER-01");

        assertEquals(orderRepository.findByOrderId("CUST-01-ORDER-01"), Optional.empty());
        assertNotNull(order);
    }
}
~~~

1. @SpringBootTest로 통합테스트를 실시합니다.
2. @BeforeEach를 통해 각 테스트 수행 전 동일한 DB환경을 만들어줍니다.
 
![](http://thumbnail.egloos.net/600x0/http://pds17.egloos.com/pds/200910/15/18/f0081118_4ad709b55a159.jpg)

결과적으로 Repository의 구현 클래스만 변경되었을 뿐 도메인 모델에는 변함이 없다. 이것이 바로 Domain-Driven-Design, 낮은 결합도, 비침투적 인프라의 힘이다.

#### 도메인을 닮은 어플리케이션

좋은 어플리케이션은 시간이 지남에 따라 도메인과 닮아가는 어플리케이션이다. 이를 위해서는 어플리케이션의 시작부터 도메인을 염두에 두어야 한다. 실제 도메인의 용어와 개념을 차용하여 어플리케이션을 구성하고, 도메인을 추상화한 단일 모델을 통해 개발을 이끌어 나가며, 비침투적 인프라를 사용하여 도메인의 독립성을 보장하는 것이 그 출발점이 될 것이다.

DOMAIN MODEL 패턴은 도메인-어플리케이션간 표현적 차이를 최소화하기 위해 도메인 레이어를 구성하는 방법이다. 그러나 이를 위한 원칙이나 비침투적 인프라스트럭쳐가 없이 DOMAIN MODEL 패턴을 적용할 경우 프로젝트의 실패로 이어질 확률이 높다.
---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 10부](http://aeternum.egloos.com/1648727)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 11부](http://aeternum.egloos.com/1651174)  