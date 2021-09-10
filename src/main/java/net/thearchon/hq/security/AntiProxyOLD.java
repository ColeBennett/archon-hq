package net.thearchon.hq.security;

//import org.xbill.DNS.Lookup;
//import org.xbill.DNS.Record;

import java.util.concurrent.CopyOnWriteArrayList;

public class AntiProxyOLD {

    private boolean honeypot = false;
    private String honeypotKey = "";
    private boolean hotspotShield = false;

    private final CopyOnWriteArrayList<String> servers = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> whitelist = new CopyOnWriteArrayList<>();

    public AntiProxyOLD() {

    }

//    public void onLogin(LoginEvent e) {
//        if (whitelist.contains(e.getConnection().getName().toLowerCase())) {
//            return;
//        }
//        InetAddress inet = e.getConnection().getAddress().getAddress();
//        if (hotspotShield && inet.getHostName().toLowerCase().contains("anchorfree")) {
//            getLogger().info("Player: " + e.getConnection().getName() + " tried to login with Hotspot Shield (" + inet.getHostName() + ").");
//            e.setCancelled(true);
//            e.setCancelReason("You are not allowed to join from this IP.");
//            return;
//        }
//        String ip = inet.getHostAddress();
//        Entry<Boolean, String> result = checkIp(ip);
//        if (result.getKey()) {
//            e.setCancelled(true);
//            e.setCancelReason("You are not allowed to join from this IP.");
//        }
//    }
//
//    private Entry<Boolean, String> checkIp(String ip) {
//        try {
//            InetAddress address = InetAddress.getByName(ip);
//            if (!address.isLoopbackAddress()) {
//                String[] split = ip.split("\\.");
//                StringBuilder lookup = new StringBuilder();
//                for (int i = split.length - 1; i >= 0; i--) {
//                    lookup.append(split[i]);
//                    lookup.append(".");
//                }
//                String backwards = lookup.toString();
//                for (String s : servers) {
//                    Record[] records = new Lookup(backwards + s, 255).run();
//                    if (records != null) {
//                        getLogger().info("IP address (" + ip + ") was found in " + s);
//                        return new SimpleEntry<>(true, "IP address (" + ip + ") was found in " + s);
//                    }
//                }
//                if (honeypot && !honeypotKey.isEmpty() && new Lookup(honeypotKey + "." + backwards + "dnsbl.httpbl.org.", 255).run() != null) {
//                    getLogger().info("IP address (" + ip + ") was found in Project Honeypot");
//                    return new SimpleEntry<>(true, "IP address (" + ip + ") was found in Project Honeypot");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new SimpleEntry<>(false, "IP address was not found in any blacklist");
//    }
}
