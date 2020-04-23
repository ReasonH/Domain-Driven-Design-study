## 1. Value Object와 Reference

### 목차 <a id="0"></a>
1. [동일함과 별칭 문제](#1)
2. [불변성](#2)
3. [생명 주기](#3)
4. [생명 주기 관리1](#4)
5. [생명 주기 관리2](#5)

#### 개요
> 프로젝트를 가장 훌륭하게 작성하는 방법은 상태가 변경되는 오브젝트들과 수학적인 값을 나타내는 오브젝트들의 조합으로 표현하는 것이다.

애플리케이션을 구성하는 객체
1. Value Object (이하 VO)
    - 추적성에는 관심을 두지 않음
    - ex) 오늘 출금한 10,000원과 과거 출금한 10,000이 동일할 필요는 없음
2. Reference Object (이하 Ref Obj)
    - 실세계의 추적 가능한 개념
    - 시스템 내에서 유일한 식별 가능

시스템에서 해당 객체를 계속 추적해야하며 그 개념이 유일하게 존재해야 한다면 Ref Obj로 만든다.
객체가 추적 필요가 없으며 속성값이 동일하면 동일한 객체로 간주해도 되는 경우 VO로 만든다.

### 동일함과 별칭 문제 <a id="1"></a>
[목차로](#0)

#### 동일함의 의미

##### Coustomer.java
~~~java
package org.eternity.customer;

public class Customer {
  private String customerNumber;
  private String name;
  private String address;
  private long mileage;

  public Customer(String customerNumber, String name, String address) {
    this.customerNumber = customerNumber;
    this.name = name;
    this.address = address;
  }

  public void purchase(long price) {
    mileage += price * 0.01;
  }

  public boolean isPossibleToPayWithMileage(long price) {
    return mileage > price;
  }

  public boolean payWithMileage(long price) {
    if (!isPossibleToPayWithMileage(price)) {
      return false;
    }
    mileage -= price;
    return true;
  }

  public long getMileage() {
    return mileage; 
  }
}
~~~

##### Money.java
~~~java
package org.eternity.customer;
import java.math.BigDecimal;

public class Money {
  private BigDecimal amount;

  public Money(BigDecimal amount) {
    this.amount = amount;
  }

  public Money(long amount) {
    this(new BigDecimal(amount));
  }

  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof Money)) {
      return false;
    }           
    return amount.equals(((Money)object).amount);
  }

  public int hashCode() {
    return amount.hashCode();
  }

  public Money add(Money added) {
    this.amount = this.amount.add(added.amount);
    return this;
  }

  public String toString() {
    return amount.toString();
  }
}
~~~

고객은 객체 동일성을 확인 시 메모리 주소를 비교하는 "=="연산자를 사용하여 확인한다.
반면 금액은  equal을 이용한 객체 속성 값의 비교를 진행한다.

#### 별칭(aliasing) 문제
java에서는 하나의 객체를 서로 다른 변수가 참조 가능하다. 이를 별칭이라고 한다. 동일한 고객 객체를 참조하는 두 가지 별칭이 존재할 경우 한 쪽에서 변화가 일어났을 때 다른 쪽에서 이를 놓치기 쉽다.

따라서 고객 객체를 다루는 가장 효과적 방법은 별칭을 만들지 않는 것이다. 
그러나, 해당 객체를 다른 메소드 인자로 전달할 때 또한 별칭이 자동으로 생성되기에 문제 방지에 한계가 있다.

##### `MoneyTest.java`
~~~ java
public void testMehodAlaising() {
  Money money = new Money(2000);
  doSomethingWithMoney(money);
  assertEquals(new Money(2000), money);
}

private void doSomethingWithMoney(final Money money) {
  money.add(new Money(2000));
}
~~~

메소드 인자에 final을 사용해도 문제를 막을 수 없다.
파라미터 전달에서 final은 재할당을 막을 뿐 속성의 변경을 막진 못한다.

#### 객체가 메소드 인자로 전달될 때의 주의사항
- 인자를 전달하는 동안 별칭(aliasing)이 자동으로 생성된다.
- 지역 객체란 존재하지 않는다. 다만 지역 참조만이 존재할 뿐이다.
- 참조는 범위(scope)를 가지지만 객체는 그렇지 않다.
- 객체의 생명주기는 java에서 이슈가 아니다.
- java에는 오브젝트의 수정과 별칭의 부정적인 영향을 막을(const와 같은) 언어적인 지원 메커니즘이 존재하지 않는다. 인자 목록에 final을 사용할 수는 있지만 이것은 단순히 참조가 다른 객체와 다시 묶이는 것을 막아줄 뿐이다.

별칭 문제를 해결하기 위한 가장 좋은 방법은 객체를 불변 상태로 만드는 것이다.

### 불변성<a id="2"></a>
[목차로](#0)

불변성 클래스는 다음의 규칙을 따른다.

- 객체를 변경하는 메소드(mutator)를 제공하지 않는다.
- 재정의할 수 있는 메소드를 제공하지 않는다.
- 모든 필드를 final로 만든다.
- 모든 필드를 private으로 만든다.
- 가변 객체를 참조하는 필드는 배타적으로 접근해야 한다.

`Money` 클래스를 불변으로 만들기 위해 위의 규칙을 적용한다.
Remove Setting Method 리팩토링을 진행한다.

~~~java
public class Money {
  private final BigDecimal amount; // final 적용

  public Money(BigDecimal amount) {
    this.amount = amount;
  }

  public Money(long amount) {
    this(new BigDecimal(amount));
  }

  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof Money)) {
      return false;
    }           
    return amount.equals(((Money)object).amount);
  }

  public int hashCode() {
    return amount.hashCode();
  }

  // setting method로 간주되어 다음을 삭제한다
  // public Money add(Money added) {
  //   this.amount = this.amount.add(added.amount);
  //   return this;
  // }

  // 변경 후: 이는 기존 객체의 값은 변경시키지 않고 연산 결과를 상태로 갖는 새로운 불변 객체를 생성한다.
  public Money add(Money added) {
    return new Money(this.amount.add(added.amount));
  } 

  public String toString() {
    return amount.toString();
  }
}
~~~

