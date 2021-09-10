package net.thearchon.hq.command;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.Rank;

public abstract class Command {

    protected final Archon archon;
    
    private final String syntax;
    private Rank permission;
    
    public Command() {
        this(null, null);
    }
    
    public Command(String syntax) {
        this(null, syntax);
    }
    
    public Command(Archon archon) {
        this(archon, null);
    }

    public Command(Archon archon, String syntax) {
        this.archon = archon;
        this.syntax = syntax;
    }

    public String getSyntax() {
        return syntax;
    }

    public Rank getPermission() {
        return permission;
    }

    public void setPermission(Rank permission) {
        this.permission = permission;
    }
    
    public abstract void execute(Player player, String[] args);
}
