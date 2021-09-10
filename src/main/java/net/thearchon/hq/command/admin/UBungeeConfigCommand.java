package net.thearchon.hq.command.admin;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.command.Command;
import net.thearchon.hq.util.io.FileUtil;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

import java.io.File;
import java.util.List;

public class UBungeeConfigCommand extends Command {

    public UBungeeConfigCommand(Archon archon) {
        super(archon, "/ubungeeconfig");
    }

    @Override
    public void execute(Player player, String[] args) {
        File f = new File("updates/bungee/config.yml");
        f.getParentFile().mkdir();
        if (!f.exists()) {
            player.error("Config file not found! " + f.getAbsolutePath());
            return;
        }
        List<String> lines = FileUtil.readLines(f);
        BufferedPacket update = Protocol.UPDATE_CONFIG.construct(lines.size());
        for (String line : lines) {
            update.writeString(line);
        }
        int sent = archon.sendAll(update, ServerType.BUNGEE);
        player.message("&6Sent config file to &a" + sent + " &6bungee server(s). Use &7/restart bungee &6for it to take effect.");
    }
}
