package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class DoubleCoinsCommand extends Command {

    public DoubleCoinsCommand(Archon archon) {
        super(archon);
    }
    
    @Override
    public void execute(Player player, String[] args) {
//        Settings config = archon.getSettings();
//        if (config.getDoubleCredits()) {
//            config.setDoubleCredits(false);
//            BufferedPacket buf = Protocol.COIN_MULTIPLIER_UPDATE.construct(false);
//            for (Client client : archon.getClientsExcluding(ServerType.LOBBY, ServerType.BUNGEE, ServerType.APP)) {
//                archon.send(client, buf);
//            }
//            player.message("&cDisabled &7double coins on all games.");
//        } else {
//            config.setDoubleCredits(true);
//            BufferedPacket buf = Protocol.COIN_MULTIPLIER_UPDATE.construct(true);
//            for (Client client : archon.getClientsExcluding(ServerType.LOBBY, ServerType.BUNGEE, ServerType.APP)) {
//                archon.send(client, buf);
//            }
//            player.message("&aEnabled &7double coins on all games.");
//        }
    }
}
