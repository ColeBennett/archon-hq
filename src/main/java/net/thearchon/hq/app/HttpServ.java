package net.thearchon.hq.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import net.thearchon.hq.Archon;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class HttpServ {

    private ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    public HttpServ() {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }

    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                .group(masterGroup, slaveGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new ChannelHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                if (msg instanceof HttpRequest) {
                                    FullHttpRequest req = (FullHttpRequest) msg;

                                    FullHttpResponse resp = new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.OK,
                                            Unpooled.wrappedBuffer("OK".getBytes()));

                                    if (is100ContinueExpected(req)) {
                                        ctx.write(new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.CONTINUE));
                                    }
                                    boolean keepAlive = isKeepAlive(req);

                                    resp.headers().set(CONTENT_TYPE, "text/plain");
                                    resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());

                                    if (!keepAlive) {
                                        ctx.write(resp).addListener(ChannelFutureListener.CLOSE);
                                    } else {
                                        resp.headers().set(CONNECTION, Values.KEEP_ALIVE);
                                        ctx.write(resp);
                                    }

                                    String sig = req.headers().get("X-BC-Sig");
//                                    hash("sha256", $webhook_secret . $json['payment']['txn_id'] . $json['payment']['status'] . $json['customer']['email']);

                                    String json = req.content().toString(CharsetUtil.UTF_8);
                                    Archon.getInstance().getDataSource().execute("INSERT INTO data VALUES(NOW(),?,?);", json, sig);
//                                }
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                ctx.flush();
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                ctx.writeAndFlush(new DefaultFullHttpResponse(
                                        HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                        copiedBuffer(cause.getMessage().getBytes())));
                            }
                        });
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024);
            channel = bootstrap.bind(7070).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();
        try {
            channel.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
