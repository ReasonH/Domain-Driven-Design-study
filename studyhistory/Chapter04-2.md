## ORM과 투명한 영속성 - ORM과 영속성
[전체 목차로](../README.md)

### 목차 <a id="0"></a>
1. 객체 관계 맵핑과 도메인 모델
2. [영속성 관리 REPOSITORY](#2)

### 객체 관계 맵핑과 도메인 모델

상태와 행위를 함께 갖는 풍부한 객체 모델로 도메인 레이어를 구성하는 것을 DOMAIN MODEL 패턴이라고 한다. DOMAIN MODEL 패턴은 상속, 캡슐화, 다형성 등 객체 지향의 장점을 십분 활용함으로써 수정이 용이하고 확장성이 높으며 이해하기 쉬운 시스템을 개발할 수 있도록 한다. 그러나 이 패턴은 객체 지향의 모든 특징을 활용하기에 영속성 메커니즘을 주도하는 RDB와의 임피던스 불일치 문제가 발생한다. 

이를 해결하는 가장 좋은 방법은 도메인 로직을 처리하는 도메인 레이어와 영속성 로직을 처리하는 퍼시스턴스 레이어 간 불일치를 조정하는 중간 레이어를 도입하는 것이다. 중간 레이어는 불일치 사항들을 조정함으로써 객체와 테이블이 독립적으로 발전하도록 돕는다. 이처럼 객체와 RDB 간 독립성을 보장할 수 있도록 객체와 DB 테이블 간의 데이터를 이동시키는 객체를 DATA MAPPER라고 한다.

#### ORM
DATA MAPPER를 구현한 SW를 ORM이라 한다. ORM은 객체와 DB 테이블 간 매핑 정보를 XML 파일이나 Java5 Annotation 등 메타 데이터로 표현할 수 있도록 Metadata Mapping을 지원한다. 도메인 객체 자체는 자신이 DB에 저장된다는 사실 자체를 알지 못한다. 객체가 DB에서 조회된 정보를 저장하고 있는지, 메모리에 임시로 생성된 일시적 상태를 표현하고 있는지와 무관하게 도메인 로직 작성이 가능하다. 

도메인 레이어는 메모리에 생성된 객체 그래프를 대상으로 로직을 실행한다. 이 과정에서 하부의 영속성 메커니즘이 개입하지만 도메인 레이어의 객체들은 이 영속성 메커니즘에 대해 투명하다. 이처럼 도메인 객체가 하부의 영속성 메커니즘에 독립적인 특징을 **투명한 영속성**이라 한다.

#### IDENTITY MAP
ORM은 동일 트랜잭션 내에서 동일한 객체들이 한번만 로드될 수 있도록 하기 위해 IDENTITY MAP을 유지한다. 
객체를 로드하라는 요청을 받으면 
1. ORM은 내부의 IDENTITY MAP을 조사한다.
2. IDENTITY MAP에 존재하지 않으면 DB로부터 로드하여 추가한다.
3. IDENTITY MAP에 존재할 경우 DB에 요청 없이 저장된 객체를 반환한다.

IDENTITY MAP을 사용하면 동일 트랜잭션 내 객체들을 캐싱함으로써 성능 향상과 동시에 자동으로 트랜잭션의 **REPEATABLE READ(반복 가능한 읽기) 격리 레벨 (고립 수준) 특성**을 얻을 수 있다. 또한 동일한 트랜잭션 내에서는 항상 동일한 객체가 반환되기 때문에 DB 식별자가 동일한 객체의 경우 객체 식별자도 항상 동일하게 유지시켜 준다. 즉, 트랜잭션 범위 내에서는 == 연산자와 equals() 메소드의 결과가 동일하다.

일반적으로 IDENTITY MAP은 UNIT OF WORK 내에 위치한다. 트랜잭션 확약 시에 UNIT OF WORK는 IDENTITY MAP에 저장된 모든 객체들의 변경 상태를 확인한 후 모든 외래 키 제약 조건을 위반하지 않으면서도 가장 효율적인 SQL문 조합을 생성한다. 이를 **transactional write-behind** 라고한다.

#### ORM과 LAZY LOADING

객체들이 최초 로드될 때 DB 접근 코드는 객체 그래프의 어떤 부분들까지 사용될지 알 수 없다. 따라서 접근되지 않을 수도 있는 객체들을 모두 로딩하는 것은 매우 비효율적이며 성능에 문제를 일으킬 것이다. 따라서 최초 필요 객체만을 로드한 후 나머지 객체들은 연관 관계 그래프 탐색을 통해 필요 시점에 로딩하는 것이 효율적일 것이다. 이처럼 객체를 필요한 시점이 DB로부터 로드하는 것을 **LAZY LOADING**이라 한다.

주문 시스템의 Order AGGREGATE를 살펴보자. AGGREGATE의 정의에 따라 Order AGGREGATE 내에서 전역으로 접근할 수 있는 객체는 EP인 Order 뿐이다. 따라서 OrderRepository를 통해 Order만을 로드하고 ORderLineItem은 필요 시 Order로부터의 탐색(항해)을 통해서만 접근 가능하다. 

![](http://pds15.egloos.com/pds/200906/15/18/f0081118_4a35ce5926415.jpg)

이것을 ORM 관점에서 살펴보면 OrderRepository는 DB로부터 Order 객체만을 로드하고 이를 UNIT OF WORK 내의 IDENTITY MAP에 추가한다. 후에 Order로부터 OrderLineItem으로 연관을 통해 탐색이 일어날 경우 ORM은 LAZY LOADING 기법을 사용하여 OrderLineItem을 로딩한다. 따라서 AGGREGATE와 EP 별 REPOSITORY 할당의 배후에는 구현 기술로서의 LAZY LOADING이 위치하고 있다. 

#### 영속성 메커니즘

고객이 새로운 주문을 입력했다고 가정한다. 시스템 내부에서는 새로운 트랜잭션이 시작되고 Order 객체와 OrderLineItem이 생성된 후 Order에 추가된다. Order 객체가 생성은 되었지만 DB와 관계가 맺어지지 않은 상태를 **비영속 상태**라 한다. 어플리케이션은 트랜잭션 종료 시점에 OrderRepository save()를 호출하여 생성된 Order를 DB에 저장한다. 이 때부터 Order는 DB 테이블의 한 레코드로 저장되는데 이처럼 DB와 연관 관계를 가지고 있는 상태를 **영속 상태**라 한다.

앞에서는 OrderRepository를 통해 Order 만을 저장했을 뿐 OrderLineItem에 대한 영속성 로직은 처리하지 않았다(역: 앞의 save()함수는 단순 Order만을 저장함). 이 경우 Order만 저장되고 OrderLineItem은 DB에 저장되니 않을까? ORM은 **영속성 전이**라는 특징을 지원한다. 영속성 전이는 영속 객체와 연관된 객체들에 연속성이 전이된다는 것을 의미한다. 따라서 영속 객체인 Order와 연관된 OrderLineItem 역시 영속 객체가 되며 트랜잭션 확약 시 자동으로 DB에 저장된다. 삭제의 경우 또한 마찬가지다. 영속 객체인 Order가 삭제될 경우 연관 관계로 묶인 OrderLineItem 역시 삭제된다. 이는 AGGREGATE의 불변식을 보장하기 위해 AGGREGATE 전체가 하나의 단위로 처리해야 하고 AGGREGATE 내부 객체들이 EP의 생명주기에 종속된다는 개념을 지원한다. 즉, AGGREGATE의 생명주기와 관련된 제약사항의 구현 메커니즘으로 ORM의 영속성 전이를 사용할 수 있다.

**도달 가능성에 의한 영속성**은 어떤 영속 객체로부터 도달 가능한 모든 객체들이 영속 객체가 된다는 것을 의미한다. 즉, 어떤 객체가 영속 객체라면 연관된 객체는 무조건 영속 객체가 된다. 일반적인 ORM은 이를 완전히 지원하지 않지만 영속성을 전이 시킬지를 매핑 시 설정하게 하여 더 세밀한 제어가 가능하도록 한다. 
- 주의: ORM 사용시, 영속 객체와 연관된 객체라고 해서 무조건 함께 저장되거나 삭제되지 않는다.

#### VO와의 매핑
ORM과 관련하여 살펴볼 마지막 이슈는 VO와의 매핑이다. VO는 식별자를 가지지 않으며 Entity의 생명주기에 종속된다. VO는 속성 값이 같은 경우 동등한 것으로 판단한다. 일반적으로 Entity가 별도의 테이블로 매핑되는 반면 VO는 자신이 속한 Entity의 단순 컬럼으로 매핑된다. 이를 **Embedded Value** 패턴이라 한다.

![](http://thumbnail.egloos.net/600x0/http://pds13.egloos.com/pds/200906/23/18/f0081118_4a404dbeb02d2.jpg)

주문 시스템에서 Product Entity를 PRODUCTS 테이블에 매핑하는 경우를 살펴 보자. Product는 상품의 가격을 나타내기 위해 VO인 Money를 속성으로 포함한다. 이 경우 VO인 Money를 별도의 테이블로 매핑하지 않고 PRODUCTS 테이블의 한 컬럼으로 매핑한다.

### Hibernate ORM <a id = "2"></a>
[목차로](#0)

Hibernate는 투명한 영속성을 지원하는 오픈소스 ORM으로 Java ORM의 표준이다. Hibernate는 EJB 3.0의 엔티티 빈 스펙인 JPA에 큰 영향을 끼쳤으며 JBoss의 엔티티빈 구현체로 포함되어 있다. Spring Framework는 Hibernate를 통합하기 편리하도록 여러가지 지원 클래스들을 제공한다. 주문 시스템 역시 Hibernate를 적용하기 위해 Spring 지원 클래스를 사용할 것이다.

![](http://pds15.egloos.com/pds/200906/29/18/f0081118_4a4836c01df52.jpg)

위는 주문 시스템의 도메인 모델과 매핑될 데이터 모델이다.

우선 Product와 PRODUCTS 테이블 간 매핑을 살펴본다. 지금까지는 EntryPoint라는 Layer Supertype을 상속받아 구현했다. EntryPoint는 Registrar 클래스가 도메인 객체를 클래스별로 관리할 수 있도록 강제하기 위해 추가된 상위 클래스이다. 따라서 지금까지의 도메인 모델은 Registrar라는 인프라 스트럭쳐와 결합되어 있었다. 이처럼 하위 인프라 스트럭쳐에 의해 사용되는 클래스나 인터페이스에 도메인 레이어의 클래스들이 의존하는 것을 침투적이라 한다.

Spring과 Hibernate는 비침투적이다. 비침투적인 Framework를 사용할 경우 도메인 클래스들은 하위 인프라스트럭처에 독립적이다. 즉, 프레임워크의 특정 인터페이스를 구현하거나 클래스를 상속받을 필요가 없다. 우리가 궁극적으로 지향하는 순수한 객체 지향 모델로 도메인 모델을 구성할 수 있는 것이다.

> 이후 진행되는 Metadata Mapping 실습은 원문에서는 XML을 통해 진행했습니다. 그러나 여기서는 편의성을 위해 JPA를 사용하여 진행했으며 코드 또한 JPA에 알맞게 변경했습니다.

#### Order class 개선

Order 클래스와 EntryPoint 간의 상속 관계를 제거하자. 이제 Order는 어떤 하위 인프라 스트럭처에도 의존하지 않는 순수한 객체이다. 이처럼 어떤 인프라스트럭쳐 코드에도 의존하지 않는 순수한 Java 객체를 POJO (Plain Old Java Object)라고 한다. 주문 Entity 추적성을 보장하기 위해서는 Order 클래스에 IDENTITY FEILD를 추가해야 한다. 즉, ORDERS 테이블의 주 키를 Order 클래스의 속성으로 포함시켜야 한다. 주 키와 매핑될 Long 타입의 id 속성을 추가한다.

##### `Order.java`
~~~ java
@NoArgsConstructor
@Getter
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderLineItem> lineItems = new HashSet<OrderLineItem>();

    @ManyToOne
    private Customer customer;

    public static Order order(String orderId, Customer customer) {
        return new Order(orderId, customer);
    }

    Order(String orderId, Customer customer) {
        this.orderId = orderId;
        this.customer = customer;
    }
    ...
~~~

EntryPoint로부터의 상속 관계가 제거되었으며 id를 반환하기 위한 getter 메소드가 추가되었다. id를 반환하는 getter 메소드를 추가한 이유는 대부분의 엔터프라이즈 어플리케이션이 프레젠테이션 레이어에서 해당 도메인 객체의 id를 필요로 하기 때문이다.

다음으로 equals()와 hashCode()를 구현한다. equals()와 hashCode()는 대리 키 대신 자연 키를 비교해야 한다. Order를 유일하게 식별하면서도 잘 변하지 않는 값은 orderId라는 것을 알 수 있다.

~~~ java
    public boolean equals(Object object){
        if (object == this){
            return true;
        }

        if (!(object instanceof Order)){
            return false;
        }

        final Order other = (Order)object;
        return this.orderId.equals(other.getOrderId());
    }

    public int hashCode(){
        return this.orderId.hashCode();
    }
~~~
equals()에서 파라미터로 전달된 객체의 orderId를 비교하지 않고 별도의 getOrderId() 메소드를 호출한 이유는 비교 대상으로 Proxy 객체가 전달될 수도 있기 때문이다. 따라서 equals()에서의 비교를 위해서는 getter를 호출하여 Lazy loading이 일어나도록 해야한다. 도메인 모델이 하부 인프라스트럭처에 완전히 독립적일 수 없다는 것이 바로 이런 이유 때문이다. ORM으로 Hibernate를 쓰는 경우 Hibernate에 의한 제약사항이 도메인 모델 구현에 미세하게 영향을 미친다. 그러나 self-encapsulation은 좋은 습관이기에 지키면 좋다.

#### OrderLineItem 개선

##### `OrderLineItem.java`
~~~ java
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
~~~
OrderLineItem에도 Long 타입의 IDENTITY FIELD를 추가한다. @Configurable Annotation은 Spring 컨테이너 외부에서 생성되는 객체에 Spring 컨테이너에서 선언된 빈을 의존 삽입하기 위해 사용된다. 여기서는 Hibernate가 생성하는 OrderLineItem 객체에 ProductRepository 타입의 빈을 의존 삽입하기 위해 사용되고 있다.

~~~ java
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
~~~

OrderLineItem은 Order의 HashSet에 저장되기 때문에 equals()와 hashCode()를 반드시 오버라이딩 해야한다. 하나의 Order 내에는 하나의 Product에 대해 하나의 OrderLineItem만이 존재해야 한다는 도메인 규칙이 있으므로 Product와 quantity를 사용하여 비교를 수행하면 된다. OrderLineItem 역시 LazyLoading 문제를 방지하기 위해 getQuantity() 메소드를 사용한다.

#### Product 개선

##### `Product.java`
~~~java
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
~~~

Product는 VALUE OBJECT인 Money를 속성으로 포함한다. 앞에서 설명한 바와 같이 VO는 별도의 테이블로 맵핑되지 않고 의존하는 ENTITY가 맵핑되는 테이블의 컬럼으로 맵핑된다. Hibernate는 component를 사용하여 VO의 개념을 지원한다. 맵핑 시 엘리먼트는 PRODUCTS 테이블의 PRICE 컬럼의 값을 Money 클래스의 amount 속성에 맵핑항 후 이를 Product 클래스의 price 속성에 설정한다.

---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 5부](http://aeternum.egloos.com/1524214)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 6부](http://aeternum.egloos.com/1533526)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 7부](http://aeternum.egloos.com/1540234)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 8부](http://aeternum.egloos.com/1555909)  
