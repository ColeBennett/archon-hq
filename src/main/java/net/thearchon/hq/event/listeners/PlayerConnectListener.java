package net.thearchon.hq.event.listeners;

import net.thearchon.hq.Player;
import net.thearchon.hq.event.Listener;

public interface PlayerConnectListener extends Listener {

    void playerConnected(Player player);
}
