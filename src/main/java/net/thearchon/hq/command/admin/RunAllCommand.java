package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.nio.Protocol;

import java.util.concurrent.TimeUnit;

public class RunAllCommand extends Command {

    public RunAllCommand(Archon archon) {
        super(archon);
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.error(getSyntax());
            return;
        }
        String server = args[0];

        StringBuilder buf = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            buf.append(args[i]);
            buf.append(' ');
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 1);
        }
        String command = buf.toString();

        BukkitClient s = archon.getBukkitClient(server);
        if (s == null) {
            player.error(s + " not found");
            return;
        }

        player.message("&dRunning all for" + s + ". DO NOT RESTART SERVER OR RUN THIS AGAIN.");

        int i = 0;
        for (Player p : s.getPlayers()) {
            archon.runTaskLater(() -> {
                s.send(Protocol.EXEC_COMMAND.construct(command.replace("{name}", p.getName())));
            }, i, TimeUnit.MILLISECONDS);
            i += 50;
        }
    }
}
