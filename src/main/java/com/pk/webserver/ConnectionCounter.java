package com.pk.webserver;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class ConnectionCounter extends ChannelInboundHandlerAdapter {

    private static int connections = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(connections < 4) {
            connections++;
            super.channelActive(ctx);
        } else
            ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        connections--;
    }
}