package net.thearchon.hq.util.unused.subscriptions.actions;

import net.thearchon.hq.Archon;
import net.thearchon.hq.payment.PaymentAction;
import net.thearchon.hq.service.buycraft.PackageCommand;

public class SubscriptionAdd implements PaymentAction {

    private final Archon server;

    public SubscriptionAdd(Archon server) {
        this.server = server;
    }

    @Override
    public void handle(PackageCommand command) {
//        Subscription sub = Subscription.valueOf(command.getArgs()[1]);
//        server.getSubscriptionManager().update(command.getUuid(), command.getUsername(), sub);
    }
}
