package net.thearchon.hq.handler.setup;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;

public class SetupClient extends BukkitClient {

    public SetupClient(Archon archon) {
        super(archon, ServerType.SETUP, "setup");
    }
}
