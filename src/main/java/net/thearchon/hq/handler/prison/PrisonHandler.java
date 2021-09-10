package net.thearchon.hq.handler.prison;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.Client;
import net.thearchon.hq.handler.NamedBukkitHandler;
import net.thearchon.nio.BufferedPacket;
import net.thearchon.nio.Protocol;
import net.thearchon.nio.protocol.impl.RequestPacket;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

public class PrisonHandler extends NamedBukkitHandler<PrisonClient> implements Runnable {

    private ScheduledFuture<?> task;
    private boolean restarting;
    private int counter;
    private PrisonClient current;
    private Iterator<PrisonClient> itr;

    public PrisonHandler(Archon archon) {
        super(archon);

//        archon.runTaskTimer(new Runnable() {
//            @Override
//            public void run() {
//                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
//                if (cal.get(Calendar.HOUR_OF_DAY) == 0 && !restarting) {
//                    restarting = true;
//                    itr = getClients().iterator();
//                    task = archon.runTaskTimer(PrisonHandler.this, 1, TimeUnit.SECONDS);
//                }
//            }
//        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (current == null) {
            if (!itr.hasNext()) {
                restarting = false;
                task.cancel(true);
                task = null;
                return;
            }
            current = itr.next();
            counter = 60 * 5;
        }
        if (counter >= 60 && (counter % 60) == 0) {
            int minute = counter / 60;
            broadcast(current, "&4This server &7&o(" + current.getServerName() + ") &4will restart in &c&l" + minute + " &4minute" + (minute == 1 ? "" : "s") + "!");
        } else if (counter > 0 && counter < 60) {
            if (counter == 30 || counter == 15 || counter == 10 || counter <= 5) {
                broadcast(current, "&4This server &7&o(" + current.getServerName() + ") &4will restart in &c&l" + counter + " &4second" + (counter == 1 ? "" : "s") + "!");
            }
        } else if (counter == 0) {
            current.setLocked(true);
            current.setLockMessage("&cThis server is currently restarting. Please try again in a few seconds.");
            for (Player player : current.getPlayers()) {
                player.connect("lobby1");
            }
        } else if (counter == -10) {
            server.send(current, Protocol.SHUTDOWN.construct());
        } else if (counter == -15) {
            current.setLocked(false);
            current.setLockMessage(null);
            current = null;
        }
        counter--;
    }

    private void broadcast(PrisonClient client, String message) {
        message = "&c&l[WARNING] " + message;
        for (Player player : client.getPlayers()) {
            player.message(message);
        }
    }

    @Override
    public void requestReceived(Client client, RequestPacket request,
            Protocol header, BufferedPacket buf) {
    }

    @Override
    protected void handleInbound(PrisonClient client, Protocol header,
            BufferedPacket buf) {
    }

    @Override
    protected void handleConnect(PrisonClient client) {

    }

    @Override
    protected void handleDisconnect(PrisonClient client) {

    }
}
