package net.thearchon.hq.util.unused.jackpot;

public class Participant {

    private final String uuid, name, server;

    Participant(String uuid, String name, String server) {
        this.uuid = uuid;
        this.name = name;
        this.server = server;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Participant
                && ((Participant) o).uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
