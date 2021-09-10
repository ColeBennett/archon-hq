package net.thearchon.hq.command.all;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Settings;
import net.thearchon.hq.command.Command;

import java.util.ArrayList;
import java.util.List;

public class VoteInfoCommand extends Command {

    public VoteInfoCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<String> msg = new ArrayList<>();
        msg.add("&8" + Message.BAR);
        msg.add("&eNote: &bYou can once per day on each link!");
        msg.add("");

        Settings settings = archon.getSettings();
        int link = 1;
        for (String site : settings.getKeys("votingLinks")) {
            String url = settings.getString("votingLinks." + site);
            msg.add("&3#" +  link++ + ": &a" + url + " &7&o(" + site + ')');
        }

        msg.add("");
        msg.add("&2Use &6/claim &2while on a faction server to receive rewards!");
        msg.add("&8" + Message.BAR);
        player.message(msg);
    }
}
