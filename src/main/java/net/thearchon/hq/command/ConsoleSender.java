package net.thearchon.hq.command;

import net.thearchon.hq.Archon;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;

public final class ConsoleSender extends Player {

    public static final ConsoleSender INSTANCE = new ConsoleSender();

    private ConsoleSender() {
        super(-1, null, null, null, null, null);
    }

    @Override
    public void error(String message) {
        Archon.getInstance().getLogger().warning(message);
    }
    
    @Override
    public void message(String message) {
        Archon.getInstance().getLogger().info(message);
    }

    @Override
    public void message(String... messages) {
        for (String message : messages) {
            message(message);
        }
    }

    @Override
    public boolean isStaff() {
        return true;
    }

    @Override
    public boolean hasPermission(Rank check) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return toString();
    }

    @Override
    public Rank getRank() {
        return Rank.OWNER;
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public String toString() {
        return Message.CONSOLE.toString();
    }
}
