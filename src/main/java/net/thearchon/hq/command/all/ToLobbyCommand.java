package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.handler.Handler;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.handler.GameLobbyHandler;

public class ToLobbyCommand extends Command {

    public ToLobbyCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        ServerType type;
        try {
            ServerType curr = player.getCurrentServer().getType();
            type = ServerType.valueOf(curr.name() + "_LOBBY");
        } catch (IllegalArgumentException e) {
            type = ServerType.LOBBY;
        }
        Handler handler = archon.getHandler(type);
        if (handler instanceof LobbyHandler) {
            ((LobbyHandler) handler).toAvailableServer(player);
        } else if (handler instanceof GameLobbyHandler<?, ?>) {
            ((GameLobbyHandler<?, ?>) handler).toAvailableServer(player);
        }
    }
}
