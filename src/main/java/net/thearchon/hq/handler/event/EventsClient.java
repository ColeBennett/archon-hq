package net.thearchon.hq.handler.event;

import net.thearchon.hq.Archon;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;

public class EventsClient extends BukkitClient {

    public EventsClient(Archon archon) {
        super(archon, ServerType.EVENTS, "events");
    }
}
