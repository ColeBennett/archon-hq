package net.thearchon.hq.security;

import com.google.gson.reflect.TypeToken;
import net.thearchon.hq.Archon;
import net.thearchon.hq.util.io.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class HostBlocker {

    static final boolean ENABLED = false;

    private final Type typeOfHashMap = new TypeToken<Map<String, String>>(){}.getType();

    private final Archon archon;
    private final Map<String, HostCacheRecord> cache = new ConcurrentHashMap<>();

    public HostBlocker(Archon archon) {
        this.archon = archon;

        archon.getDataSource().getLogDb().sync().execute("CREATE TABLE IF NOT EXISTS xioax_cache (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "date DATETIME NOT NULL, " +
                "host_ip BOOLEAN NOT NULL, " +
                "org VARCHAR(60), " +
                "cc VARCHAR(10)," +
                "query_time MEDIUMINT UNSIGNED NOT NULL);");
        archon.getDataSource().getLogDb().sync().execute("CREATE TABLE IF NOT EXISTS blocked_logins (" +
                "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "date DATETIME NOT NULL, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "ip_address VARCHAR(39) NOT NULL, " +
                "ip_host_name VARCHAR(60) NOT NULL, " +
                "virt_host_name VARCHAR(60), " +
                "org VARCHAR(60), " +
                "cc VARCHAR(10));");

        try (Statement stmt = archon.getDataSource().getLogDb().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM xioax_cache;")) {
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                HostCacheRecord record = new HostCacheRecord(uuid);
                record.setHostIp(rs.getBoolean("host_ip"));
                record.setOrg(rs.getString("org"));
                record.setCc(rs.getString("cc"));
//                record.setCc(rs.getString("country"));
                cache.put(uuid, record);
            }
        } catch (SQLException e) {
            archon.getLogger().log(Level.SEVERE, "Failed to cache host blocker cache", e);
        }

        archon.runTaskTimer(() -> {

        }, 0, 1, TimeUnit.MINUTES);
    }

    public boolean check(String uuid, String ip, String ipHostName, String virtHostName) {
        if (isHotspotShield(ipHostName)) {
            log(uuid, ip, ipHostName, virtHostName, "Hotspot Shield", null);
            return true;
        }

//        if (ENABLED) {
//            HostCacheRecord record = cache.get(uuid);
//            if (record == null) {
//                record = fetchApiData(uuid, ip);
//                if (record == null) {
//                    return false;
//                }
//            }
//            if (record.isHostIp()) {
//                log(uuid, ip, ipHostName, virtHostName, record.getOrg(), record.getCc());
//                return true;
//            }
//        }
        return false;
    }

    private boolean isHotspotShield(String ipHostName) {
        return ipHostName.toLowerCase().contains("anchorfree");
    }

    private HostCacheRecord fetchApiData(String uuid, String ip) {
        HostCacheRecord record = null;
        String url = xioaxApiUrl(ip);
        try {
            long start = System.currentTimeMillis();
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(2500);
            conn.setReadTimeout(7500);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            long queryTime = System.currentTimeMillis() - start;

            debug("Querying: " + url);
            debug("raw: " + sb.toString());

            Map<String, Object> data = JsonUtil.load(sb.toString(), typeOfHashMap);
            if (data.isEmpty()) {
                // something went wrong
                debug("something went wrong: " + url);
                return null;
            }

            boolean status = data.get("status").equals("success");
            if (!status) {
                // service was not able to process the request
                debug("service was not able to process the request: " + url);
                return null;
            }

            boolean hostIp = Boolean.parseBoolean((String) data.get("host-ip"));
            String org = (String) data.get("org");
            String cc = (String) data.get("cc");
            String country = (String) data.get("country");
            String city = (String) data.get("city");
            int postal = (Integer) data.get("postal");

//            {
//                "status":"success",
//                    "package":"Professional",
//                    "ipaddress":"189.230.198.152",
//                    "host-ip":false,
//                    "org":"Uninet S.A. de C.V.",
//                    "country":{
//                "name":"Mexico",
//                        "code":"MX"
//            },
//                "subdivision":{
//                "name":"Estado de Mexico",
//                        "code":"MEX"
//            },
//                "city":"Zumpango",
//                    "postal":"55646",
//                    "location":{
//                "lat":19.7978,
//                        "long":-99.1017
//            }
//            }


            debug("status: " + status);
            debug("hostIp: " + hostIp);
            debug("org: " + org);
            debug("cc: " + cc);
            debug("country: " + country);
            debug("query time: " + queryTime);

            record = new HostCacheRecord(uuid);
            record.setHostIp(hostIp);
            record.setOrg(org);
            record.setCc(cc);

            cache(record, queryTime);
            cache.put(uuid, record);
        } catch (IOException e) {
            archon.getLogger().log(Level.SEVERE, "Failed to fetch data from " + url, e);
        }
        return record;
    }

    private void debug(Object msg) {
        archon.getLogger().warning("[HostBlocker] " + msg);
    }

    private String xioaxApiUrl(String ip) {
        return "http://tools.xioax.com/networking/ip/" + ip + '/' + "ADz83Lh7jUhpsqKkZaEFSns4RmnBtR";
    }

    private void cache(HostCacheRecord record, long queryTime) {
        archon.getDataSource().getLogDb().execute("INSERT INTO xioax_cache VALUES(?, CONVERT_TZ(NOW(), @@session.time_zone, 'UTC'), ?, ?, ?, ?)",
                record.getUuid(), record.isHostIp(), record.getOrg(), record.getCc(), queryTime);
    }

    private void log(String uuid, String ip, String ipHostName, String virtHostName, String org, String cc) {
        archon.getDataSource().getLogDb().execute("INSERT INTO blocked_logins VALUES(NULL, CONVERT_TZ(NOW(), @@session.time_zone, 'UTC'), ?, ?, ?, ?, ?, ?)",
                uuid, ip, ipHostName, virtHostName, org, cc);
    }

    /*

     Example JSON response:
     {
         "status": "success",
         "host-ip": true,
         "org": "RAMNODE - RamNode LLC",
         "cc": "US"
     }

     status -> returns "success" or "failed" whether or not we were able to process your IP request.
     msg -> returns string which contains information regarding why the status is failed.
     host-ip -> returns boolean value if the requested IP belongs to a hosting organization or not.
     org -> returns the organization who owns the IP address.
     cc -> returns the country code.

     */
}
