package ciclops.updates;

public enum UpdateOrder {
    CREATE_BUCKETS(20),
    CREATE_WEBHOOK_BUCKET(21),
    CREATE_SETTINGS_BUCKET(22);

    private final int order;

    UpdateOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
