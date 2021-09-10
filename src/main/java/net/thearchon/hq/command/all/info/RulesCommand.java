package net.thearchon.hq.command.all.info;

import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class RulesCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        player.message(Message.PREFIX + "&6Rules: &7https://www.thearchon.net/community/threads/server-rules.3/");
    }
}
