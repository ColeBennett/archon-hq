package net.thearchon.hq.event.listeners;

import net.thearchon.hq.Player;
import net.thearchon.hq.event.Listener;

public interface PlayerCommandListener extends Listener {

    void playerCommand(Player player, String command, String[] args);
}
