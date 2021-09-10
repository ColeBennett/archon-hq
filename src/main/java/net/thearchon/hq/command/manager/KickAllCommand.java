package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.util.Util;

public class KickAllCommand extends Command {

    public KickAllCommand(Archon archon) {
        super(archon, "/kickall [server, empty=current]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 1) {
            player.error(getSyntax());
            return;
        }
        BukkitClient client;
        if (args.length == 0) {
            client = player.getCurrentServer();
        } else {
            String input = args[0];
            client = archon.getBukkitClientHolder(input);
            if (client == null) {
                player.error(Message.SERVER_NOT_FOUND.format(input));
                return;
            }
            if (!client.isActive()) {
                player.error(Message.SERVER_OFFLINE.format(input));
                return;
            }
        }

        LobbyHandler h = archon.getHandler(ServerType.LOBBY);
        int kicked = 0;
        for (Player p : archon.getPlayers()) {
            BukkitClient cl = p.getCurrentServer();
            if (cl != null && cl.equals(client)) {
                h.toAvailableServer(p);
                kicked++;
            }
        }
        player.message("&6Kicked &a" + kicked + " &6" + Util.pluralize("player", "s", kicked) +
                " &6from &7" + client.getDisplayName() + " &6to &7lobby&6.");
    }  
}