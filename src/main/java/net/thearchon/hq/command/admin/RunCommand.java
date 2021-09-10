package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;
import net.thearchon.nio.Protocol;
import net.thearchon.hq.ServerType;

public class RunCommand extends Command {

    public RunCommand(Archon archon) {
        super(archon, "/runcmd <server-type> <command>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.error(getSyntax());
            return;
        }
        ServerType type;
        try {
            type = ServerType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.error("Server type not found: " + args[0]);
            return;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            buf.append(args[i]);
            buf.append(' ');
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 1);
        }

        String command = buf.toString();
        int sent = archon.sendAll(Protocol.EXEC_COMMAND.construct(command), type);
        player.message("&7Executed &6'" + command + "' &7on &e" + sent + " &7" + type + " servers.");
    }
}
