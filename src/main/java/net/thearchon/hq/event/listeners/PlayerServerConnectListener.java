package net.thearchon.hq.event.listeners;

import net.thearchon.hq.Player;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.event.Listener;

public interface PlayerServerConnectListener extends Listener {

    void connectedTo(Player player, BukkitClient server);
}
