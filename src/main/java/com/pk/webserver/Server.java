package com.pk.webserver;

import com.pk.dao.IndexCalculator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Server {

    public final static AtomicInteger connections = new AtomicInteger();
    public final static AtomicBoolean anyPostCalled = new AtomicBoolean(false);

    public final static AtomicInteger oldPhase = new AtomicInteger(0);
    public final static AtomicInteger phase = new AtomicInteger(0);
    public final static AtomicLong lastQueryTime = new AtomicLong(0);

    public Server() {
    }

    public void start() throws Exception {
        final int port = 80;

        final Thread phaseChangeThread = new Thread(this::phaseChangeMonitor);
        phaseChangeThread.start();


        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .childOption(ChannelOption.SO_KEEPALIVE, true);;

            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void phaseChangeMonitor() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long curTime = Calendar.getInstance().getTimeInMillis();

            if (curTime - lastQueryTime.get() > 200) {
                if (phase.get() == 0)
                    continue;

                if(phase.get() == 1 && oldPhase.get() == 0) {
                    oldPhase.set(1);
                    System.gc();
                    System.out.println("FIRST PHASE END");
                }

                if(phase.get() == 2 && oldPhase.get() == 1 && anyPostCalled.get()) {
                    oldPhase.set(2);
                    IndexCalculator id = new IndexCalculator();
                    id.calculateIndexes();
                    id.clearTempData();

                    System.gc();
                    System.out.println("SECOND PHASE END");
                }
            } else {
                if(phase.get() == oldPhase.get())
                    phase.set(phase.get() + 1);
            }
        }
    }

    private class ServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch){

            ch.pipeline().addLast("codec", new HttpServerCodec());
            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512*1024));
            ch.pipeline().addLast("request",new ServerHandler());
        }
    }

    class ServerHandler extends ChannelInboundHandlerAdapter {

        private HttpRequest request;
        private final StringBuilder buf = new StringBuilder();
        private final Workers workers = new Workers();
        private HttpResponseStatus status = NOT_FOUND;

        public ServerHandler() {
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {

                lastQueryTime.set(Calendar.getInstance().getTimeInMillis());

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
                    anyPostCalled.set(true);
                    if (context.startsWith("/accounts/new/")) {
                        if(context.equals("/accounts/new/"))
                            status = workers.newAccount(request, buf);
                        else
                            status = NOT_FOUND;
                    } else {
                        if (context.startsWith("/accounts/likes/")) {
                            if(context.equals("/accounts/likes/"))
                                status = workers.likes(request, buf);
                            else
                                status = NOT_FOUND;
                        } else {
                            status = workers.refresh(request, buf);
                        }
                    }
                }
            }

            try{
                ((FullHttpRequest) request).content().release();
            } catch (Exception ex) {}

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