package net.thearchon.hq.punish;

public enum Punishment {

    KICK("Kick"),
    MUTE("Mute"),
    TEMPBAN("Tempban"),
    BAN("Ban"),
    UNBAN("Unban"),
    UNMUTE("Unmute"),
    WARNING("Warning");

    private final String name;

    Punishment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
