package net.thearchon.hq;

public enum ServerType {

    APP,
    BUNGEE,
    LOBBY,
    SETUP,

    /**
     * SMP Servers
     */
    FACTIONS,
    PRISON,

    /**
     * Minigames
     */
    WARFARE,
    ARCADE,
    SKYWARS,
    SG,
    SGB,
    UHC,
    UHC_HOSTED,
    RANKUP,

    EVENTS,

    /**
     * Minigame Lobbies
     */
    WARFARE_LOBBY,
    ARCADE_LOBBY,
    SKYWARS_LOBBY,
    SG_LOBBY,
    SGB_LOBBY,
    UHC_LOBBY;

    public static final ServerType[] ALL_LOBBIES = {
            LOBBY, ARCADE_LOBBY, SKYWARS_LOBBY,
            SG_LOBBY, UHC_LOBBY, SGB_LOBBY, WARFARE_LOBBY};

    public static final ServerType[] ALL_MINIGAMES = {
            ARCADE, SKYWARS, SG, SGB, UHC, RANKUP, WARFARE};

    public static final ServerType[] ALL_SMP = {
            FACTIONS, PRISON};

    public boolean isLobbyType() {
        for (ServerType type : ALL_LOBBIES) {
            if (type == this) {
                return true;
            }
        }
        return false;
    }

    public boolean isMinigameType() {
        for (ServerType type : ALL_MINIGAMES) {
            if (type == this) {
                return true;
            }
        }
        return false;
    }

    public boolean isSmpType() {
        for (ServerType type : ALL_SMP) {
            if (type == this) {
                return true;
            }
        }
        return false;
    }
}
