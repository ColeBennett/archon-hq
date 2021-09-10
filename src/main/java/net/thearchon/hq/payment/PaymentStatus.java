package net.thearchon.hq.payment;

public enum PaymentStatus {

    COMPLETE("Complete"),
    CHARGEBACK("Chargeback"),
    REFUND("Refund"),
    EXPIRY("Expiry");

    private final String name;

    PaymentStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
