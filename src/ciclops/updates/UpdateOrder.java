package ciclops.updates;

public enum UpdateOrder {
    CREATE_BUCKETS(20);

    private final int order;

    UpdateOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
