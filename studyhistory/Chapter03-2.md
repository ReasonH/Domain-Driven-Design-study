## Dependency injection과 AOP
[전체 목차로](../README.md)

### 목차 <a id="0"></a>
1. 사용과 구성의 분리
2. [관점의 변경](#2)

### 사용과 구성의 분리
![](http://pds12.egloos.com/pds/200812/24/18/f0081118_4951ab22a44fd.jpg)

**OrderLineItem이 직접 ProductRepositoryImpl를 생성하기에 OrderLineItem이 여전히 구체적인 클래스에 의존한다.**

OrderLineItem이 직접 ProductRepositoryImpl를 생성하기에 여전히 둘 간에 강한 결합 관계가 존재한다. 만약 ProductRepositoryImpl를 DB에 접근하도록 수정하면 이전 설계에서의 OCP 위반, 단위 테스트의 번거로움, DB에 대한 종속성과 같은 문제점이 그대로 유지된다.

문제의 원인은 객체 구성과 사용이 OrderLineItem 한 곳에 공존하고 있다는 것이다. 현재 설계에서는 OrderLineItem이 직접 ProductRepositoryImpl와의 관계를 설정한다. 객체 구성과 사용이 한 곳에 모여 있을 경우 객체 간 결합도가 높아진다. 해결 방법은 외부 객체가 OrderLineItem과 ProductRepositoryImpl 간 관계를 설정하도록 함으로써 구성을 사용으로부터 분리시키는 것이다.

![](http://pds1.egloos.com/pds/200812/24/18/f0081118_4951ab3a22816.jpg)

**구성과 사용을 분리시킴으로 직접적 결합을 제거했다.**

이처럼 협력 객체들의 외부에 존재하는 제3의 객체가 협력 객체 간의 의존성을 연결하는 것을 의존성 주입이라 한다. 이를 위한 다양한 프레임워크가 있는데, 이는 의존성 주입을 위한 객체 생명주기를 관리하는 컨테이너 역할을 수행하기에 경량 컨테이너라고도 한다. 여기서는 Spring Framework를 사용하도록 한다.

#### 의존성 주입
##### `OrderLineItem.java`
~~~java
public class OrderLineItem {
    private Product product;
    private int quantity;

    private ProductRepository productRepository;

    public OrderLineItem() {
    }

    public OrderLineItem(String productName, int quantity) {
        this.product = productRepository.find(productName);
        this.quantity = quantity;
    }
    
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
~~~
우선 OrderLineItem에서 Repository 생성 부분을 제거하며 DI를 위한 setter를 추가한다. (*setter injection*) setter의 인자 타입 또한 ProductRepository 인터페이스라는 것에 주의하자.

Spring과 같은 경량 컨테이너를 사용하여 얻을 수 있는 장점은 불필요한 SINGLETON을 줄일 수 있다는 점이다. Spring은 컨테이너에서 관리할 객체를 등록할 때 객체의 인스턴스를 하나만 유지할 지 필요 시 매번 새로운 인스턴스를 생성할 지 정의할 수 있다. 따라서 오버라이딩이 불가능하고 결합도가 높은 static 메소드를 사용하지 않고도 객체를 singleton으로 유지할 수 있다. 따라서 spring을 사용하면 singleton으로 구현된 Registrar을 인터페이스와 구체적인 클래스로 분리함으로써 낮은 결합도와 높은 유연성을 제공할 수 있다. EXTRACT INTERFACE 리팩토링을 진행한다.

##### `Registrar.java`
~~~java
public interface Registrar {
    void init();

    void add(Class<?> entryPointClass, EntryPoint newObject);

    EntryPoint get(Class<?> entryPointClass, String objectName);

    Collection<? extends EntryPoint> getAll(Class<?> entryPointClass);

    EntryPoint delete(Class<?> entryPointClass, String objectName);
}
~~~

##### `EntryPointRegistrar.java`
~~~java
public class EntryPointRegistrar implements Registrar {
    private Map<Class<?>, Map<String,EntryPoint>> entryPoints;

    public EntryPointRegistrar() {
        init();
    }

    public void init() {
        entryPoints = new HashMap<Class<?>, Map<String, EntryPoint>>();
    }

    public void add(Class<?> entryPointClass, EntryPoint newObject) {
        Map<String,EntryPoint> theEntryPoint =
                entryPoints.get(entryPointClass);
        if (theEntryPoint == null) {
            theEntryPoint = new HashMap<String,EntryPoint>();
            entryPoints.put(entryPointClass, theEntryPoint);
        }
        theEntryPoint.put(newObject.getIdentity(), newObject);
    }

    public EntryPoint get(Class<?> entryPointClass, String objectName) {
        Map<String,EntryPoint> theEntryPoint =
                entryPoints.get(entryPointClass);
        return theEntryPoint.get(objectName);
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends EntryPoint> getAll(
            Class<?> entryPointClass) {
        Map<String,EntryPoint> foundEntryPoints =
                entryPoints.get(entryPointClass);
        return (Collection<? extends EntryPoint>)
                Collections.unmodifiableCollection(
                        foundEntryPoints != null ?
                                entryPoints.get(entryPointClass).values() : Collections.EMPTY_SET);
    }

    @SuppressWarnings("unused")
    public EntryPoint delete(Class<?> entryPointClass,
                             String objectName) {
        Map<String,EntryPoint> theEntryPoint =
                entryPoints.get(entryPointClass);
        return theEntryPoint.remove(objectName);
    }
}
~~~
Registrar 인터페이스의 구현 클래스는 더 이상 SINGLETON일 필요가 없다. static 멤버 변수와 CREATION METHOD, static 메소드들을 인스턴스 메소드로 변경한다.

##### `ProductRepositoryImpl.java`
~~~java
public class ProductRepositoryImpl implements ProductRepository{
    private Registrar registrar;

    public ProductRepositoryImpl() {
    }

    public void setRegistrar(Registrar registrar) {
        this.registrar = registrar;
    }

    public void save(Product product) {
        registrar.add(Product.class, product);
    }

    public Product find(String productName) {
        return (Product)registrar.get(Product.class, productName);
    }
}
~~~
이제 ProductRepositoryImpl 클래스는 Registrar 인터페이스에 의존 가능하다. setter injection을 위한 메소드를 추가한다.

마지막으로 annotation을 통한 의존성 주입을 설정해준다.
- 원문에서는 Setter DI 수행 및 xml파일로 의존성을 설정해주었으나 여기서는 편의상 annotation 방식을 사용합니다. (코드 수정됨)
    1. 인터페이스 구현체를 Bean 등록한다. (Component)
    2. 이를 사용하는 곳에서 선언시 DI 한다. (Autowired)
~~~ java
// EntryPointRegistrar.java
@Component("registrar")
public class EntryPointRegistrar implements Registrar{}

// ProductRepositoryImpl.java
@Component("productRepository")
public class ProductRepositoryImpl implements ProductRepository{

    @Autowired
    private Registrar registrar;
}

// OrderLineItem.java
public class OrderLineItem {
    private Product product;
    private int quantity;

    @Autowired
    private ProductRepository productRepository;
}
~~~

![](http://pds12.egloos.com/pds/200812/24/18/f0081118_4951b0f771e24.jpg)

- 최종적인 약한 결합도 형태

---
### 관점의 변경 <a id="2"></a>
[목차로](#0)

#### Refactoring
ProductRepository와 마찬가지로 CustomerRepository, OrderRepository에 EXTRACT INTERFACE리팩토링을 수행한다. 

- Code는 생략한다.

#### AOP
Spring 컨테이너 외부에서 생성되는 객체 *(여기서는 OrderLineItem)* 에 의존성 주입을 제공하는 가장 효과적인 방법은 AOP를 적용하는 것이다. AOP를 사용하면 시스템의 핵심 관심사(Core Concerns)와 횡단 관심사(Cross-Cutting Concerns)의 분리를 통해 결합도가 낮고 재사용 가능한 시스템을 개발할 수 있다.

Spring은 프록시 기반 메커니즘에 AspectJ를 통합하여 이를 유연하게 지원한다. 그 대표적인 것이 Spring 컨테이너 외부에서 생성되는 도메인 객체에 Spring 컨테이너에서 관리하는 빈을 의존 삽입할 수 있도록 해주는 기능이다.

이것은 AspectJ 5부터 지원되는 LTW 기능을 사용하여 클래스 로더가 클래스 로드 시 바이트 코드를 수정하여 Spring 빈을 삽입하는 것이다. 따라서 Order에서 new 연산자를 사용하여 OrderLineItem을 생성하더라도 ProductRepositoryImpl를 의존 삽입하는 것이 가능해진다.

#### LTW 
> 여기부터는 [원문](http://aeternum.egloos.com/1316318)의 내용이 아닙니다.

스프링에서 제공하는 AOP는 IOC 대상에 포함되는 객체에만 사용 가능하다. 스프링 프레임워크가 부팅되면 IOC 대상에 포함시킬 클래스들을 찾아 컨테이너에 빈으로 등록하며 이때 해당 빈들은 모두 프록시 객체로 감싸지게 된다. 실제 어떤 기능이 실행되도 프록시 객체를 통해 실행되는 점을 이용해 구체화된 기술이 AOP이며 이를 RTW(런타임 타임 위빙)이라 부른다. LTW(로드 타임 위빙)은 클래스로드 타임에 위빙하여 일반 클래스 또한 스프링에 의해 AOP 처리가 가능하도록 하는 라이브러리이다.
- Weaving 이란? 타겟을 감싸는 객체(Proxy)를 생성하는 과정

##### `build.gradle`
~~~java
dependencies {
    // ...
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    implementation 'org.springframework:spring-instrument'
}

File instrumentLibPath = file{
    return sourceSets.getByName("main").compileClasspath.find {
        cls -> return cls.getName().contains("spring-instrument")
    }
}

test{
    jvmArgs "-javaagent:${instrumentLibPath}"
    useJUnitPlatform()
}

bootRun {
    doFirst {
        jvmArgs "-javaagent:${instrumentLibPath}"
    }
}
~~~
1. LTW 의존성 추가: spring-instrument
2. jvm로드를 위해 spring-instrument 라이브러리 경로를 jvm agent 옵션으로 넘겨준다.

~~~java
 @Configuration
 @EnableSpringConfigured
 @EnableLoadTimeWeaving
~~~
3. Application 최소 설정
    - @EnableLoadTimeWeaving은 LTW을 가능하게 함
    - @EnableSpringConfigured는 일반클래스 또한 스프링 설정을 DI받는게 가능하도록 함

~~~java
@Configurable(autowire = Autowire.BY_TYPE, value = "orderLineItem", preConstruction = true)
public class OrderLineItem {
    private Product product;
    private int quantity;

    @Autowired
    private ProductRepository productRepository;
~~~
4. @Configurable 어노테이션 적용
 value에는 Spring 빈 컨텍스트에 정의한 OrderLineItem 빈의 id를 정의한다. preConstruction의 값을 true로 한 이유는 이 값을 true로 설정하지 않으면 기본적으로 생성자 호출이 끝난 후 의존성이 주입되기 때문이다. 따라서 위와 같이 생성자 내부에서 주입될 대상 객체를 호출하는 경우 NullPointerException이 발생하게 된다. 이를 방지하기 위해서는 생성자가 호출되기 전에 의존성이 주입되도록 preConstruction의 값을 true로 설정해 주어야 한다.

 #### 결과
 1. 도메인 클래스들은 REPOSITORY의 인터페이스에만 의존할 뿐 실제적인 구현 클래스에 의존하지 않게 되었다.
 2. 메모리 컬렉션을 처리하는 REPOSITORY 내부 구현을 DB에 접근하는 REPOSITORY로 대체하더라도 다른 클래스에 영향을 미치지 않을 것이다.
 3. 도메인 클래스가 REPOSITORY 인터페이스에만 의존하기에 Mock 객체를 사용하여 DB없이도 테스트가 가능해졌다.

 ---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 5부](http://aeternum.egloos.com/1265684)  
[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 6부](http://aeternum.egloos.com/1284544)  
[Domain-Driven Design의 적용-3.Dependency Injection과 Aspect-Oriented Programming 7부](http://aeternum.egloos.com/1316318)  