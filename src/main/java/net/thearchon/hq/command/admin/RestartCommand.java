package net.thearchon.hq.command.admin;

import net.thearchon.hq.*;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.handler.LobbyHandler;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RestartCommand extends Command implements Runnable {

    private final Map<ServerType, Integer> groupRestarts = new HashMap<>();
    private final Map<String, BukkitClient> serverRestarts = new HashMap<>();

    private ScheduledFuture<?> task;
    private int counter;

    public RestartCommand(Archon archon) {
        super(archon, "/restart <server type, bungee, all> [minute delay]");
    }

    @Override
    public void run() {
        if (counter >= 60 && (counter % 60) == 0) {
            int minute = counter / 60;
            broadcast("&4Network restarting in &c&l" + minute + " &4minute" + (minute == 1 ? "" : "s") + "!");
//            broadcast("&4This archon will restart in &c&l" + minute + " &4minute" + (minute == 1 ? "" : "s") + "!");
        } else if (counter > 0 && counter < 60) {
            if (counter == 30 || counter == 15 || counter == 10 || counter <= 5) {
                broadcast("&4Network restarting in &c&l" + counter + " &4second" + (counter == 1 ? "" : "s") + "!");
//                broadcast("&4This archon will restart in &c&l" + counter + " &4second" + (counter == 1 ? "" : "s") + "!");
            }
        } else if (counter == 0) {
            archon.sendAll(Protocol.SHUTDOWN.construct());
        }
        counter--;
    }

    private void broadcast(String message) {
        archon.sendAll(Protocol.BROADCAST.construct(Message.PREFIX + message), ServerType.BUNGEE);
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || args.length > 2) {
            player.error(getSyntax());
            return;
        }

        String input = args[0];
        ServerType type = null;
        if (!input.equalsIgnoreCase("all")) {
            try {
                type = ServerType.valueOf(input.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.error("Server type not found: " + input);
                return;
            }
        }

        if (args.length == 2) {
            if (type == ServerType.BUNGEE) {
                int mins = 1;
                try {
                    mins = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.error("Invalid input: " + args[1]);
                    return;
                }

                if (task != null) {
                    task.cancel(true);
                    task = null;
                    player.error("Canceled existing scheduled bungee restart.");
                }
                counter = mins * 60;
                player.message("&6All Bungee instances will restart in &a" + mins + " &6" + Util.pluralize("minute", "s", mins) + '!');
                task = archon.runTaskTimer(this, 1, TimeUnit.SECONDS);
            } else if (type == ServerType.LOBBY) {
                LobbyHandler h = archon.getHandler(ServerType.LOBBY);
                h.restartLobbies();
                player.message("&6All lobbies will now restart one at a time!");
            } else {
                player.error("Scheduled restarts only available for bungee and lobby.");
            }
        } else {
            BufferedPacket packet = Protocol.SHUTDOWN.construct();
            int count;
            if (type == null) {
                Set<Client> clients = archon.getClientsExcluding(ServerType.BUNGEE, ServerType.APP);
                count = clients.size();
                for (Client client : archon.getActiveClients().values()) {
                    if (client instanceof BukkitClient) {
                        ((BukkitClient) client).removePlayers();
                    }
                    archon.send(client, packet);
                }
            } else {
                count = archon.sendAll(packet, type);
            }
            player.message("&7Sent restart request to &e" + count + "&7" + (type == null ? "" : " " + type.name()) + " servers.");
        }
    }

    private static final class RestartTask implements Runnable {
        @Override
        public void run() {

        }
    }
}
