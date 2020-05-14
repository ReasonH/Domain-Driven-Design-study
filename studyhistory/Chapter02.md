## Aggregate와 Repository
[전체 목차로](../README.md)

### 목차 <a id="0"></a>
1. 비즈니스 책임과 동시성
2. [AGGREGATE](#2)
3. [Fluent Interface](#3)
4. [Entry Point와 Repository](#4)

#### 개요

![](http://pds12.egloos.com/pds/200811/20/18/f0081118_4924fee94926b.jpg)

- 고객은 시스템을 사용해 상품을 주문한다.
- 한 번 주문시 다수의 상품을 구매할 수 있다.
- 상품에 대한 이름, 가격과 같은 기본 정보는 별도의 상품 클래스에 정의되어 있다.
- 고객은 고객 등급에 따라 일 회 주문 시 구매 가능한 금액에 제한을 받는다.

여기에서 상대적으로 중요도가 낮은 금액(VO)는 프로퍼티로 표현한다. 주문 도메인의 핵심 주체는 고객, 주문 항목, 주문, 상품이기 때문이다. 이것이 경제적이고 의미 전달이 명확한 좋은 모델이다. 다음은 상대적으로 비경제적인 모델이다.

![](http://pds10.egloos.com/pds/200811/20/18/f0081118_4924ff3ab76fd.jpg)

일회 주문시 구매 가능 금액에 제한을 받는 것은 비즈니스 룰로 볼 수 있다. 이 책임은 어떤 도메인에 할당해야 할까?

### 비즈니스 책임과 동시성

#### 한도액 검증 책임이 고객 객체에 할당되는 경우
1. 고객 객체가 주문 객체의 내부 상태를 알고 있어야 한다. 
    - 양방향 연관 발생
    - 도메인 모델 간 결합도의 증가
    - 관계 간 일관성 유지에 필요한 구현 복잡도 증가
2. 기능에 대한 욕심 문제 발생
    - 메소드가 자신의 내용보다 다른 클래스 내용에 더 관심을 가짐
    - 객체 간 결합도의 증가
    - 정보를 가진 클래스에 책임을 할당하라는 *INFORMATION EXPERT* 패턴 위배

#### 한도액 검증 책임이 주문 객체에 할당되는 경우
1. 주문 객체는 고객 객체의 한도액 프로퍼티를 알아야 한다.
    - 이미 연관관계가 설정되어 있기에 양방향 연관 없이 검증 가능
    - *LOW COUPLING* 및 *HIGH COHESION* 패턴 준수

즉, 한도액 검증 책임은 주문 객체에 할당되는 것이 적절하다.
다음은 시나리오를 생각해보자

#### 주문에 주문 항목을 추가하는 시나리오
1. 고객은 상품을 선택하고 상품 개수를 입력한다.
2. 시스템은 상품과 개수를 가진 주문 항목을 생성하고 주문에 추가한다.
3. 주문은 새로 추가된 주문 항목의 가격을 더한 주문 총액과 구매 고객의 한도액을 비교한다.
4. 한도를 초과한다면 예외를 발생시키고 주문 프로세스를 중단시킨다.

이 시나리오에서 주문 시 한도액을 검증하기 위해서는 **주문 객체와 주문 항목** 객체 간의 협력이 필요하며 상태 변경 시 하나의 단위로 취급되어야 한다는 것을 알 수 있다. 즉, 주문 객체와 주문 항목 객체들은 **구매액이 고객의 주문 한도액을 초과할 수 없다는 불변식을 공유하는 하나의 논리적 단위**라고 할 수 있다.

위의 불변식을 유지하기 위해서는 주문에 주문 항목이 추가된 이후에 외부에서 직접적으로 주문 항목을 수정할 수 없도록 해야한다. 즉, 주문 항목을 임의 변경할 수 있다면 한도액에 관한 불변식 유지가 불가능하다. 따라서 주문 항목은 외부에 노출되지 않아야 하며 주문 항목의 추가, 수정, 삭제는 반드시 주문의 제어 하에 수행되어야 한다. 주문은 주문 항목을 캡슐화한다.

#### 상품 객체: 동시성과 일관성 문제

주문과 주문 항목이 하나의 클러스터로 결정됐다면 상품은 이 클러스터에 포함되어야 할까 외부 객체로 보아야 할까? 이 문제는 컨텍스트를 다중 사용자 환경으로 확장해보면 알 수 있다.
(역: 고객이 여러명인 상황이 아니다. 하나의 주문 메커니즘 안에서 주문 목록을 수정하려는 사용자가 여러명이라고 생각하면 된다. -> EX. 동일한 11번가 계정에 두명이 접근해서 장바구니 목록을 수정 중)

![](http://thumbnail.egloos.net/600x0/http://pds11.egloos.com/pds/200811/20/18/f0081118_4924ff7f6cc87.jpg)

위는 주문의 초기 상태이다.

![](http://thumbnail.egloos.net/600x0/http://pds10.egloos.com/pds/200811/20/18/f0081118_4924ffa15cec9.jpg)

위는 초기 상태에서 첫 번째 사용자가 주문을 수정한 결과**1**이다.

![](http://thumbnail.egloos.net/600x0/http://pds10.egloos.com/pds/200811/20/18/f0081118_4924ffd62e081.jpg)

위는 초기 상태에서 두 번째 사용자가 주문을 수정한 결과**2**이다.

![](http://thumbnail.egloos.net/600x0/http://pds11.egloos.com/pds/200811/20/18/f0081118_492500018ea48.jpg)

위는 **1**과 **2**가 단일 프로세서 시스템의 별도 쓰레드로 처리되는 상황에서 발생할 수 있는 시나리오이다.
1. 첫 번째 사용자의 쓰레드가 불변식 검증 통과
2. 두 번째 사용자의 쓰레드가 불변식 검증 통과
3. 첫 번째 사용자의 쓰레드가 주문 내역 반영
4. 두 번째 사용자의 쓰레드가 주문 내역 반영
5. 무결성이 깨지게됨 (주문 한도액 초과)

#### LOCK과 AGGREGATE

이런 불변식 위반을 방지하기 위해서는 주문-주문 항목 클러스터에 대한 배타적 접근이 가능해야 한다. 이를 위해 주문에 대한 lock 설정이 가능해야 한다.
첫 번째 사용자가 Order를 얻을 때 두 번째 사용자는 이를 얻을 수 없으며 우회해서 주문 항목에 접근하는 경우도 없어야 한다.
- 따라서 주문 항목은 항상 주문을 통해서만 접근 가능해야 한다.

주문 항목과 연관된 상품 객체는 어떻게 처리해야 할까? 

주문 항목은 각 주문의 일부이며 하나의 주문에 의해서만 참조되기 때문에 주문과 함께 잠기더라도 시스템 성능에 영향을 미치지 않는다. 그러나 상품은 하나 이상의 주문에 의해 참조된다. 따라서 주문을 잠글 때마다 연결된 모든 상품을 잠근다면 해당 상품에 접근하려는 모든 주문 객체가 동시에 대기 상태로 빠지게 된다.
- 주문, 주문 항목과 달리 상품은 높은 빈도의 경쟁이 발생하는 객체이다.

주문과 주문 항목이 변경되는 빈도는 상품의 변경 빈도에 비해 상대적으로 매우 낮다. 즉, 주문과 주문 항목이 빈번하게 생성, 수정, 삭제되는데 비해 상품의 명칭, 가격의 수정 및 신규 상품 추가-삭제는 빈번하게 발생하지 않는다. 주문과 주문 항목의 수정이 거의 비슷한 시점에 발생하는데 비해 상품의 수정 시점은 주문, 주문 항목의 수정 시점과 무관하다.

따라서, 주문과 주문 항목은 하나의 객체 클러스터를 구성하며 고객, 상품은 주문 클러스터에 존재하지 않는 독립 객체로 존재한다.

이처럼 변경에 대한 불변식을 유지하기 위해 하나의 단위로 취급되면서 변경 빈도가 비슷하고, 동시 접근에 대한 잠금 단위가 되는 객체 집합을 AGGREGATE라고 한다.

![](http://pds11.egloos.com/pds/200811/20/18/f0081118_492500615afbc.jpg)

---
### AGGREGATE <a id="2"></a>
[목차로](#0)
#### 루트 (Entry point)
AGGREGATE는 데이터 변경 시 하나의 단위로 취급할 수 있는 연관 객체들의 클러스터이다. 이는 루트와 경계를 가진다. 
1. 경계는 AGGREGATE 내부에 무엇이 포함되어야 하는지 정의한다. 
2. 루트는 AGGREGATE내에 포함된 하나의 Ref Obj이다.
3. 루트는 외부에서 참조 가능한 유일한 AGGREGATE의 내부 객체이다.
4. AGGREGATE의 내부 객체는 외부 객체를 자유롭게 참조할 수 있다.
5. 루트를 제외한 나머지 Ref Obj들은 외부로부터 접근이 불가능하기 때문에 **지역 식별자**를 가진다.
6. 지역 식별자는 AGGREGATE 내부에서 Ref Obj를 식별하기 위한 용도로만 사용된다.
7. 루트는 **전역 식별자**를 가진다.

(P.S:루트 = EP) 
EP는 객체 그룹 항해를 위한 시작 위치를 제공한다.

#### AGGREGATE 패턴 규칙
Eric Evans가 정의하는 AGGREGATE 패턴의 규칙은 다음과 같다.
1. EP는 전역 식별자를 가지며 궁극적으로 불변식을 검증하는 책임을 갖는다.
2. AGGREGATE 내부에 속한 Ref Obj들은 지역 식별자를 가지며, 지역 식별자는 AGGREGATE 내부에서만 유일하다.
3. AGGREGATE 경계 외부에 있는 어떤 객체도 EP외의 AGGREGATE 내부 객체를 참조할 수 없다.
4. EP는 내부에 속한 Ref Obj를 외부 전달할 수 있지만 이를 전달받은 객체는 일시적으로만 사용하며 참조를 유지하지 않는다.
5. EP는 VO에 대한 복사본을 다른 객체에게 전달할 수 있으며 VO에 발생하는 일은 신경쓰지 않는다.
6. EP만이 Repository로부터 직접 얻어질 수 있다. 다른 객체들은 EP로부터의 연관 관계 항해를 통해서만 접근 가능하다.
7. AGGREGATE 내부의 객체들은 다른 AGGREGATE의 EP를 참조할 수 있다.
8. 삭제 오퍼레이션은 AGGREGATE 내 모든 객체를 제거해야한다. (GC가 있을 경우 EP 삭제 시 수반되는 모든 객체가 삭제됨)
9. AGGREGATE 내부의 어떤 객체에 대한 변경이 확약되면 전체 AGGREGATE에 대한 모든 불변식이 만족되어야 한다.

여기에서 눈 여겨 봐야할 부분은 여섯 번째 규칙이다. 이 규칙은 주문 도메인 예에서 살펴본 AGGREGATE 식별 규칙에 다음 한 가지 규칙을 추가한다.

**어떤 Ref Obj가 다른 객체에 대해 독립적으로 얻어져야 한다면 이 Ref Obj를 중심으로 AGGREGATE 경계를 식별하고 해당 Ref Obj를 EP로 정한다.**

#### 도메인 적용
시스템 내의 모든 주문이 특정 고객 객체를 얻은 후에만 접근할 수 있다면 주문과 주문 항목은 고객 객체를 EP로 하는 AGGREGATE의 일부가 되어야 한다. 그러나 고객과 무관하게 주문에 직접 접근해야 할 필요가 있다면 주문을 EP로 하는 AGGREGATE를 만드는 것이 좋다.
Ex) 특정 일자에 발생한 모든 주문을 조회해야 한다는 요구사항이 존재

AGGREGATE와 EP 역시 시스템의 복잡도를 낮춰주는 기법이다. AGGREGATE를 정의하여 불변식을 공유하는 도메인 클래스들의 클러스터에 집중할 수 있다. 구현에 있어서는 동시성 컨텍스트 하에서 일관성 유지를 위해 도메인 클래스에 잠금 전략을 적용할 수 있는 위치를 제공하며 높은 경합 지점의 식별을 통해 전반적 성능 향상을 꾀할 수 있는 기초 자료가 된다.

REPOSITORY는 AGGREGATE의 EP에 대해서만 할당한다. REPOSITORY는 객체 그래프에 대한 무분별한 접근을 제어한다. 즉, AGGREGATE는 REPOSITORY를 통해 직접 접근 / 간접 접근 도메인 객체를 구별하여 효율적인 객체 항해 지침을 제공한다.

AGGREGATE, EP, REPOSITORY는 유요한 분석기법임과 동시에 도메인 객체에 대한 관점을 메모리 컬렉션에서 엔터프라이즈 어플리케이션 환경으로 자연스럽게 이어주는 기법이다. 

![](http://thumbnail.egloos.net/600x0/http://pds11.egloos.com/pds/200811/23/18/f0081118_4928dae8d3c79.jpg)

이는 AGGREGATE와 EP를 결정한 후 REPOSITORY를 추가한 주문 도메인이다. 주문에 대한 불변식이 AGGREGATE에 추가되었다.

---
### Fluent Interface <a id="3"></a>
[목차로](#0)

여기서는 도메인 로직을 개발한다. 테스트 주도 개발 방식을 사용하며 이를 통해 시나리오를 실행하기 위해 어떤 객체의 어떤 오퍼레이션을 어떤 순서로 호출하는 것이 효율적인지 결정한다. 이를 통해 테스트를 작성함과 동시에 사용하기 편리한 인터페이스를 설계하게 된다.

#### FLUENT INTERFACE란?

전통적인 방식의 객체 인터페이스에서 객체의 상태를 변경하는 setting 메소드는 반환형을 void로 설정한다. 이 방식은 C++, C#, java 등 void 타입이 지원되는 정적 타입 언어에서 널리 사용되는 방식으로 상태 변경 메소드와 상태 조회 메소드를 명시적 분리해야 한다는 COMMAND-QUERY SEPARATION 원칙을 따른다.

FLUENT INTERFACE는 COMMAND-QUERY SEPARATION 원칙은 위배하지만 읽기 쉽고 사용하기 편리한 객체 인터페이스를 설계할 수 있도록 한다. FLUENT INTERFACE는 Method Chaining 스타일에 기반을 두어 메소드를 호출한 후 반환된 객체를 사용해 연속적으로 다른 메소드 호출이 가능하다. java에서 Method Chaining 스타일을 가장 빈번히 사용하는 경우는 내부 구조가 복잡한 복합 객체를 생성하는 경우이다.

Method Chaining 스타일을 도메인 객체 인터페이스의 설계에 적용한 것이 FLUENT INTERFACE 방식이다. 도메인 모델에서 FLUENT INTERFACE를 사용하기에 적절한 경우는 AGGREGATE 내부를 생성하는 단계가 간단하지 않지만 BUILDER 등 별도의 FACTORY 객체를 도입할 경우 불필요한 복잡성이 생기는 경우이다. 우리의 주문 도메인에서는 주문 AGGREGATE를 생성하기 위해 FLUENT INTERFACE 스타일을 사용한다.

#### 테스트 작성
다음은 주문 처리를 테스트 하기 위한 클래스 및 setup이다.

##### `reason.OrderTest.java`
~~~ java
import org.junit.Before;

public class reason.OrderTest extends TestCase {
    private Customer customer;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        Registrar.init();
        orderRepository = new OrderRepository();
        productRepository = new ProductRepository();
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
    }
}
~~~

테스트 코드를 작성하며 도메인 객체에게는 의미가 명확한 오퍼레이션을 할당하도록 노력한다. 오퍼레이션은 구현 전략, 알고리즘과 독립적으로 오퍼레이션을 호출할 사용자의 사용 의도에 적합한 이름을 가져야 한다. 즉, 오퍼레이션 내부 구현이나 컴퓨터의 관점이 아닌 클라이언트 관점을 반영해야 한다. INTENTION-REVEALING NAME 패턴을 따른 메소드의 경우 가독성이 높아진다.

우선 두 가지 상품을 주문한 후 주문의 총액을 계산하는 테스트 코드를 작성하자. 주문 AGGREGATE는 FLUENT INTERFACE 스타일을 사용하여 생성한다.
##### `reason.OrderTest.java`
~~~ java
    public void testOrderPrice() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 10)
                .with("상품2", 20);
        orderRepository.save(order);
        assertEquals(new Money(110000), order.getPrice());
    }
~~~

가격이 1000원인 '상품1'을 10개 주문하고, 가격이 5000원인 '상품2'를 20개 주문한 총액이 110,000원인지 테스트한다. Order 객체는 AGGREGATE의 EP이므로 REPOSITORY를 사용하여 등록한다. REPOSITORY는 주문이 시스템 내에 유일하게 하나만 존재하도록 제어하며 변경을 추적할 수 있도록 해준다.

Customer 클래스에 newOrder() 메소드를 추가한다.
##### `Customer.java`
~~~ java
public class Customer extends EntryPoint {
    private String customerNumber;
    private String name;
    private String address;
    private Money mileage;
    private Money limitPrice;

    public Customer(String customerNumber, String name, String address,
                    long limitPrice) {

        super(customerNumber);
        this.customerNumber = customerNumber;
        this.name = name;
        this.address = address;
        this.limitPrice = new Money(limitPrice);
    }

    public Order newOrder(String orderId) {
        return Order.order(orderId, this);
    }

    public boolean isExceedLimitPrice(Money money) {
        return money.isGreaterThan(limitPrice);
    }
}
~~~

Customer 클래스에는 고객의 주문 한도를 검증하기 위한 limitPrice 속성이 추가되었다. 또한, INFORMATION EXPERT 패턴에 따라 한도액을 검증하는 isExceedLimitPrice() 메소드를 검증하는 메소드를 Customer에 추가되었다.

newOrder() 메소드는 EP 검색에 사용될 주문 ID를 인자로 새로운 Order를 생성한다. Order는 order() CREATION method를 사용하여 Order를 생성한다.

##### `Order.java`
~~~ java
package reason.domain;

public class Order extends EntryPoint {
    private Set<OrderLineItem> lineItems = new HashSet<OrderLineItem>();
    private Customer customer;

    public static Order order(String orderId, Customer customer) {
        return new Order(orderId, customer);
    }

    Order(String orderId, Customer customer) {
        super(orderId);
        this.customer = customer;
    }
}
~~~

Order 클래스는 주문 AGGREGATE의 ENTRY POINT이므로 EntryPoint 클래스를 상속받고 검색 키로 orderId를 사용한다. order() CREATION METHOD는 Order 클래스의 생성자를 호출해서 새로운 Order 인스턴스를 생성하고 Customer와의 연관 관계를 설정한다. order() CREATION METHOD를 통해서만 객체를 생성할 수 있도록 제한하기 위해 생성자에게 default 가시성을 부여했다.

다음으로 주문 항목을 생성하는 with() 메소드를 추가한다. 주문 AGGREGATE의 생성 인터페이스에 METHOD CHAINING 스타일을 적용하기로 했으므로 with() 메소드는 this를 반환한다. Order는 주문 AGGREGATE의 ENTRY POINT이므로 주문 항목이 추가될 때마다 주문 총액이 고객의 한도액을 초과했는지 여부를 검증하는 책임을 진다.

##### `Order.java`
~~~ java
    public Order with(String productName, int quantity)
            throws OrderLimitExceededException {
        return with(new OrderLineItem(productName, quantity));
    }

    private Order with(OrderLineItem lineItem)
            throws OrderLimitExceededException {
        if (isExceedLimit(customer, lineItem)) {
            throw new OrderLimitExceededException();
        }
        lineItems.add(lineItem);
        return this;
    }

    private boolean isExceedLimit(Customer customer, OrderLineItem lineItem) {
        return customer.isExceedLimitPrice(getPrice().add(lineItem.getPrice()));
    }
~~~

with() 메소드는 제품 명과 수량을 인자로 전달 받아 OrderLineItem 인스턴스를 생성한다. 이 때 주문 AGGREGATE의 불변식을 검증하기 위해 isExceedLimit() 메소드를 호출한다. isExceedLimit() 메소드는 현재 주문 총액을 구한 후 Customer 클래스의 isExceedLimitPrice()를 호출하여 주문 가격이 고객의 한도액을 초과했는지 여부를 체크한다. isExceedLimitPrice() 메소드는 한도액 초과 시 OrderLimitExceededException을 던진다.

##### `OrderLineItem.java`
~~~ java
public class OrderLineItem {
    private Product product;
    private int quantity;

    private ProductRepository productRepository = new ProductRepository();

    public OrderLineItem(String productName, int quantity) {
        this.product = productRepository.find(productName);
        this.quantity = quantity;
    }

    public Money getPrice() {
        return product.getPrice().multiply(quantity);
    }

    public Product getProduct() {
        return product;
    }
}
~~~

OrderLineItem은 Product 클래스와 연관 관계를 가지며, 상품의 수량을 속성으로 포함한다. 생성자에 전달된 productName은 Product EP를 검색하기 위해 사용하는 키다. Product 또한 Ref Obj인 동시에 EP이기 때문에 REPOSITORY를 사용하며 이를 통해 인스턴스를 얻고 OrderLineItem의 product 속성에 할당한다. 

##### `Product.java`
~~~ java
package reason.domain;

public class Product extends EntryPoint {
   private Money price;
   private String name;

   public Product(String name, long price) {
       super(name);
       this.price = new Money(price);
   }

   public Product(String name, Money price) {
        super(name);
        this.price = price;
   }

   public Money getPrice() {
        return price;
   }

   public String getName() {
        return name;
   }
}
~~~

Product는 상품 명과 상품 가격을 알 책임을 지닌 EP로 상품 명을 검색 키로 사용한다.

##### `Order.java`
~~~ java
public Money getPrice() {
    Money result = new Money(0);

    for(OrderLineItem item : lineItems) {
        result = result.add(item.getPrice());
    }
    return result;
}
~~~

Order에 전체 주문 가격을 구할 수 있는 메소드를 추가할 수 있다. Order.getPrice()메소드는 주문 항목들의 전체 가격을 더한 금액을 반환한다.

##### `reason.OrderTest.java`
~~~ java
public void testOrderLimitExceed() {
        try {
            customer.newOrder("CUST-01-ORDER-01")
                    .with("상품1", 20)
                    .with("상품2", 50);
            fail();
        } catch(OrderLimitExceededException ex) {
            assertTrue(true);
        }
    }
~~~
고객의 주문 한도액을 초과하는 경우 with() 메소드는 OrderLimitExceededException 을 던진다.

##### `reason.OrderTest.java`
~~~ java
    public void testOrderWithEqualProductsPrice() throws Exception{
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5);
        orderRepository.save(order);
        assertEquals(new Money(110000), order.getPrice());
    }
~~~
위는 고객이 상품1을 두 번의 주문 항목으로 나누어 구매할 경우 주문 가격이 정확한지 검증하는 테스트이다. 이 테스트는 통과한다. 즉, 동일 상품을 여러 개의 주문 항목으로 나누어도 주문 총액이 정확하게 계산된다.

##### `reason.OrderTest.java`
~~~ java
    public void testOrderLineItems() throws Exception {
        Order order = customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5);
        orderRepository.save(order);

        assertEquals(2,order.getOrderLineItemSize());
    }
~~~
위는 고객이 동일 상품에 대해 별도의 주문을 수행했을 때이다. 고객은 동일한 상품을 나누어 요청하더라도 시스템은 이들을 취합하여 동일한 주문 항목으로 처리해야 한다.

##### `Order.java`
~~~ java
    public int getOrderLineItemSize() {
        return lineItems.size();
    }
~~~
위는 테스트에 사용될 주문 항목을 반환하는 함수이다. Order에 추가되었다. 이제 `testOrderLineItems`를 실행하면 테스트는 통과하지 못한다. 동일한 상품이라도 개별적으로 추가되는 경우에는 별도의 주문 항목으로 취급되는 것을 알 수 있다. 요구사항이 변경되도 위와같이 테스트를 만들어 두면 어떤 부분에서 문제가 생겼는지 추적이 가능하다.

이제 Order의 with() 메소드를 변경하도록 한다. 이미 등록된 상품을 주문하는 경우 두 주문 항목을 합치도록 변경한다.

##### `Order.java`
~~~ java
private Order with(OrderLineItem lineItem) throws OrderLimitExceededException {
    if (isExceedLimit(customer, lineItem)) { // lineItem
        throw new OrderLimitExceededException();
    }

    for(OrderLineItem item : lineItems) { // lineItems
        if (item.isProductEqual(lineItem)) {
            item.merge(lineItem);
            return this;
        }
    }

    lineItems.add(lineItem);
    return this;
}
~~~

##### `OrderLineItem.java`
~~~ java
    public boolean isProductEqual(OrderLineItem lineItem) {
        return product == lineItem.product;
    }

    public OrderLineItem merge(OrderLineItem lineItem) {
        quantity += lineItem.quantity;
        return this;
    }
~~~

isProductEqual을 통해 등록된 주문 항목 내에 동일 상품에 대한 주문 정보가 있는지 체크한다. 존재할 경우 하나의 주문 항목으로 병합하도록 OrderLineItem 메소드를 호출한다. 

---
### Entry Point와 Repository <a id="4"></a>
[목차로](#0)

#### 특정한 고객에 대한 주문?
주문은 주문 AGGREGATE의 EP이다. 따라서 주문이 필요한 경우 OrderRepository를 통해 해당 주문 객체를 얻을 수 있다. 그렇다면 특정한 고객에 대한 주문 목록을 얻어야 한다면 어떻게 할까? 단순하게 CustomerRepository로부터 고객 객체를 얻은 후 연관을 통해 Order 객체들에게 접근하는 것이다. 그러나 이는 Order와 Customer 간 양방향 연관 관계를 추가하기 때문에 모델의 복잡성을 높인다.

고객에 대한 **주문 목록**을 얻기 위한 적절한 방법은 OrderRepository에 고객 별 **주문 목록**을 조회하는 메소드를 추가하는 것이다. 주문 객체를 얻기 위해 OrderRepository를 사용하는 것은 논리적으로 타당할 뿐 아니라 주문 객체에 접근하기 위한 일관성 있는 방법을 제공한다. 또한, 양방향 연관 관계도 방지할 수 있다.

다음은 OrderRepository에 기능을 추가하기 전 테스트를 작성한다.
##### `reason.OrderRepositoryTest.java`
~~~ java
public class reason.OrderRepositoryTest extends TestCase {
    private Customer customer;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    public void setUp() throws Exception {
        Registrar.init();
        orderRepository = new OrderRepository();
        productRepository = new ProductRepository();
        productRepository.save(new Product("상품1", 1000));
        productRepository.save(new Product("상품2", 5000));

        customer = new Customer("CUST-01", "홍길동", "경기도 안양시", 200000);
    }

    public void testOrdreCount() throws Exception {
        orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                .with("상품1", 5)
                .with("상품2", 20)
                .with("상품1", 5));

        orderRepository.save(customer.newOrder("CUST-01-ORDER-02")
                .with("상품1", 20)
                .with("상품2", 5));

        assertEquals(2, orderRepository.findByCustomer(customer).size());
    }
}
~~~
두 번의 주문을 수행하여 생성된 Order 객체를 OrderRepository에 추가한다. 이후 고객에 속한 주문이 두 건인지 검증한다.

##### `OrderRepository.java`
~~~ java
public Set<Order> findByCustomer(Customer customer) {
    Set<Order> results = new HashSet<Order>();

    for(Order order : findAll()) {
        if (order.isOrderedBy(customer)) {
            results.add(order);
        }
    }
    return results;
}

@SuppressWarnings("unchecked")
public Set<Order> findAll() {
    return new HashSet<Order>((Collection<Order>)Registrar.getAll(Order.class));
}
~~~
findByCustomer() 메소드는 전체 주문 중 특정 고객에 속한 주문 컬렉션을 반환한다. Order가 Customer 정보를 알고 있기 때문에 INFORMATION EXPERT 패턴에 따라 본인이 고객에 포함되어 있는지 여부를 판단할 수 있는 isOrderedBy() 메소드를 Order에 추가한다.

##### `Order.java`
~~~ java
public boolean isOrderedBy(Customer customer) {
    return this.customer == customer;
}
~~~

Customer 클래스가 Ref Obj이고 CustomerRepository에 의해 유일성과 추적성이 보장되므로 동등성 비교를 위해 ”==” 연산자를 사용했다. 

#### 결론
테스트를 실행하면 성공이다. 주문도 정상적으로 처리되고 불변식도 위반하지 않고 중복되는 주문 항목도 없고 고객의 주문 목록도 조회할 수 있게 된것이다.

---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-2.AGGREGATE와 REPOSITORY 1부](http://aeternum.egloos.com/1144679)  
[Domain-Driven Design의 적용-2.AGGREGATE와 REPOSITORY 2부](http://aeternum.egloos.com/1165089)  
[Domain-Driven Design의 적용-2.AGGREGATE와 REPOSITORY 3부](http://aeternum.egloos.com/1173825)  
[Domain-Driven Design의 적용-2.AGGREGATE와 REPOSITORY 4부](http://aeternum.egloos.com/1189025)  
[Domain-Driven Design의 적용-2.AGGREGATE와 REPOSITORY 5부](http://aeternum.egloos.com/1201820)  