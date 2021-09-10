package net.thearchon.hq.command;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;

import java.util.*;
import java.util.logging.Level;

public abstract class BaseCommand extends Command implements ICommandManager {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public BaseCommand(Archon archon) {
        super(archon);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            displayCommands(player);
            return;
        }
        Command command = commands.get(args[0].toLowerCase());
        if (command != null) {
            args = Arrays.copyOfRange(args, 1, args.length);
            if (command.getPermission() == null) {
                command.execute(player, args);
                return;
            }
            if (player.hasPermission(command.getPermission())) {
                command.execute(player, args);
            } else {
                player.message(Message.UNKNOWN_COMMAND);
            }
        } else {
            displayCommands(player);
        }
    }

    @Override
    public void register(String name, Command command, Rank permission) {
        command.setPermission(permission);
        commands.put(name, command);
    }

    @Override
    public void register(String name, Command command, Rank permission, String... aliases) {
        command.setPermission(permission);
        commands.put(name, command);
        for (String alias : aliases) {
            commands.put(alias, command);
        }
    }

    @Override
    public void unregister(String name) {
        commands.remove(name);
    }

    @Override
    public void unregisterAll() {
        commands.clear();
    }

    @Override
    public boolean isCommand(String name) {
        return commands.containsKey(name);
    }

    @Override
    public Command getCommand(String name) {
        return commands.get(name);
    }

    @Override
    public Set<String> getCommandNames() {
        return commands.keySet();
    }

    @Override
    public ConsoleSender getConsoleSender() {
        return ConsoleSender.INSTANCE;
    }

    @Override
    public void setPermission(String name, Rank permission) {
        Command command = commands.get(name);
        if (command == null) {
            throw new NullPointerException(name);
        }
        command.setPermission(permission);
    }

    @Override
    public void runCommand(String cmdName, Player player, String[] args) {
        Command command = commands.get(cmdName);
        if (command == null) {
            archon.getLogger().info("Unknown command: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "]");
            return;
        }
        if (command.getPermission() == null) {
            player.error("Command '" + cmdName + "' does not have a permission set.");
            return;
        }
        if (player.hasPermission(command.getPermission())) {
            try {
                archon.getLogger().info("Executing: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "] ...");
                long start = System.currentTimeMillis();
                command.execute(player, args);
                long duration = System.currentTimeMillis() - start;
                archon.getLogger().info("Executed: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "] - Took " + duration + " ms");
                if (duration >= 1000) {
                    archon.getLogger().warning("Command execution took too long (" + duration + " ms): " + cmdName);
                }
            } catch (Exception e) {
                player.error("An error occured while executing command: " + cmdName);
                archon.getLogger().log(Level.SEVERE, "Failed to execute: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "]", e);
            }
        } else {
            player.message(Message.UNKNOWN_COMMAND);
            archon.getLogger().warning("Permission denied: [command: " + cmdName + ", player: " + player + ", args: " + Arrays.toString(args) + "]");
        }
    }

    protected abstract void displayCommands(Player player);
}
