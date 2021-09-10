package net.thearchon.hq.command.admin;

import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class EchoCommand extends Command {

    public EchoCommand() {
        super("/echo <text>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.error(getSyntax());
            return;
        }
        StringBuilder buf = new StringBuilder();
        for (String arg : args) {
            buf.append(arg);
            buf.append(' ');
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 1);
        }
        player.message(buf.toString());
    }
}
