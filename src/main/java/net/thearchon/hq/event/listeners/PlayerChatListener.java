package net.thearchon.hq.event.listeners;

import net.thearchon.hq.Player;
import net.thearchon.hq.event.Listener;

public interface PlayerChatListener extends Listener {

    void playerChat(Player player, String message);
}
