## 영속성 관리
[전체 목차로](../README.md)

### 목차 <a id="0"></a>
1. [도메인 객체의 생명주기](#1)
2. [객체 그리고 영속성](#2)
3. [영속성과 REPOSITORY](#3)
4. [ProductRepository Refactoring](#4)

#### 개요
어플리케이션은 상태를 끊임없이 기록하고 이를 재구성할 수 있어야 한다. 그러나 이를 매번 동시에 복구하는 것은 소모적이다. 시스템의 메모리는 한정된 자원이기에 어플리케이션은 지금 작업을 처리하기 위해 필요한 최소한의 정보만 가지고 있도록 한다. 어플리케이션을 망각의 늪에서 구하기 위해선 **영속성**이 필요하다.

### 도메인 객체의 생명주기 <a id="1"></a>
[목차로](#0)  

Chapter02 에서는 EP에 대한 저장, 조회 등의 컬렉션 연산을 위해 REPOSITORY를 사용했다. 여기서는 주문 도메인을 분석한 후 Customer, Product, Order를 EP로 식별하고 각 EP의 생명 주기를 관리하기 위해 각각의 Repository를 도메인 모델에 추가했다.
![](http://thumbnail.egloos.net/600x0/http://pds13.egloos.com/pds/200812/05/18/f0081118_493881ce6feab.jpg)

##### `Order.java`
~~~ java
public static Order order(String orderId, Customer customer) {
    return new Order(orderId, customer);
}

Order(String orderId, Customer customer) {
    super(orderId);
    this.customer = customer;
}

public Order with(String productName, int quantity) throws OrderLimitExceededException {
    return with(new OrderLineItem(productName, quantity));
}
~~~

Order와 OrderLineItem은 Customer의 newOrder() 메소드를 통해 생성된다. newOrder() 메소드는 Order의 CREATION METHOD인 order()를 호출하며 이 메소드는 내부적으로 생성자를 호출하여 Order 클래스를 생성한 후 반환한다. OrderLineItem은 Order의 with()메소드를 사용하여 생성되며, with() 메소드 역시 OrderLineItem의 생성자를 호출하여 인스턴스를 생성한다.

##### `reason.OrderTest.java`
~~~ java
public void testOrderIdentical() throws Exception {
    Order order = customer.newOrder("CUST-01-ORDER-01")
            .with("상품1", 10)
            .with("상품2", 20);
    orderRepository.save(order);

    Order anotherOrder = orderRepository.find("CUST-01-ORDER-01");
    assertEquals(order, anotherOrder);
}
~~~

Order와 OrderLineItem의 생성자가 호출되는 순간 사용자가 입력한 주문 정보를 저장하고 있는 주문 AGGREGATE가 생성된다. 이 때 Ref Obj의 추적성과 유일성을 만족시키기 위해서는 REPOSITORY를 통한 관리가 필요하다. 위는 이를 구현한 동일 객체 참조 테스트 코드이다.

이 테스트를 통해 REPOSITORY를 통해 등록된 주문 객체들은 추적성과 유일성이라는 Ref Obj 특성을 만족한다는 것을 알 수 있다.

##### `reason.OrderRepositoryTest.java`
~~~ java
public void testDeleteOrder() throws Exception {
    orderRepository.save(customer.newOrder("CUST-01-ORDER-01")
                            .with("상품1", 5)
                            .with("상품2", 20));
    Order order = orderRepository.find("CUST-01-ORDER-01");

    orderRepository.delete("CUST-01-ORDER-01");

    assertNull(orderRepository.find("CUST-01-ORDER-01"));
    assertNotNull(order);
}
~~~

이번에는 생성된 주문 내역을 삭제하는 테스트이다. 이번에도 메소드보다 테스트를 먼저 추가한다. 여기서는 주문한 건을 생성한 후 REPOSITORY에 등록한다. 등록 성공 시 해당 주문을 REPOSITORY로부터 삭제한다. 삭제 역시 조회의 경우와 마찬가지로 삭제할 주문 객체를 주문 ID를 통해 명시한다. 이후 삭제를 확인하기 위해 `null` 검증을 진행한다.

마지막 단언문을 보면 delete() 메소드 호출 이후 주문 객체를 삭제하기 전 find()를 통해 REPOSITORY로부터 생성된 객체 참조를 보관한다. delete() 메소드를 호출하여 REPOSITORY로부터 해당 객체를 삭제한 후 find()로 조회된 객체가 null인지 아닌지 확인한다.

이 때 주문 객체는 소멸되는 것이 아니라 시스템 상 Ref Obj 자격을 잃게 된다. 즉, 시스템은 해당 객체의 추적성을 보장하지 않으며 REPOSITORY를 통해 해당 객체를 얻을 수 없게 된다. 

##### `Registrar.java`
~~~java
public static EntryPoint delete(Class<?> entryPointClass, String objectName) {
    return soleInstance.deleteObj(entryPointClass, objectName);
}

@SuppressWarnings("unused")
private EntryPoint deleteObj(Class<?> entryPointClass, String objectName) {
    Map<String,EntryPoint> theEntryPoint =
            entryPoints.get(entryPointClass);
    return theEntryPoint.remove(objectName);
}
~~~

다음은 REPOSITORY에 삭제 메소드를 추가하기 위해 Registrar에 삭제 관련 기본 기능을 추가한다. 각 EP 클래스와 연관된 Map으로부터 검색 키의 엔트리를 삭제한 후 반환하도록 구현한다.

##### `OrderRepository.java`
~~~java
OrderRepository.java
public Order delete(String identity) {
    return (Order)Registrar.delete(Order.class, identity);
}
~~~
Registrar을 사용하여 OrderRepository에 주문 삭제 메소드를 추가한다. 이제 삭제 테스트 또한 통과한다. REPOSITORY는 객체 생명 주기 관리를 위한 모든 기능을 제공하며 시스템은 Ref Obj를 정상적으로 추적한다.

---
### 객체 그리고 영속성(Persistence) <a id="2"></a>
[목차로](#0)

모든 객체는 생성자가 호출되는 시점에 생성된다. 짧은 VO의 수명에 비해 Ref Obj는 상대적으로 긴 수명으로 다양한 이벤트에 반응하며 상태가 변한다. 고객 객체의 경우 한 번 생성되면 시스템은 고객이 탈퇴할 때까지 지속적으로 참조하고 추적할 수 있어야 한다. 이를 위해 도메인 모델에 추가되는 PURE FABRICATION이 REPOSITORY이다.

#### 메모리
메모리는 비싼 자원이다. CPU가 어플리케이션을 실행하기 위해 지속적으로 데이터를 쌓다보면 빈번한 스와핑을 일으키며 이는 전체적인 성능 저하를 가져온다. 따라서 시스템은 자기가 현재 처리해야 할 고객, 주문, 상품을 제외한 모든 다른 정보를 최대한 빨리 잊어버려야 한다. 이는 전체적인 성능을 유지하게 하는 훌륭한 방법이다.

메모리는 휘발성이다. 전원이 꺼질 시 모든 데이터를 잊어버리기에 시스템은 도메인 객체를 메모리에 보관해서는 안된다. 따라서 시스템 내에 지속적으로 보관되어야 하지만 지금 당장 필요하지 않은 정보는 이차 저장소로 옮겨놓을 필요가 있다.

#### 영속성과 RDB
이처럼 일차 저장소인 메모리에 상주하는 도메인 객체를 붙잡아 이차 저장소에 저장(동면)시키는 기법을 영속성 메커니즘이라고 한다. 도메인 객체 중 고객, 주문, 상품과 같이 이차 저장소에 영구히 보관하는(보관되어야 하는) 객체를 영속 객체라고 한다.

다양한 형식의 직렬화(Serialization) 기법들 역시 영속성 메커니즘의 일종이라고 볼 수 있다. java에서 제공하는 기본 직렬화는 메모리 내의 객체 상태를 이차 저장소인 파일 시스템에 저장한 후 필요 시 원래의 상태로 복원한다. 그러나 대용량 데이터를 처리하는 엔터프라이즈 어플리케이션의 경우 직렬화보다 견고하고 성능이 뛰어나며, 데이터 보안과 같은 다양한 지원 기능을 제공할 수 있는 인프라 스트럭쳐를 요구한다.

일반적으로 엔터프라이즈 어플리케이션은 이차 저장소로 관계형 DB를 사용한다. 대부분 어플리케이션이 RDB를 사용한다는 것은 어플리케이션 개발자들에게 두 가지 의미를 가진다.

1. RDB는 안정적인 기술이다. 많은 벤더가 RDMS를 개발하고 있으며 수많은 레거시 시스템들이 RDB를 영속성 저장소로 사용한다. 이는 안정적인 어플리케이션 개발을 가능케 한다.
2. 객체와 관계형 테이블 간 거리는 너무 멀다. RDB는 수학적 집합 개념을 기반으로하며 정규화를 통해 데이터의 중복을 제거하는 것이 목적이다. 이에 비해 객체 지향은 객체 또는 객체들간 응집도와 결합도를 고려한 책임 할당과 행위의 다양성을 기반으로 한다. RDB는 객체 지향의 핵심 개념을 지원하지 않기에 임피던스 불일치가 발생한다.

#### 임피던스 불일치와 ORM

임피던스 불일치의 어려움은 엔터프라이즈 어플리케이션의 아키텍처에 큰 영향을 끼쳤다. 임피던스 불일치를 극복하지 못했던 초창기 개발 커뮤니티의 선택은 데이터베이스 테이블에 매핑하기 쉬운 구조로 도메인 레이어를 설계하는 것이었다. 불에 기름을 붓는 격으로 엔티티 빈의 제약 사항은 이런 설계를 장려하는 결과를 나았다. 결과적으로 객체 지향 언어로 개발된 절차적 방식의 어플리케이션 아키텍처라는 사생아가 탄생했으며 J2EE 엔터프라이즈 어플리케이션의 주도적인 아키텍처로 자리잡고 말았다. 이 영향으로 현재까지도 행위가 없이 상태만 가지는 Anemic Domain Model을가지는 TRANSACTION SCRIPT 패턴이엔터프라이즈 어플리케이션 아키텍처의 주를 이루고 있다.

**행위와 상태를 함께 가지는 객체를 사용하여 도메인 레이어를 설계하는 방식을 DOMAIN MODEL 패턴이라고 한다. DOMAIN MODEL 패턴은 상속성, 캡슐화, 다형성과 같은 모든 객체 지향 기법들을 활용하기 때문에 임피던스 불일치를 해결하기 위한 하부 인프라 지원 없이 적용하기가 쉽지 않다.**

임피던스 불일치를 해결하는 가장 쉬운 방법은 객체 지향 인터페이스를 사용하는 것이나 이는 상업적으로 성공하지 못했으며 대부분 프로젝트에서 고려사항이 아니다. 이를 극복하는 방법은 객체 계층과 RDB 계층 사이 가상의 객체 지향 DB를 구축하는 것이다. 이처럼 객체와 RDB 테이블간 불일치를 SW적으로 해결하는 것을 객체 관계 매핑이라 하며 이를 수행하는 SW를 객체 관계 매퍼 (ORM)이라 한다.

ORM은 내부적으로 DATA MAPPER 패턴을 사용한다. DATA MAPPER는 객체 지향 도메인 객체와 RDB 테이블, 그리고 매퍼 자체의 독립성을 유지하면서 도메인 객체와 테이블 간 매핑 정보를 어플리케이션 외부 설정 파일로 관리한다.

#### 결론

주문 도메인에서 Customer 클래스는 고객 상태와 상태를 변경시키기 위한 행위를 함께 가지고 있다. Order 클래스는 주문의 상태와 상태를 변경시키기 위한 행위를 함께 가지고 있다. 즉, 주문 도메인 어플리케이션의 도메인 로직은 DOMAIN MODEL 패턴으로 구성되어 있다. 따라서 임피던스 불일치 문제를 해결하는 가장 쉬운 방법은 DATA MAPPER 패턴을 사용한 ORM을 적용하는 것이다.

지금까지는 어플리케이션의 생명 주기 동안 지속적으로 추적해야 하는 객체들을 REFERENCE OBJECT로 모델링하고 연관된 REFERENCE OBJECT들을 AGGREGATE라고 하는 하나의 객체 클러스터로 식별했다. 각 AGGREGATE에 대해 ENTRY POINT를 선정하고 ENTRY POINT 별로 REPOSITORY를 할당한 후 REPOSITORY를 통해 AGGREGATE의 생명 주기를 관리하도록 했다. 이제까지는 단순히 REPOSITORY를 REFERENCE OBJECT의 메모리 컬렉션을 관리하는 객체로만 바라 보았다. 이제 REFERENCE OBJECT의 영속성을 관리하는 객체로 REPOSITORY의 개념을 확장한다.

---
### 영속성과 REPOSITORY <a id="3"></a>
[목차로](#0)

REPOSITORY는 도메인 객체 생성 이후 생명주기를 책임진다. 도메인 객체가 생성되고 상태가 초기화된 후에는 REPOSITORY에 넘겨진다. REPOSITORY는 객체를 넘겨 받아 내부 저장소에 보관하고 요청이 있는 경우 객체를 조회-반환 하거나 삭제한다. 클라이언트 입장에서의 REPOSITORY는 메모리에 도메인 객체들을 담고 있는 객체 풀과 같다. 클라이언트는 이를 통해 객체를 생성하며 조회를 요청한다.

REPOSITORY의 기능을 메모리 컬렉션에 대한 오퍼레이션으로 바라보는 것은 도메인 모델을 단순화하기 위한 중요한 추상화 기법이다. 도메인 모델을 설계하고 필요한 오퍼레이션을 식별하는 동안 하부의 어떤 메커니즘이 도메인 객체들의 생명 주기를 관리하는지에 대한 세부 사항을 무시할 수 있다. REPOSITORY가 제공하는 인터페이의 의미론을 메모리 컬렉션 관리 개념으로 추상화함으로써 자연스럽게 하부 데이터 소스와 관련된 영속성 메커니즘을 도메인 모델로부터 분리할 수 있다.

복잡성을 관리하는 중요한 방법은 서로 다른 관심사를 고립시켜 한 번에 하나의 이슈만 해결하도록 하는 것이다. 
1. 도메인을 모델링할 때는 REPOSITORY를 통해 모든 객체가 메모리에 있다는 착각을 주어 하부 인프라에 대한 부담 없이 도메인 로직에 집중할 수 있다. 
2. 하부의 데이터 접근 로직을 REPOSITORY에 집중시킴으로써 도메인 로직과 데이터 접근 로직을 자연스럽게 분리시킬 수 있다. 
3. 영속성 메커니즘이 REPOSITORY 내부로 제한되어 있기에 도메인 모델에 영향을 미치지 않고 영속성 메커니즘을 교체하는 것이 가능하다.

따라서 REPOSITORY를 모델링 할 때는 
1. 하부 영속성 메커니즘에 관한 세부 사항을 배제하고 메모리 컬렉션을 관리하는 객체로 모델링한다. 
2. 인터페이스는 메모리 객체 풀을 관리한다는 의도를 나타내도록 명명한다. 
3. REPOSITORY를 사용하는 클라이언트는 DB에 대한 고려는 하지 않는다.
4. 클라이언트는 객체 정보가 어디에 어떻게 저장되어있는지 관심이 없다.

REPOSITORY를 구현할 때는 현재 사용중인 하부의 데이터 소스를 고려해야 한다. 클라이언트 개발에서는 하부의 소스를 몰라도 되지만 내부 구현에서는 그렇지 않다.

> REPOSITORY는 전역으로 접근 될 필요가 있는 각 객체 타입에 대해 해당 타입의 모든 객체들을 메모리 컬렉션으로 저장하고 있는 듯한 착각을 일으키는 객체를 생성한다. 잘 알려진 전역 인터페이스를 통해 이 객체들에 접근할 수 있도록 한다. 실제 데이터 저장소에 데이터를 추가하고 삭제하는 실제적인 작업을 캡슐화하는 추가/삭제 메소드를 작성한다. 특정 쿼리 조건을 만족하는 객체 또는 객체들의 컬렉션을 반환하는 조회 메소드를 추가함으로써 실제 저장소와 쿼리 기술을 캡슐화한다. 모든 객체 저장소와 접근을 REPOSITORY로 위임함으로써 클라이언트가 모델에만 초점을 맞추도록 한다. *by. Domain-Driven Design, Eric Evans*

#### 영속성 메커니즘의 추가
결론적으로 REPOSITORY는 영속성 메커니즘을 캡슐화시키는 지점이다. 따라서 영속성 메커니즘을 추가하기 위해서는 이 REPOSITORY의 구현을 바꿔야 한다. 이를 위해 먼저 도메인 객체와 REPOSITORY 간 결합도를 낮추도록 한다.

##### `OrderLineItem.java`
~~~ java
public class OrderLineItem { 
    private ProductRepository productRepository = new ProductRepository();

    public OrderLineItem(String productName, int quantity) {
        this.product = productRepository.find(productName);
        this.quantity = quantity;
    }
}
~~~

OrderLineItem은 인스턴스 변수로 ProductRepository를 포함하며 클래스 로딩 시 new 연산자를 사용하여 ProductRepository 인스턴스를 직접 생성한다. 그리고 OrderLineItem은 생성자에 전달된 상품명을 가진 Product 객체를 찾기 위해 이를 사용한다.

Ref Obj들을 메모리가 아닌 RDB내에 관리하기로 정책을 수정했다고 가정하면 기존의 ProductRepository는 메모리 컬렉션을 관리하는 Registrar를 사용하고 있으므로 어쩔 수 없이 ProductRepository 내부 코드를 수정할 수 밖에 없다.
이는 **개방 폐쇄 원칙(OCP)** 위반이다.

그러나 Ref Obj는 앞으로 RDB에서만 관리할 것이다. 이를 메모리에서 관리할 필요가 없다면 ProductRepository 내부를 변경해도 될 것 같다. 이미 Ref Obj에 대한 처리 로직은 REPOSITORY 내부로 고립시켰다. REPOSITORY 내부를 수정하여 DB 접근 코드를 추가해도 될 것 같다.

그러나 OrderLineItem은 ProductRepository에 의존한다. ProductRepository는 DB에 의존한다. 따라서 OrderLineItem 역시 DB에 의존하고 OrderLineItem을 사용하는 모든 Customer, Order역시 DB에 의존한다. 즉, 이를 통해 대부분의 도메인 클래스가 DB와 결합되게 된다.

이렇게되면 단위 테스트를 수행하기 위해 DBMS가 실행 중이어야 하고 필요한 데이터들이 미리 입력되어 있어야 하며 각 단위 테스트가 종료된 후에 다른 테스트에 영향을 미치지 않도록 모든 DB 상태를 초기화해야한다. DB는 속도가 느리고 결과 피드백 또한 느려 개발의 리듬도 방해하게 된다. 따라서 단위테스트는 DB로부터 독립시켜야 한다.

문제는 결합도이다.
- OrderLineItem은 ProductRepository에 강하게 결합되어 있다. 
- 이 때문에 ProductRepository 없이 OrderLineItem이 존재할 수 없다.
- OrderLIneItem을 사용하기 위해서는 ProductRepository가 존재해야 한다.
- ProductRepository이 존재하기 위해서는 DB가 구동 중이어야 한다.

객체 간 결합은 자연스러운 것이다. 각 클래스가 높은 응집도 유지를 위해 다른 클래스와 협력하는 것은 객체 지향의 기본 원리이다. OrderLineItem은 Product Ref Obj를 얻기 위해 ProductRepository와 협력해야 한다. 문제는 그 결합도가 필요 이상으로 높다는 것이다. OrderLineItem이 ProductRepository 인스턴스를 직접 생성하기 때문에 둘을 분리시킬 방법이 없다. 구체적인 클래스가 또 다른구체적인 클래스에 의존한다는 것은 전체적인 어플리케이션 유연성을 저해한다.

결합도를 낮추는 일반적인 방법은 둘의 직접적 의존 관계를 제거하고 두 클래스가 추상에 의존하도록 설계를 수정하는 것이다. 구체적인 클래스가 추상적 클래스에 의존하게 함으로써 전체적 결합도를 낮추는 것이다. 구체적 클래스들 간 의존 관계를 추상 계층을 통해 분리함으로써 OOP를 위반하는 설계를 제거 가능하다.

고로, OrderLineItem과 ProductRepository가 모두 인터페이스에 의존하도록 설계를 수정한다면 유연하고 낮은 결합도를 유지하면서 OOP를 위반하지 않는 설계를 만들 수 있다. 이를 위한 가장 간단한 방법은
1. ProductRepository를 인터페이스와 구현 클래스로 분리한다.
2. OrderLineItem과 ProductRepository의 구현 클래스가 ProductRepository 인터페이스에 의존하도록 한다.

---
### ProductRepository Refactoring <a id="4"></a>
[목차로](#0)

우선 ProductRepository를 리팩토링한다. 구체적인 클래스에서 인터페이스를 추출하는 EXTRACT INTERFACE를 적용한다.

##### `ProductRepository.java`
~~~ java
public interface ProductRepository {
    public void save(Product product1);
    public Product find(String productName);
}
~~~

##### `ProductRepositoryImpl.java`
~~~ java
public class ProductRepositoryImpl implements ProductRepository{
    public void save(Product product) {
        Registrar.add(Product.class, product);
    }

    public Product find(String productName) {
        return (Product)Registrar.get(Product.class, productName);
    }
}
~~~
EXTRACT INTERFACE 리팩토링을 통해 구체적인 클래스인 ProductRepositoryImpl가 인터페이스인 ProductRepository에 의존하도록 수정했다.

##### `OrderLineItem.java`
~~~java
private ProductRepository productRepository = new ProductRepositoryImpl();

public OrderLineItem() {
}

public OrderLineItem(String productName, int quantity) {
    this.product = productRepository.find(productName);
    this.quantity = quantity;
}
~~~

OrderLineItem의 productRepository 속성의 타입을 ProductRepository 인터페이스로 변경함으로써 OrderLineItem이 인터페이스에 의존하도록 수정했다. OrderLineItem의 생성자 내부에서는 ProductRepository 인터페이스 타입의 productRepository만을 사용하기 때문에 인터페이스에만 의존하고 구체적인 클래스에는 의존하고 있지 않다. 그러나 여전히 OrderLineItem 자체는 구체적인 클래스인 ProductRepositoryImpl와 강하게 결합되어 있다. 원인이 무엇일까?

---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 1부](http://aeternum.egloos.com/1218235)  
[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 2부](http://aeternum.egloos.com/1228366)  
[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 3부](http://aeternum.egloos.com/1239549)  
[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 4부](http://aeternum.egloos.com/1249542)  