#### VO와 불변성
객체를 불변으로 만들면 별칭 문제를 피할 수 있다. 이 때 객체의 상태를 바꾸는 대신 새로운 불변 객체를 만들어 기존 객체를 대체 시킨다.

VO는 불변 객체여야 한다.
새로운 값이 필요한 경우 기존 객체가 아닌 새로운 VO를 생성하여 대체시킨다.

- Money가 있는 Wallet의 Money 속성을 변경하는 것 X
- Money가 있는 Wallet에 새로운 Money를 생성해 연결, 기존 Money는 GC 타겟

**VO**는 일반적으로 날짜, 금액 등 작은 개념이기에 새로운 객체로 대체할 때 오버헤드가 적다. 추적성에도 관심이 없기 때문에 동일 객체를 유지할 필요 또한 없다.

**VO**는 도메인의 복잡성을 낮출 수 있다. 유용하지만 비즈니스적 관점에서 가치가 없는 개념을 VO로 모델링하면 추적성, 별칭 문제에 대한 부담 없이 객체를 참조 가능하다. -> 객체의 생명주기 단순화

**VO**는 반드시 불변이어야 하지만 **Ref Obj**는 일반적으로 불변이 아니다. 고객, 주문과 같은 도메인 개념들은 시간에 따라 상태가 변한다.
ex) 고객은 지속적으로 상품을 구입, 지불, 마일리지를 적립함 -> 상태가 변경

시스템이 이벤트에 따른 고객의 상태를 갱신, 추적하기 위해서는 항상 동일 고객 객체가 시스템 각 부분에 전달되어야 한다. 따라서 시스템의 모든 부분은 동일한 고객 객체를 공유해야 하며 이로 인한 별칭 문제는 피할 수 없다. **Ref Obj**에 있어 별칭은 요구사항이며 시스템은 **Ref Obj**의 변경사항을 추적해야 한다.

**Ref Obj**가 불변 객체로 만들어질 수 있다면 그렇게 하는 것이 최선이다. 요구사항에서 대상이 최초 생성 시 설정된 속성이 그대로 유지되는 추적 가능한 도메인 개념이라면 불변성을 가진 **Ref Obj**로 설계하는 것이 복잡성을 낮추는 방법이다.

