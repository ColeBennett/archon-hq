package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;

public class BetaCommand extends Command {

    private String serverName;
    private int slots;

    public BetaCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            if (serverName == null) {
                player.error("This command is currently disabled.");
                return;
            }
            BukkitClient client = archon.getBukkitClientHolder(serverName);
            if (!client.isActive()) {
                player.error("Server is currently offline.");
                return;
            }
            if (client.getOnlineCount() >= slots) {
                player.error("Slots have already filled up! Sorry!");
                return;
            }
            player.connect(client);
        } else {
            if (player.hasPermission(Rank.ADMIN) && args.length == 2) {
                if (serverName != null) {
                    player.message("&cDisabled &7BETA testing for server: &e" + serverName);
                    serverName = null;
                    slots = 0;
                } else {
                    serverName = args[0].toLowerCase();
                    slots = Integer.parseInt(args[1]);
                    player.message("&aEnabled &7BETA testing for server: &e" + serverName + " &a(" + slots + " slots)");
                }
            } else {
                player.error("Syntax: /beta <server-name> <slots>");
            }
        }
    }
}
