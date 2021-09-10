package net.thearchon.hq;

public class PlayerInfo {

    private final int id;
    private final String uuid;
    private final String name;
    
    public PlayerInfo(int id, String uuid, String name) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
    }

    /**
     * Archon id number of the player.
     * @return archon id
     */
    public int getId() {
        return id;
    }

    /**
     * Minecraft UUID of the player.
     * @return uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Minecraft username of the player.
     * @return username
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PlayerInfo(id: " + id + ", uuid: " + uuid + ", name: " + name + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof PlayerInfo)) return false;
        return ((PlayerInfo) o).id == id;
    }
}
