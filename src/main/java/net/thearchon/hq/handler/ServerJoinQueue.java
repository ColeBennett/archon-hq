package net.thearchon.hq.handler;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.event.listeners.PlayerDisconnectListener;
import net.thearchon.hq.language.Message;
import net.thearchon.hq.util.Util;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class ServerJoinQueue {

    private final BukkitClient client;
    private final TreeSet<QueueEntry> queue;
    private final List<Long> waitTimes;
    private long averageWaitTime;
    private long lastPoll;

    public ServerJoinQueue(BukkitClient client) {
        this.client = client;

        /**
         * Compares ranks and then entry time for queue ranking.
         */
        queue = new TreeSet<>((e1, e2) -> {
            int p1Level = e1.getPlayer().getRank().ordinal();
            int p2Level = e2.getPlayer().getRank().ordinal();
            if (p1Level > p2Level) {
                return -1;
            } else if (p1Level < p2Level) {
                return 1;
            } else {
                return Long.compare(e1.getCreated(), e2.getCreated());
            }
        });
        waitTimes = new ArrayList<>(1000);

        Archon.getInstance().getEventManager().register((PlayerDisconnectListener) player -> {
            Iterator<QueueEntry> itr = queue.iterator();
            while (itr.hasNext()) {
                if (itr.next().getPlayer().equals(player)) {
                    itr.remove();
                    updateQueuePositions();
                    updateQueueSize();
                }
            }
        });

        Archon.getInstance().runTaskTimer(() ->  {
            if (client.getOnlineCount() < client.getSlots()) {
                poll();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public boolean queue(Player player) {
        for (QueueEntry entry : queue) {
            if (entry.getPlayer().equals(player)) {
                player.error(Message.QUEUE_PREFIX + "&cYou are already in the queue for &7" + client.getDisplayName() + "&c!");
                return false;
            }
        }
        queue.add(new QueueEntry(player));
        updateQueuePositions();
        updateQueueSize();

        player.message(Message.QUEUE_PREFIX + "&aJoined the queue for &e" + client.getDisplayName() + "&a!");
        return true;
    }

    public void poll() {
        QueueEntry entry = queue.pollFirst();
        if (entry != null) {
            lastPoll = System.currentTimeMillis();
            addWaitTime(entry);

            Player player = entry.getPlayer();
            player.connect(client);

            updateQueuePositions();
            updateQueueSize();
        }
    }

    public long getAverageWaitTime() {
        return averageWaitTime;
    }

    private void updateQueuePositions() {
        int pos = 1, size = queue.size();
        for (QueueEntry entry : queue) {
            if (entry.getPosition() != pos) {
                Player player = entry.getPlayer();
                if (player.getCurrentServer() != null && player.getCurrentServer().getType().isLobbyType()) {
                    if (entry.getEstimatedWaitTime() == 0) {
                        entry.setEstimatedWaitTime(pos * averageWaitTime);
                    }
                    player.getCurrentServer().send(Protocol.QUEUE_POSITION_UPDATE.construct(client.getServerName(), player.getId(), pos, entry.getEstimatedWaitTime()));
                }
                player.message(Message.QUEUE_PREFIX + "&aYou are now &e#" + Util.addCommas(pos) + " &aout of &e" + Util.addCommas(size)
                        + " &ain the " + client.getDisplayName() + " queue!");
                entry.setPosition(pos);
            }
            pos++;
        }

//        StringBuilder buf = new StringBuilder();
//        buf.append("&3").append(client.getDisplayName()).append(" Queue &d(").append(queue.size()).append("): ");
//        for (QueueEntry entry : queue) {
//            buf.append("&7").append(entry.getPlayer().getName());
//            if (entry.getPlayer().getRank() != Rank.DEFAULT) {
//                buf.append("&7/").append(entry.getPlayer().getRank().getDisplay());
//            }
//            buf.append("&8, ");
//        }
//        for (QueueEntry entry : queue) {
//            if (entry.getPlayer().hasPermission(Rank.MANAGER)) {
//                entry.getPlayer().message(buf.toString());
//            }
//        }
    }

    public void updateQueueSize(BukkitClient client) {
        client.send(Protocol.QUEUE_SIZE_UPDATE.construct(client.getServerName(), queue.size(), averageWaitTime));
    }

    private void updateQueueSize() {
        BufferedPacket out = Protocol.QUEUE_SIZE_UPDATE.construct(client.getServerName(), queue.size(), averageWaitTime);
        for (Client lobby : Archon.getInstance().getClients(ServerType.LOBBY)) {
            lobby.send(out);
        }
    }

    private void addWaitTime(QueueEntry entry) {
        waitTimes.add(entry.getCreated() - System.currentTimeMillis());
        if (waitTimes.size() > 1000) {
            waitTimes.remove(0);
        }
        long total = 0;
        for (Long time : waitTimes) {
            total += time;
        }
        averageWaitTime = total / waitTimes.size();

        int pos = 1;
        for (QueueEntry e : queue) {
            long newEstWaitTime = averageWaitTime * pos++;
            if (newEstWaitTime != e.getEstimatedWaitTime()) {
                e.setEstimatedWaitTime(newEstWaitTime);
                Player player = e.getPlayer();
                if (player.getCurrentServer() != null && player.getCurrentServer().getType().isLobbyType()) {
                    player.getCurrentServer().send(Protocol.QUEUE_POSITION_UPDATE.construct(client.getServerName(), player.getId(), pos, newEstWaitTime));
                }
            }
        }
    }
}
