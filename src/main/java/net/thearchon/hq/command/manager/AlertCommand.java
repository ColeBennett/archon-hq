package net.thearchon.hq.command.manager;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class AlertCommand extends Command {

    public AlertCommand(Archon archon) {
        super(archon, "/alert <message>");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.error(getSyntax());
            return;
        }
        StringBuilder buf = new StringBuilder(Message.PREFIX.toString());
        buf.append("&c");
        for (String arg : args) {
            buf.append(arg);
            buf.append(' ');
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 1);
        }
        archon.alert(buf.toString());
    }
}
