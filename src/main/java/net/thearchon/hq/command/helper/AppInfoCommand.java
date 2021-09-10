package net.thearchon.hq.command.helper;

import net.thearchon.hq.Player;
import net.thearchon.hq.command.Command;

public class AppInfoCommand extends Command {

    @Override
    public void execute(Player player, String[] args) {
        RegisterCommand.message(player,
                "&7Steps on how to access &cArchonHQ&7:"
                        + "\n &a1. &7Download: &c(coming soon)"
                        + "\n &a2. &7Enter your Minecraft &3&ousername&7."
                        + "\n &a3. &7Enter your &3&opassword &7from /register."
                        + "\n &a4. &7Click connect!");
    }
}
