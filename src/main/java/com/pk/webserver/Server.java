package com.pk.webserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Server {

    public final static AtomicInteger connections = new AtomicInteger();
    public final static AtomicBoolean gcCalled = new AtomicBoolean(false);

    public Server() {
    }

    public void start() throws Exception {
        final int port = 80;

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer());
            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch){
            ChannelPipeline p = ch.pipeline();
            p.addLast("decoder", new HttpRequestDecoder());
            p.addLast("encoder", new HttpResponseEncoder());
            p.addLast("handler", new ServerHandler());
        }
    }

    class ServerHandler extends SimpleChannelInboundHandler<Object> {

        private HttpRequest request;
        private final StringBuilder buf = new StringBuilder();
        private final Workers workers = new Workers();
        private HttpResponseStatus status = NOT_FOUND;

        public ServerHandler() {
        }

/*
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            int val = Server.connections.incrementAndGet();
            if (val <= 4*10) {
                super.channelActive(ctx);
            } else {
                System.out.print("refused");
                ctx.close();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Server.connections.decrementAndGet();
        }
*/


        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
                buf.setLength(0);
                String context = queryStringDecoder.path();
                if (request.method() == HttpMethod.GET) {
                    if (context.startsWith("/accounts/filter/")) {
                        if(!context.equals("/accounts/filter/"))
                            status = NOT_FOUND;
                        else
                            status = workers.filter(request, buf);
                    } else {
                        if (context.startsWith("/accounts/group/")) {
                            if(!context.equals("/accounts/group/"))
                                status = NOT_FOUND;
                            else
                                status = workers.group(request, buf);
                        } else {
                            if (context.endsWith("/recommend/")) {
                                status = workers.recommend(request, buf);
                            } else {
                                if (context.endsWith("/suggest/")) {
                                    status = workers.suggest(request, buf);
                                } else {
                                    status = NOT_FOUND;
                                }
                            }
                        }
                    }
                } else {
                    if(!gcCalled.get()) {
                        gcCalled.set(true);
                        System.gc();
                    }

                    if (context.startsWith("/accounts/new/")) {
                        status = workers.newAccount(request, buf);
                    } else {
                        if (context.startsWith("/accounts/likes/")) {
                            status = workers.newAccount(request, buf);
                        } else {
                            status = workers.refresh(request, buf);
                        }
                    }
                }
            }

            if (msg instanceof LastHttpContent) {
                FullHttpResponse response = new DefaultFullHttpResponse (
                        HTTP_1_1,
                        status,
                        Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8)
                );
                //response.c
                response.headers().set(SERVER,"PK");
                response.headers().set(CONTENT_TYPE,"application/json");
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                ctx.writeAndFlush(response);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}