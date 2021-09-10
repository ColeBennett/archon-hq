package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.client.BukkitClient;

public class SendCommand extends Command {

    public SendCommand(Archon archon) {
        super(archon, "/send <player|current> <server>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            player.error(getSyntax());
            return;
        } 
        String name = args[0];
        String serv = args[1].toLowerCase();
        if (name.equalsIgnoreCase("current")) {
            int sent = 0;
            String curr = player.getCurrentServer().getServerName();
            for (Player p : archon.getPlayers()) {
                BukkitClient cl = p.getCurrentServer();
                if (cl != null && cl.getServerName().equals(curr)) {
                    p.connect(serv);
                    sent++;
                }
            }
            player.message("&7Sent &b" + sent + " &7player(s) to server: &a" + serv);
        } else {
            Player target = archon.matchOnlinePlayer(args[0]);
            if (target != null) {
                target.connect(serv);
                player.message("&7Sent " + target.getDisplayName() + " &7to server: &a" + serv + " &c(Proxy: " + target.getProxy().getId() + ")");
            } else {
                player.error(Message.PLAYER_NOT_FOUND.format(args[0]));
            }   
        }
    }  
}