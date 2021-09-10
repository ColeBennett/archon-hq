package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.FilePacket;
import net.thearchon.nio.protocol.Packet;

import java.io.File;
import java.util.Set;

public class UpdateCommand extends Command {

    public UpdateCommand(Archon archon) {
        super(archon, "/update <server-type|all> [restart]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || args.length > 2) {
            player.error(getSyntax());
            return;
        }
        String intent = args[0];
        boolean restart = true;
        if (args.length == 2) {
            String bool = args[1].toLowerCase();
            if (bool.equals("true") || bool.equals("false")) {
                restart = Boolean.parseBoolean(bool);
            } else {
                player.error("Invalid boolean value for [restart] argument.");
                return;
            }
        }
        ServerType serverType = null;
        if (!intent.equalsIgnoreCase("all")) {
            try {
                serverType = ServerType.valueOf(intent.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.error("Server type not found: " + intent);
                return;
            }
        }

        File jarFile = new File("updates", "ArchonSuite.jar");
        if (jarFile.exists()) {
            Packet update = new FilePacket(jarFile, new BufferedPacket(1).writeBoolean(restart));
            int count;
            if (serverType == null) {
                Set<Client> clients = archon.getClientsExcluding(ServerType.BUNGEE, ServerType.APP);
                count = clients.size();
                for (Client client : archon.getActiveClients().values()) {
                    if (client instanceof BukkitClient) {
                        ((BukkitClient) client).removePlayers();
                    }
                    archon.send(client, update);
                }
            } else {
                count = archon.sendAll(update, serverType);
            }
            player.message("&7Sent &c" + jarFile.getName() + " &7update to &e" + count + "&7" + (serverType == null ? "" : " " + serverType.name()) + " servers.");
            player.message("&7Data broadcasted: &c" + Util.humanReadableByteCount(jarFile.length() * count, true));
        } else {
            player.error("Jar file not found: " + jarFile.getAbsolutePath());
        }
    }
}
