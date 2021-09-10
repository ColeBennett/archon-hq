package net.thearchon.hq.event;

import net.thearchon.hq.Archon;
import net.thearchon.hq.Player;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.event.listeners.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EventManager {

    private final Archon archon;
    private final List<Listener> listeners;

    public EventManager(Archon archon) {
        this.archon = archon;
        listeners = new ArrayList<>();
    }

    public void register(Listener listener) {
        listeners.add(listener);
    }

    public void unregister(Listener listener) {
        listeners.remove(listener);
    }

    public void firePlayerConnected(Player player) {
        for (Listener listener : listeners) {
            if (listener instanceof PlayerConnectListener) {
                try {
                    ((PlayerConnectListener) listener).playerConnected(player);
                } catch (Throwable t) {
                    archon.getLogger().log(Level.WARNING, "Failed to handle PlayerConnected listener", t);
                }
            }
        }
    }

    public void firePlayerDisconnected(Player player) {
        for (Listener listener : listeners) {
            if (listener instanceof PlayerDisconnectListener) {
                try {
                    ((PlayerDisconnectListener) listener).playerDisconnected(player);
                } catch (Throwable t) {
                    archon.getLogger().log(Level.WARNING, "Failed to handle PlayerDisconnected listener", t);
                }
            }
        }
    }

    public void firePlayerServerConnect(Player player, BukkitClient server) {
        for (Listener listener : listeners) {
            if (listener instanceof PlayerServerConnectListener) {
                try {
                    ((PlayerServerConnectListener) listener).connectedTo(player, server);
                } catch (Throwable t) {
                    archon.getLogger().log(Level.WARNING, "Failed to handle PlayerServerConnected listener", t);
                }
            }
        }
    }

    public void firePlayerChat(Player player, String message) {
        for (Listener listener : listeners) {
            if (listener instanceof PlayerChatListener) {
                try {
                    ((PlayerChatListener) listener).playerChat(player, message);
                } catch (Throwable t) {
                    archon.getLogger().log(Level.WARNING, "Failed to handle PlayerChat listener", t);
                }
            }
        }
    }

    public void firePlayerCommand(Player player, String command, String[] args) {
        for (Listener listener : listeners) {
            if (listener instanceof PlayerCommandListener) {
                try {
                    ((PlayerCommandListener) listener).playerCommand(player, command, args);
                } catch (Throwable t) {
                    archon.getLogger().log(Level.WARNING, "Failed to handle PlayerCommand listener", t);
                }
            }
        }
    }
}
