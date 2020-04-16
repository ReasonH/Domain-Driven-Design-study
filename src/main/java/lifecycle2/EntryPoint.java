package lifecycle2;

public class EntryPoint {
    private final String identity;

    public EntryPoint (String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }
}