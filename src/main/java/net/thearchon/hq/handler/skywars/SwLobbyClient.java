package net.thearchon.hq.handler.skywars;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.IdBukkitClient;

public class SwLobbyClient extends IdBukkitClient {

    public SwLobbyClient(Archon archon, int id) {
        super(archon, ServerType.SKYWARS_LOBBY, "skywarslobby", id);
    }
}
