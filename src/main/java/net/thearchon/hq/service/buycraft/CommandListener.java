package net.thearchon.hq.service.buycraft;

import net.thearchon.hq.service.Listener;

public interface CommandListener extends Listener {

    void commandReceived(PackageCommand command);
}
