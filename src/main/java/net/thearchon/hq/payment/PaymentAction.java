package net.thearchon.hq.payment;

import net.thearchon.hq.service.buycraft.PackageCommand;

public interface PaymentAction {

    void handle(PackageCommand command);
}
