package net.thearchon.hq.event.listeners;

import net.thearchon.hq.Player;
import net.thearchon.hq.event.Listener;

public interface PlayerDisconnectListener extends Listener {

    void playerDisconnected(Player player);
}
