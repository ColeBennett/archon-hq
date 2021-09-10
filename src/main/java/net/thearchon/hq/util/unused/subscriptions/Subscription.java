package net.thearchon.hq.util.unused.subscriptions;

public enum Subscription {

    ONE_WEEK("1 Week"),
    ONE_MONTH("1 Month"),
    THREE_MONTHS("3 Months"),
    SIX_MONTHS("6 Months");

    private final String name;

    Subscription(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
