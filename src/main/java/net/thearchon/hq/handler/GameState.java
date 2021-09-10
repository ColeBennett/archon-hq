package net.thearchon.hq.handler;

public enum GameState {

    LOADING("Loading"),
    WAITING("Waiting"),
    STARTING("Starting"),
    IN_PROGRESS("In Progress"),
    RESTARTING("Restarting"),
    OFFLINE("Offline");

    private final String name;

    GameState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
