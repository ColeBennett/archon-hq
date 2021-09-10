package net.thearchon.hq.util.unused.subscriptions;

public class SubscriptionInfo {

    private Subscription subscription;
    private long expiry;

    SubscriptionInfo(Subscription subscription, long expiry) {
        this.subscription = subscription;
        this.expiry = expiry;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }
}