가능하다면 불변 객체로 시작하되 객체의 변경 사항이 시스템 내에서 전파될 필요가 있다면(**추적 필요성**) 가변 객체로 변경한다. 단, Ref Obj 상태를 바꾸기 위해 VO처럼 새로운 Ref Obj를 생성하여 대체시키면 안 된다(**시스템 내 일관성 유지**). Ref Obj를 불변으로 만드는 유일한 방법은 Ref Obj 인터페이스에 상태 변경 메소드를 포함시키지 않는 것이다.

#### 정리
1. VO는 객체 상태 변경 메소드 포함 가능하며 이는 속성 변경이 아닌 새로운 VO의 반환하는 것이다.
2. Ref Obj는 오직 유일한 식별자를 가진 하나의 객체만이 존재해야 한다.
3. 일반적인 Ref Obj는 상태 변경이 가능하다. 
4. Ref Obj가 불변이고 별칭 문제를 신경쓰고 싶지 않다면 객체에 상태 변경 메소드를 포함시키지 않는다.

### 생명 주기<a id="3"></a>
[목차로](#0)

#### 생명 주기 제어

객체지향 시스템에서 특정 작업을 수행하기 위해서는 어떤 객체에서 이를 시작할지 결정해야 한다.

DB와 다르게 객체 지향 시스템은 임의 결과 목록에 자동 접근 가능한 메커니즘을 제공하지 않는다. 따라서 어떤 객체 그룹을 사용하기 위해서는 객체 그래프 상에서 시작 객체(ENTRY POINT)를 정해야 한다. **ENTRY POINT(이하 EP)**는 항상 Ref Obj여야 하며 VO는 될 수 없다.

따라서 시스템은 임의의 EP에 접근 가능해야 한다. 또한, EP는 Ref Obj이므로 EP 접근 때마다 동일한 객체 인스턴스를 반환 받아야한다. 즉, 동일 EP요청에 동일 식별자를 지닌 객체가 반환되어야 한다.

EP의 유일성과 추적성 유지를 위해서는 EP를 관리하는 특별 객체가 필요하다. 특별 객체는 특정 EP 목록을 유지하고 클라이언트에 EP에 대한 인터페이스를 제공한다. 모든 EP에 대한 검색은 해당 관리 객체를 통해 이루어지므로 시스템 모든 부분은 항상 동일하고 유일한 EP를 대상으로 작업을 수행할 수 있다.

EP 관리 인터페이스 구성 방법은 두 가지이다.
- 각각의 EP가 스스로 관리 인터페이스 제공
- 별도 객체가 EP에 대한 관리 인터페이스 제공

##### `EntryPoint.java`
~~~ java
package org.eternity.common;
public class EntryPoint { 
  private final String identity;

  public EntryPoint (String identity) {
    this.identity = identity;
  }

  public String getIdentity() {
    return identity;
  }

  public EntryPoint persist() {
    Registrar.add(this.getClass(), this);
    return this;
  }
}
~~~
이는 모든 EP에 대한 Layer supertype인 `EntryPoint` 클래스이다. 해당 클래스는 EP 검색에 사용될 검색 키를 생성자 인자로 전달받는다. 이를 상속받는 모든 EP들은 객체 생성 시 자신의 id를 제공하도록 강제된다. 객체 생성 후에는 persist method를 통해 EP 관리 객체를 사용하여 자신을 등록한다. 등록된 EP는 검색 키를 사용하여 다시 조회 가능하다.

##### `Registrar.java`
~~~ java
package org.eternity.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Registrar {
  private static Registrar soleInstance = new Registrar();
  private Map<Class<?>,Map<String,EntryPoint>> entryPoints =
    new HashMap<Class<?>, Map<String, EntryPoint>>();

  public static void init() {
    soleInstance.entryPoints =
      new HashMap<Class<?>, Map<String, EntryPoint>>();
  }

  public static void add(Class<?> entryPointClass, EntryPoint newObject){
    soleInstance.addObj(entryPointClass, newObject);
  }

  public static EntryPoint get(Class<?> entryPointClass, String objectName) {
    return soleInstance.getObj(entryPointClass, objectName);
  }

  public static Collection<? extends EntryPoint> getAll
    (Class<?> entryPointClass) {
    return soleInstance.getAllObjects(entryPointClass);
  }

  private void addObj(Class<?> entryPointClass, EntryPoint newObject) {
    Map<String,EntryPoint> theEntryPoint = entryPoints.get(entryPointClass);
    if (theEntryPoint == null) {
      theEntryPoint = new HashMap<String,EntryPoint>();
      entryPoints.put(entryPointClass, theEntryPoint);
    }
    theEntryPoint.put(newObject.getIdentity(), newObject);
  }

  private EntryPoint getObj(Class<?> entryPointClass, String objectName) {
    Map<String,EntryPoint> theEntryPoint = entryPoints.get(entryPointClass);
    return theEntryPoint.get(objectName);
  }

  @SuppressWarnings("unchecked")
  private Collection<? extends EntryPoint> getAllObjects(Class<?> entryPointClass) {
    Map<String,EntryPoint> foundEntryPoints = entryPoints.get(entryPointClass);
    return (Collection<? extends EntryPoint>)
      Collections.unmodifiableCollection(foundEntryPoints != null ?
        entryPoints.get(entryPointClass).values() :
        Collections.EMPTY_SET);
  }
}
~~~

이는 메모리 내의 EP 컬렉션을 관리할 Registrar 클래스이다. Registrar 클래스는 Singleton이며 EP들의 Class와 id를 사용하여 각 EP를 관리한다. 

##### `Customer.java` 수정
~~~ java
public class Customer extends EntryPoint {
...

  public Customer(String customerNumber, String name, String address) {
    super(customerNumber);
    this.customerNumber = customerNumber;
    this.name = name;
    this.address = address;
  }
}
~~~

이제 Customer 객체가 EP를 상속받도록 한다. 고객에 대한 검색 키는 고객 번호인 customerNumber 이다.

이제 Registrar 클래스를 사용하여 고객의 유일성을 유지할 수 있다. 

### 생명 주기 관리1<a id="4"></a>
[목차로](#0)

다음은 Customer 클래스 자체에 EP의 컬렉션 관리 인터페이스를 추가하는 방식이다. 우선 Customer 클래스의 검색을 위한 TC를 작성하자

##### `CustomerTest.java`
~~~ java
public void setUp() {
  Registrar.init();
}
  
public void testCustomerIdentical() {
  Customer customer = new Customer("CUST-01", "홍길동", "경기도 안양시").persist();
  Customer anotherCustomer = Customer.find("CUST-01");
  assertSame(customer, anotherCustomer);           
}
~~~

위는 항상 실패하는 테스트 케이스이다. (*TDD*)
모든 TC가 독립적이어야 한다는 테스트 기본 원칙을 지키기 위해 setUp() 메소드 안에서 Registrar를 초기화시켰다. 또한, persist() 이용하여 Registrar에 등록한다. id인 고객 번호는 find 메소드 인자로 전달하여 객체 조회 후 반환된 anotherCustomer가 이미 등록된 customer와 동일한 식별자를 가지는지 검사한다.

이제 `Customer.java`를 수정한다.
~~~ java
public static Customer find(String customerName) {
  return (Customer)Registrar.get(Customer.class, customerName);
}

public Customer persist() { 
  // 각 EP가 자신의 타입 반환하도록하여 클라이언트 측에서 매번 형변환을 하지 않도록 만든다
  return (Customer)super.persist(); // downcasting
}
~~~

##### 수정된 `Customer.java`

~~~ java
package org.eternity.customer;

public class Customer extends EntryPoint {
  private String customerNumber;
  private String name;
  private String address;
  private long mileage;

  public Customer(String customerNumber, String name, String address) {
    super(customerNumber);
    this.customerNumber = customerNumber;
    this.name = name;
    this.address = address;
  }

  public static Customer find(String customerName) {
    return (Customer)Registrar.get(Customer.class, customerName);
  }

  public Customer persist() { 
    // 각 EP가 자신의 타입 반환하도록하여 클라이언트 측에서 매번 형변환을 하지 않도록 만든다
    return (Customer)super.persist(); // downcasting
  }

  public void purchase(long price) {
    mileage += price * 0.01;
  }

  public boolean isPossibleToPayWithMileage(long price) {
    return mileage > price;
  }

  public boolean payWithMileage(long price) {
    if (!isPossibleToPayWithMileage(price)) {
      return false;
    }
    mileage -= price;
    return true;
  }

  public long getMileage() {
    return mileage; 
  }
}
~~~

### 생명 주기 관리 2<a id="5"></a>
[목차로](#0)

이는 Customer 클래스와는 분리된 별도 클래스에 Customer 클래스의 컬렉션 관리 인터페이스를 할당하는 방법이다. 우선 테스트 케이스를 작성한다.

##### `CustomerTest.java`
~~~ java
public void setUp() {
  Registrar.init();
}

public void testCustomerIdentical() {
  CustomerRepository customerRepository = new CustomerRepository();    
  Customer customer = new Customer("CUST-01", "홍길동", "경기도 안양시");
  customerRepository.save(customer);
  Customer anotherCustomer = customerRepository.find("CUST-01");
  assertSame(customer, anotherCustomer);
}
~~~

이번에는 CustomerRepository라는 별도의 REPOSITORY 객체를 사용해서 객체 컬렉션을 관리한다.  

##### 수정된 `EntryPoint.java`
~~~ java
package org.eternity.common;

public class EntryPoint {
  private final String identity;

  public EntryPoint(String identity) {
    this.identity = identity;
  }

  public String getIdentity() {
    return identity;
  }
}
~~~

별도 객체로 검색을 분리했기 때문에 EP는 단순히 검색 키를 반환하는 메소드만을 제공하면 된다.

##### 수정된 `Customer.java`
~~~ java
package org.eternity.customer;

public class Customer extends EntryPoint {
  private String customerNumber;
  private String name;
  private String address;
  private long mileage;

  public Customer(String customerNumber, String name, String address) {
    super(customerNumber);
    this.customerNumber = customerNumber;
    this.name = name;
    this.address = address;
  }

  public void purchase(long price) {
    mileage += price * 0.01;
  }

  public boolean isPossibleToPayWithMileage(long price) {
    return mileage > price;
  }

  public boolean payWithMileage(long price) {
    if (!isPossibleToPayWithMileage(price)) {
      return false;
    }
    mileage -= price;
    return true;
  }

  public long getMileage() {
    return mileage; 
  }
}
~~~

Customer는 관리 메소드를 제공하지 않아도 된다. 생명 주기 관리1 에서와 달리 find, persist(=save)가 사라졌다.

##### `CustomerRepository.java`

~~~ java
package org.eternity.customer;
import org.eternity.common.Registrar;

public class CustomerRepository {
  public void save(Customer customer) {
    Registrar.add(Customer.class, customer);
  }

  public Customer find(String identity) {
    return (Customer)Registrar.get(Customer.class, identity);
  }
}
~~~

Customer에 대한 컬렉션 관리 메커니즘을 제공하는 CustomerRepository는 Registrar를 사용하기 위한 세부 내용을 캡슐화 한다.

### 도메인의 복잡성

1. Ref Obj와 VO의 개념은 도메인 영역을 추상화시키는 한 방법이다.
  - 도메인 개념들의 추적성과 식별성을 추상화하기 위한 분석 기법
  - 도메인의 본질적인 특성에 초점을 맞춤
2. 각 도메인 개념의 유일성, 특성, 추적 처리를 계속 질문하는 것은 도메인에 대한 이해를 향상시키며 SW 복잡성을 완화시킨다.
3. Ref Obj의 식별로 시스템의 핵심 개념들 생명주기에 초점을 맞출 수 있다.
4. VO의 식별로 도메인의 일부이나 중요치 않은 개념들을 걸러낼 수 있다.

다음 챕터에서는 Ref Obj와 VO를 사용한 간단한 도메인을 모델링하고 이번 아티클에서 설명한 EP와 Aggregate, Repository에 관해 더 살펴본다.

---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-1.VALUE OBJECT와 REFERENCE OBJECT 1부](http://aeternum.egloos.com/1105776)  
[Domain-Driven Design의 적용-1.VALUE OBJECT와 REFERENCE OBJECT 2부](http://aeternum.egloos.com/1111257)  
[Domain-Driven Design의 적용-1.VALUE OBJECT와 REFERENCE OBJECT 3부](http://aeternum.egloos.com/1114933)  
[Domain-Driven Design의 적용-1.VALUE OBJECT와 REFERENCE OBJECT 4부](http://aeternum.egloos.com/1115939)  