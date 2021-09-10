package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.language.Message;
import net.thearchon.nio.Protocol;

public class SetSlotsCommand extends Command {

    public SetSlotsCommand(Archon archon) {
        super(archon, "/setslots <server> <slots>");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            player.error(getSyntax());
            return;
        }

        String input = args[0];
        BukkitClient server = archon.getBukkitClientHolder(input);
        if (server == null) {
            player.error(Message.SERVER_NOT_FOUND.format(input));
            return;
        }

        int slots;
        try {
            slots = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.error("Invalid slot amount: " + args[1]);
            return;
        }

        server.setSlots(slots);
        server.send(Protocol.SERVER_SLOT_UPDATE.construct(slots));
        player.message("&6Set slots to &a" + slots + " &6on &7" + server.getDisplayName());
    }
}
