package reason.lifecycle;

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
        return (Customer) Registrar.get(Customer.class, customerName);
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
};