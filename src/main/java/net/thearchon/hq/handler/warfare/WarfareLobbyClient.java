package net.thearchon.hq.handler.warfare;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.IdBukkitClient;

public class WarfareLobbyClient extends IdBukkitClient {

    public WarfareLobbyClient(Archon archon, int id) {
        super(archon, ServerType.WARFARE_LOBBY, "warfarelobby", id);
    }
}
