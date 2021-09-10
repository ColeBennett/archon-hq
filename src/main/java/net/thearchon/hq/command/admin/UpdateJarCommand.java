package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.FilePacket;
import net.thearchon.nio.protocol.Packet;

import java.io.File;

public class UpdateJarCommand extends Command {

    public UpdateJarCommand(Archon archon) {
        super(archon, "/update <server-type> <jar> [restart]");
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2 || args.length > 3) {
            player.error(getSyntax());
            return;
        }
        String intent = args[0];
        ServerType serverType = null;
        try {
            serverType = ServerType.valueOf(intent.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.error("Server type not found: " + intent);
            return;
        }

        File dir = new File("updates");
        if (!dir.exists()) {
            dir.mkdir();
        }
        String jar = args[1];
        for (File f : dir.listFiles()) {
            if (f.getName().equalsIgnoreCase(jar)) {
                jar = f.getName();
                break;
            }
        }

        boolean restart = true;
        if (args.length == 3) {
            String bool = args[2].toLowerCase();
            if (bool.equals("true") || bool.equals("false")) {
                restart = Boolean.parseBoolean(bool);
            } else {
                player.error("Invalid value for [restart] argument: " + args[2]);
                return;
            }
        }

        File jarFile = new File("updates", jar);
        if (jarFile.exists()) {
            Packet update = new FilePacket(jarFile, new BufferedPacket(1).writeBoolean(restart));
            int count = archon.sendAll(update, serverType);
            player.message("&7Sent &c" + jarFile.getName() + " &7update to &e" + count + "&7" + (serverType == null ? "" : " " + serverType.name()) + " servers.");
            player.message("&7Data broadcasted: &c" + Util.humanReadableByteCount(jarFile.length() * count, true));
        } else {
            player.error("Jar file not found: " + jarFile.getAbsolutePath());
        }
    }
}
