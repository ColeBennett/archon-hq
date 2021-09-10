package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.LobbyClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.handler.LobbyHandler;

import java.util.ArrayList;
import java.util.List;

public class LobbyListCommand extends Command {

    public LobbyListCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<String> msg = new ArrayList<>();
        for (LobbyClient lobby : ((LobbyHandler) archon.getHandler(ServerType.LOBBY)).getClients()) {
            msg.add("&3" +lobby.getServerName() + " &7[" + lobby.getOnlineCount() + "/" + lobby.getSlots() + "] " + (lobby.isActive() ? "&aONLINE" : "&4OFFLINE") + "&7: " + lobby.getPlayers());
        }
        msg.add("&a" + ((LobbyHandler) archon.getHandler(ServerType.LOBBY)).getOnlineCount() + " &7on lobbies");
        player.message(msg);
    }   
}
