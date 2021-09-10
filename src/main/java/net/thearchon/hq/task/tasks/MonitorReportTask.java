package net.thearchon.hq.task.tasks;

import net.thearchon.hq.*;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.BungeeClient;
import net.thearchon.hq.client.MonitorableClient;
import net.thearchon.hq.language.Message;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonitorReportTask implements Runnable {

    private final Archon archon;

    public MonitorReportTask(Archon archon) {
        this.archon = archon;
    }

    @Override
    public void run() {
        List<ReportRecord> records = new ArrayList<>();
        for (MonitorableClient client : archon.getClients(MonitorableClient.class)) {
            if (client.getType().isMinigameType()) return;

            ReportRecord record = null;
            int usagePerc = client.getMemoryUsagePercMax();
            if (usagePerc >= 85) {
                String serverName;
                if (client instanceof BukkitClient) {
                    serverName = ((BukkitClient) client).getServerName();
                } else if (client instanceof BungeeClient) {
                    serverName = "bungee" + ((BungeeClient) client).getId();
                } else {
                    serverName = client.getClass().getSimpleName() + client.getChannel().getId();
                }

                record = new ReportRecord(serverName);
                record.usedMemory = client.getReadableUsedMemory();
                record.maxMemory = client.getReadableMaxMemory();
                record.memoryUsage = usagePerc;
            }

            if (client instanceof BukkitClient) {
                BukkitClient bc = (BukkitClient) client;
                double tps = bc.getTps();
                if (tps >= 0 && tps <= 17) {
                    if (record == null) {
                        record = new ReportRecord(bc.getServerName());
                    }
                    record.tps = tps;
                }
            }

            if (record != null) {
                records.add(record);
            }
        }

        Collections.sort(records, (o1, o2) -> o1.serverName.compareTo(o2.serverName));

        if (!records.isEmpty()) {
            List<String> reportMsg = new ArrayList<>(records.size() + 4);
            reportMsg.add("&c" + Message.BAR);
            reportMsg.add("&7[&c&lArchonHQ&7] &6Monitor Report");
            reportMsg.add("");
            int i = 0;
            for (ReportRecord record : records) {
                StringBuilder buf = new StringBuilder();
                buf.append("&6#").append(++i).append(": &3");
                buf.append(record.serverName);
                buf.append(": ");

                if (record.tps != null) {
                    buf.append("&7Tps &c");
                    buf.append(decFormat.format(record.tps));
                    if (record.memoryUsage != null) {
                        buf.append("&7, ");
                    }
                }
                if (record.memoryUsage != null) {
                    buf.append("&7Memory &c");
                    buf.append(record.usedMemory.replace(" ", "").toLowerCase());
                    buf.append("&8/&c");
                    buf.append(record.maxMemory.replace(" ", "").toLowerCase());
                    buf.append(" &7&o(");
                    buf.append(record.memoryUsage);
                    buf.append("%)");
                }
                reportMsg.add(buf.toString());
            }
            reportMsg.add("&c" + Message.BAR);

            String[] msg = reportMsg.toArray(new String[reportMsg.size()]);
            for (Player player : archon.getPlayers()) {
                if (player.getName().equalsIgnoreCase("TheCampingRusher")) continue;
                if (player.hasPermission(Rank.ADMIN) && !player.hasStaffSilent()) {
                    player.message(msg);
                }
            }
        }
    }

    private static final DecimalFormat decFormat = new DecimalFormat(".##");

    private static final class ReportRecord {
        final String serverName;
        Double tps;
        String usedMemory;
        String maxMemory;
        Integer memoryUsage;

        ReportRecord(String serverName) {
            this.serverName = serverName;
        }
    }
}
