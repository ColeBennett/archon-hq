package net.thearchon.hq.app.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import net.thearchon.hq.Archon;
import net.thearchon.hq.util.io.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public class WebSocketServer {

    public static final AttributeKey<Boolean> REGISTERED = AttributeKey.valueOf("registered");
    public static final AttributeKey<String> USERNAME = AttributeKey.valueOf("username");

    private final Archon archon;
    private ChannelGroup channels;
    private final int port = 8950;

    private ChannelConnectHandler connectHandler;
    private ChannelDisconnectHandler disconnectHandler;

    private final List<Integer> onlineCounts = new ArrayList<>();

    public WebSocketServer(Archon archon) {
        this.archon = archon;

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        channels = new DefaultChannelGroup(archon.getExecutor());

                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(archon.getExecutor(), new WebSocketServerHandler(WebSocketServer.this));
                        pipeline.addLast(archon.getExecutor(), new ChannelHandlerAdapter() {
                            @Override
                            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                                Channel channel = ctx.channel();
                                Archon.getInstance().getLogger().warning(Thread.currentThread().getName() + " [WS] channel connected, awaiting handshake: " + channel + " - " + channels.size());
                            }

                            @Override
                            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                                Channel channel = ctx.channel();
                                channels.remove(channel);
                                Archon.getInstance().getLogger().warning(Thread.currentThread().getName() + " [WS] channel disconnected: " + channel + "/" + channel.attr(USERNAME) + " - " + channels.size());
                                if (disconnectHandler != null) {
                                    Boolean reg = channel.attr(REGISTERED).get();
                                    if (reg != null && reg) {
                                        disconnectHandler.channelDisconnected(channel);
                                    } else {
                                        Archon.getInstance().getLogger().warning(Thread.currentThread().getName() + " [WS] channel disconnected without being registered: " + channel + "/" + channel.attr(USERNAME));
                                    }
                                }
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {

                            }
                        });
                    }
                });
        b.bind(port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Archon.getInstance().getLogger().warning("[WS] WebSocket server running: " + future.channel().localAddress());
            } else {
                Archon.getInstance().getLogger().log(Level.SEVERE, "[WS] WebSocket server failed to bind to port: " + port, future.cause());
            }
        });
    }

    public Set<Channel> getChannels() {
        return channels;
    }

    void addChannel(Channel channel) {
        channels.add(channel);
        Archon.getInstance().getLogger().warning(Thread.currentThread().getName() + " [WS] successfully registered: " + channel + " - username=" + channel.attr(USERNAME) + " - " + channels.size());
        if (connectHandler != null) {
            connectHandler.channelConnected(channel);
        }
    }

    void handleInbound(Channel channel, Map<String, Object> payload) {
        for (Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case "set_motd":
                    String motd = (String) entry.getValue();
                    archon.getSettings().setMotd(motd);
                    archon.reloadSettings();
                    sendAll("motd", motd);
                    break;
                case "set_maintenance_motd":
                    String maintenanceMotd = (String) entry.getValue();
                    archon.getSettings().setMaintenanceMotd(maintenanceMotd);
                    archon.reloadSettings();
                    sendAll("maintenance_motd", maintenanceMotd);
                    break;
                case "set_slots":
                    int slots = (Integer) entry.getValue();
                    archon.getSettings().setSlots(slots);
                    archon.reloadSettings();
                    break;
                case "set_lobby_slots":
                    int lobbySlots = (Integer) entry.getValue();
                    archon.getSettings().setLobbySlots(lobbySlots);
                    archon.reloadSettings();
                    sendAll("lobby_slots", lobbySlots);
                    break;
                case "set_maintenance_mode":
                    boolean maintenanceMode = (Boolean) entry.getValue();
                    archon.getSettings().setMaintenanceMode(true);
                    sendAll("maintenance_mode", maintenanceMode);
                    // TODO
                    break;
            }
        }
    }

    public interface ChannelConnectHandler {
        void channelConnected(Channel channel);
    }
    public interface ChannelDisconnectHandler {
        void channelDisconnected(Channel channel);
    }

    public void setOnConnect(ChannelConnectHandler connectHandler) {
        this.connectHandler = connectHandler;
    }

    public void setOnDisconnect(ChannelDisconnectHandler disconnectHandler) {
        this.disconnectHandler = disconnectHandler;
    }

    public void send(Channel channel, WebSocketPacket packet) {
        send(channel, packet.getFrame());
    }

    public void send(Channel channel, Object msg) {
        channel.writeAndFlush(construct(msg));
    }

    public void send(Channel channel, String text) {
        channel.writeAndFlush(construct(text));
    }

    public void send(Channel channel, String key, Object value) {
        channel.writeAndFlush(construct(key, value));
    }

    public void send(Channel channel, TextWebSocketFrame frame) {
        channel.writeAndFlush(frame);
    }

    public void sendAll(TextWebSocketFrame frame) {
        if (channels != null) {
            Archon.getInstance().getLogger().warning("[WS] sendAll(): " + (frame == null ? "NONE" : frame.getClass().getSimpleName()));
            channels.writeAndFlush(frame);
        }
    }

    public void sendAll(String key, Object value) {
        sendAll(construct(key, value));
    }

    public void sendAll(Object msg) {
        sendAll(construct(msg));
    }

    public void sendAll(WebSocketPacket packet) {
        sendAll(packet.getFrame());
    }

    public void sendAll(String text) {
        sendAll(construct(text));
    }

    public static TextWebSocketFrame construct(String key, Object value) {
        if (value instanceof String) {
            value = "\"" + value + "\"";
        }
        return construct("{\"" + key + "\":" + value + '}');
    }

    public static TextWebSocketFrame constructJson(String key, String json) {
        return construct("{\"" + key + "\":" + json + '}');
    }

    public static TextWebSocketFrame construct(Object msg) {
        return construct(JsonUtil.toJsonCompact(msg));
    }

    public static TextWebSocketFrame construct(String text) {
        return new TextWebSocketFrame(text);
    }
}
