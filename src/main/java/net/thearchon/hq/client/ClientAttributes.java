package net.thearchon.hq.client;

import net.thearchon.hq.ServerRegion;

public class ClientAttributes {

    private String ipAddress;
    private int port;
    private String location;
    private ServerRegion region;
    private String datacenter;

    public ClientAttributes() {

    }

    public ClientAttributes(String ipAddress, int port, String location,
            ServerRegion region, String datacenter) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.location = location;
        this.region = region;
        this.datacenter = datacenter;
    }

    /**
     * Defined ip address of client.
     * @return ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Defined port of client.
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Geographical location of client ip's host. e.g. New York
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Continent of client ip's host. e.g. North America
     * @return region
     */
    public ServerRegion getRegion() {
        return region;
    }

    /**
     * Name of client ip's dedicated server.
     * @return datacenter
     */
    public String getDatacenter() {
        return datacenter;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRegion(ServerRegion region) {
        this.region = region;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    @Override
    public String toString() {
        return "ClientAttributes(ip: " + ipAddress
                + ", port: " + port
                + ", location: " + location
                + ", region: " + region.getName()
                + ", datacenter: " + datacenter + ')';
    }
}
