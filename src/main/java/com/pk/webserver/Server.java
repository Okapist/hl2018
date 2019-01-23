package com.pk.webserver;

import com.pk.dao.IndexCalculator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
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

/*

    NioEventLoopGroup → EpollEventLoopGroup
    NioEventLoop → EpollEventLoop
    NioServerSocketChannel → EpollServerSocketChannel
    NioSocketChannel → EpollSocketChannel
    
*/
    
    //public final static AtomicInteger connections = new AtomicInteger();
    public final static AtomicBoolean anyPostCalled = new AtomicBoolean(false);

    public static volatile int oldPhase = 0;
    public static volatile int phase = 0;
    public static volatile long lastQueryTime = 0;
    public static volatile long phase2begin = 0;

    public Server() {
    }

    public void start() throws Exception {
        final int port = 80;

        final Thread phaseChangeThread = new Thread(this::phaseChangeMonitor);
        phaseChangeThread.start();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

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

            if (curTime - lastQueryTime > 200 && curTime-phase2begin > 296000) {

                if (phase == 0)
                    continue;

                if(phase == 1 && oldPhase == 0) {
                    oldPhase = 1;
                    System.gc();
                    System.out.println("FIRST PHASE END " + curTime);
                }

                if(phase == 2 && oldPhase == 1 && anyPostCalled.get()) {
                    oldPhase = 2;
                    System.gc();
                    IndexCalculator id = new IndexCalculator();
                    id.calculateIndexes();
                    id.clearTempData();

                    System.gc();
                    System.out.println("SECOND PHASE END " + curTime);
                    return;
                }
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
        public void channelRead(ChannelHandlerContext ctx, Object msg) {

            long curTime = Calendar.getInstance().getTimeInMillis();
            lastQueryTime = curTime;

            HttpRequest request = this.request = (HttpRequest) msg;
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            buf.setLength(0);
            String context = queryStringDecoder.path();
            try {
                if (request.method() == HttpMethod.GET) {

                    if (phase == 0) {
                        phase = 1;
                        System.out.println("FIRST PHASE START " + curTime);
                    }
                    if (phase == 2) {
                        phase = 3;
                        System.out.println("THIRD PHASE START " + curTime);
                    }
/*

                    if (anyPostCalled.get()) { //PHASE 3 IGNORING
                        buf.append("{\"accounts\": []}");
                        status = OK;
                    } else {
*/

                        if (context.startsWith("/accounts/filter/")) {
                            if (!context.equals("/accounts/filter/"))
                                status = NOT_FOUND;
                            else
                                status = workers.filter(request, buf);
                        } else {
                            if (context.startsWith("/accounts/group/")) {
                                if (!context.equals("/accounts/group/"))
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
                    //}
                } else {
                    try {
                        if (phase == 1) {
                            phase = 2;
                            System.out.println("SECOND PHASE START " + curTime);
                        }
                        if (oldPhase == 2) {
                            System.out.print("bad ");
                        }

                        if(phase2begin == 0)
                            phase2begin = Calendar.getInstance().getTimeInMillis();
                        anyPostCalled.set(true);


                        if (context.startsWith("/accounts/new/")) {
                            if (context.equals("/accounts/new/"))
                                status = workers.newAccount(request);
                            else
                                status = NOT_FOUND;
                        } else {
                            if (context.startsWith("/accounts/likes/")) {
                                if (context.equals("/accounts/likes/"))
                                    status = workers.likes(request);
                                else
                                    status = NOT_FOUND;
                            } else {
                                status = workers.refresh(request, context);
                            }
                        }
                    } catch (Exception ex){
                        status = BAD_REQUEST;
                    }
                    finally {
                        buf.append("{}");
                    }
                }
            } finally {
                try {
                    ((FullHttpRequest) request).content().release();
                } catch (Exception ex) {
                }

            }

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1,
                    status,
                    Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8)
            );
            response.headers().set(SERVER, "PK");
            response.headers().set(CONTENT_TYPE, "application/json");
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

}