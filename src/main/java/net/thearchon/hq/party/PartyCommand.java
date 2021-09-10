package net.thearchon.hq.party;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;
import net.thearchon.hq.command.BaseCommand;
import net.thearchon.hq.party.commands.*;

public class PartyCommand extends BaseCommand {

    public PartyCommand(Archon archon) {
        super(archon);

        register("host", new PartyHostCommand(), Rank.DEFAULT);
        register("end", new PartyEndCommand(), Rank.DEFAULT);
        register("play", new PartyPlayCommand(), Rank.DEFAULT);
        register("kick", new PartyKickCommand(), Rank.DEFAULT);
        register("invite", new PartyInviteCommand(archon), Rank.DEFAULT);
        register("accept", new PartyAcceptCommand(), Rank.DEFAULT);
        register("leave", new PartyLeaveCommand(), Rank.DEFAULT);
        register("chat", new PartyChatCommand(), Rank.DEFAULT);
        register("info", new PartyInfoCommand(), Rank.DEFAULT);
    }

    @Override
    protected void displayCommands(Player player) {
        player.message(
                "&c&lArchon Party Commands:",
                "&6&o/party host &7- Create a new party &a(VIP or higher required)",
                "&6&o/party end &7- End your current party if you're the host",
                "&6&o/party play <game> &7- Send your party members to a game",
                "&6&o/party kick <player> &7- Kick a player from your party if you're the host",
                "&6&o/party invite <player> &7- Invite a player to your party if you're the host",
                "&6&o/party accept &7- Accept an invitation to a party",
                "&6&o/party leave &7- Leave your current party",
                "&6&o/party chat &7- Toggle party chat",
                "&6&o/party info &7- View who is in your party");
    }
}
