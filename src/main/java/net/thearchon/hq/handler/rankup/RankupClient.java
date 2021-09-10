package net.thearchon.hq.handler.rankup;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;

public class RankupClient extends BukkitClient {

    public RankupClient(Archon archon) {
        super(archon, ServerType.RANKUP, "rankup");
    }
}
