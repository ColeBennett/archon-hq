package net.thearchon.hq.pool;

import net.thearchon.hq.client.BukkitClient;

import java.util.HashMap;
import java.util.Map;

public class Machine {

    private final String name;
    private final String ip;
    private final int memory;
    private final Map<String, BukkitClient> servers;

    public Machine(String name, String ip, int memory) {
        this.name = name;
        this.ip = ip;
        this.memory = memory;

        servers = new HashMap<>();
    }

    /**
     * Name of machine, e.g. Chicago-1.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * IP address of machine.
     * @return ip address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Memory size of machine, e.g. 32GB.
     * @return memory size in gigabytes
     */
    public int getMemory() {
        return memory;
    }

    /**
     * Map of servers currently allocated to this machine.
     * @return server map
     */
    public Map<String, BukkitClient> getServers() {
        return servers;
    }
}
