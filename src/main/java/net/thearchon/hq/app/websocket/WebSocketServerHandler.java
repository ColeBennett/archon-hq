package net.thearchon.hq.app.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import net.thearchon.hq.Archon;
import net.thearchon.hq.util.Util;
import net.thearchon.hq.util.io.JsonUtil;

import java.util.Map;
import java.util.logging.Level;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private final WebSocketServer server;
    private WebSocketServerHandshaker handshaker;

    public WebSocketServerHandler(WebSocketServer server) {
        this.server = server;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
        System.out.println("\nrecv " + msg.getClass().getSimpleName() + " from " + ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if ("/".equals(req.getUri())) {
            ByteBuf content = getContent(getWebSocketLocation(req));
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            setContentLength(res, content.readableBytes());
            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        Channel channel = ctx.channel();

        QueryStringDecoder dec = new QueryStringDecoder(req.getUri());
        String username = dec.parameters().get("username").get(0);
        String page = dec.parameters().get("page").get(0);
        channel.attr(WebSocketServer.USERNAME).set(username);
        Archon.getInstance().getLogger().warning(Thread.currentThread().getName() + " [WS] channel username for " + channel + ": " + username + " - page=" + page);

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req).addListener((ChannelFutureListener) future -> {
                Channel channel1 = future.channel();
                if (future.isSuccess()) {
                    channel1.attr(WebSocketServer.REGISTERED).set(true);
                    server.addChannel(channel1);
                } else {
                    future.channel().attr(WebSocketServer.REGISTERED).set(false);
                    Archon.getInstance().getLogger().log(Level.WARNING, Thread.currentThread().getName() + " [WS] Failed to register channel (handshake): " + channel1, future.cause());
                }
            });
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        System.out.println("recv frame: " + frame);

        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            System.out.println("Close: " + ((CloseWebSocketFrame) frame).reasonText());
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();
        if (request.startsWith("username:")) {
            ctx.channel().attr(WebSocketServer.USERNAME).set(request.replace("username:", ""));
        } else {
            Map<String, Object> payload = JsonUtil.load(request, Util.MAP_TYPE);
            server.handleInbound(ctx.channel(), payload);
        }
        Archon.getInstance().getLogger().log(Level.WARNING, Thread.currentThread().getName() + " [WS] " + String.format("%s from %s", request, ctx.channel()));
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }

    private static final String NEWLINE = "\r\n";

    public static ByteBuf getContent(String webSocketLocation) {
        return Unpooled.copiedBuffer(
                "<html><head><title>Web Socket Test</title></head>" + NEWLINE +
                        "<body>" + NEWLINE +
                        "<script type=\"text/javascript\">" + NEWLINE +
                        "var socket;" + NEWLINE +
                        "if (!window.WebSocket) {" + NEWLINE +
                        "  window.WebSocket = window.MozWebSocket;" + NEWLINE +
                        '}' + NEWLINE +
                        "if (window.WebSocket) {" + NEWLINE +
                        "  socket = new WebSocket(\"" + webSocketLocation + "?username=test\");" + NEWLINE +
                        "  socket.onmessage = function(event) {" + NEWLINE +
                        "    console.log(JSON.parse(event.data));" + NEWLINE +
                        "    var ta = document.getElementById('responseText');" + NEWLINE +
                        "    ta.value = ta.value + '\\n' + event.data" + NEWLINE +
                        "  };" + NEWLINE +
                        "  socket.onopen = function(event) {" + NEWLINE +
                        "    var ta = document.getElementById('responseText');" + NEWLINE +
                        "    ta.value = \"Web Socket opened!\";" + NEWLINE +
                        "  };" + NEWLINE +
                        "  socket.onclose = function(event) {" + NEWLINE +
                        "    var ta = document.getElementById('responseText');" + NEWLINE +
                        "    ta.value = ta.value + \"Web Socket closed\"; " + NEWLINE +
                        "  };" + NEWLINE +
                        "} else {" + NEWLINE +
                        "  alert(\"Your browser does not support Web Socket.\");" + NEWLINE +
                        '}' + NEWLINE +
                        NEWLINE +
                        "function send(message) {" + NEWLINE +
                        "  if (!window.WebSocket) { return; }" + NEWLINE +
                        "  if (socket.readyState == WebSocket.OPEN) {" + NEWLINE +
                        "    socket.send(message);" + NEWLINE +
                        "  } else {" + NEWLINE +
                        "    alert(\"The socket is not open.\");" + NEWLINE +
                        "  }" + NEWLINE +
                        '}' + NEWLINE +
                        "</script>" + NEWLINE +
                        "<form onsubmit=\"return false;\">" + NEWLINE +
                        "<input type=\"text\" name=\"message\" value=\"{}\"/>" +
                        "<input type=\"button\" value=\"Send Web Socket Data\"" + NEWLINE +
                        "       onclick=\"send(this.form.message.value)\" />" + NEWLINE +
                        "<h3>Output</h3>" + NEWLINE +
                        "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE +
                        "</form>" + NEWLINE +
                        "</body>" + NEWLINE +
                        "</html>" + NEWLINE, CharsetUtil.US_ASCII);
    }
}
