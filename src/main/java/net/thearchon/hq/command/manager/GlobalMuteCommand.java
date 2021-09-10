package net.thearchon.hq.command.manager;

import net.thearchon.hq.*;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.Protocol;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GlobalMuteCommand extends Command {

    private boolean chatEnabled = true;
    private ScheduledFuture<?> task;

    public GlobalMuteCommand(Archon archon) {
        super(archon, "/gmute [minutes]");
    }
    
    @Override
    public void execute(Player player, String[] args) {
        if (!chatEnabled) {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
            enableChat();
            return;
        }
        if (args.length != 1) {
            player.error(getSyntax());
            return;
        }

        int mins;
        try {
            mins = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.error("Invalid input: " + args[0]);
            return;
        }
        archon.sendAll(Protocol.DISABLE_CHAT.construct(), ServerType.BUNGEE);
        task = archon.runTaskLater(this::enableChat, mins, TimeUnit.MINUTES);
        archon.alert(Message.PREFIX + "&c&lChat has been globally disabled for &6&l" + mins + Util.pluralize(" minute", "s", mins) + "&c&l!");
    }

    private void enableChat() {
        chatEnabled = true;
        archon.sendAll(Protocol.ENABLE_CHAT.construct(), ServerType.BUNGEE);
        archon.alert("&a&lChat is now enabled!");
    }
}
