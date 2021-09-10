package net.thearchon.hq.handler.sg;

import net.thearchon.hq.Archon;
import net.thearchon.hq.client.IdBukkitClient;
import net.thearchon.hq.ServerType;

public class SgLobbyClient extends IdBukkitClient {

    public SgLobbyClient(Archon archon, int id) {
        super(archon, ServerType.SG_LOBBY, "sglobby", id);
    }
}
