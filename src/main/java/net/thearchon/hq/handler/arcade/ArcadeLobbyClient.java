package net.thearchon.hq.handler.arcade;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.IdBukkitClient;

public class ArcadeLobbyClient extends IdBukkitClient {

    public ArcadeLobbyClient(Archon archon, int id) {
        super(archon, ServerType.ARCADE_LOBBY, "arcadelobby", id);
    }
}
