package net.thearchon.hq.handler.arcade;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.GameClient;

public class ArcadeClient extends GameClient {

    private ArcadeGameType gameType;

    public ArcadeClient(Archon archon, int id) {
        super(archon, ServerType.ARCADE, "arcade", id);
    }

    public ArcadeGameType getGameType() {
        return gameType;
    }

    public void setGameType(ArcadeGameType gameType) {
        this.gameType = gameType;
    }
}
