package net.thearchon.hq.app.websocket;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.thearchon.hq.util.io.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebSocketPacket {

    private final Map<String, Object> data;

    public WebSocketPacket() {
        this(new LinkedHashMap<>());
    }

    public WebSocketPacket(Map<String, Object> data) {
        this.data = data;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public WebSocketPacket set(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    public int size() {
        return data.size();
    }

    public String toJson() {
        return JsonUtil.toJsonCompact(data);
    }

    public TextWebSocketFrame getFrame() {
        return new TextWebSocketFrame(toJson());
    }

    @Override
    public String toString() {
        return toJson();
    }
}
