package net.thearchon.hq.command;

import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;

import java.util.Set;

public interface ICommandManager {

    void register(String name, Command command, Rank permission);

    void register(String name, Command command, Rank permission, String... aliases);

    void unregister(String name);

    void unregisterAll();

    boolean isCommand(String name);

    Command getCommand(String name);

    Set<String> getCommandNames();

    ConsoleSender getConsoleSender();

    void setPermission(String name, Rank permission);

    void runCommand(String name, Player player, String[] args);

    default void runConsoleCommand(String name, String[] args) {
        runCommand(name, getConsoleSender(), args);
    }
}
