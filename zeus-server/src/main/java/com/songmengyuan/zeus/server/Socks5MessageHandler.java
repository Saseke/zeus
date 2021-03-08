package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.util.ShadowsocksUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Socks5MessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(Socks5MessageHandler.class);

    private Channel clientChannel;

    private Channel remoteChannel;

    private Bootstrap bootstrap;

    private List<ByteBuf> clientBuf = new ArrayList<>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (clientChannel == null) {
            this.clientChannel = ctx.channel();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            InetSocketAddress remoteAddress = ctx.channel().attr(Socks5ServerConstant.DST_ADDRESS).get();
            bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

                                    clientChannel.writeAndFlush(msg.retain());
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    logger.error("channel id:[{}] ,cause {}", ctx.channel().id(), cause.getMessage());
                                    closeClientChannel();
                                    closeRemoteChannel();
                                }
                            });
                        }
                    });
            bootstrap.connect(remoteAddress).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    remoteChannel = future.channel();
                    logger.info("host: [{}:{}] connect success, client channelId is [{}],  remote channelId is [{}]",
                            remoteAddress.getHostName(), remoteAddress.getPort(), clientChannel.id(),
                            remoteChannel.id());
                    clientBuf.add(msg.retain());
                    writeAndFlushMessage();
                } else {
                    logger.error(remoteAddress.getHostName() + ":" + remoteAddress.getPort() + " connection fail");
                    closeClientChannel();
                }
            });
        }
        clientBuf.add(msg.retain());
        writeAndFlushMessage();
    }

    private void closeRemoteChannel() {
        remoteChannel.close();
        logger.info("remote channel [{}] is closed", remoteChannel.id());
    }

    private void closeClientChannel() {
        clientChannel.close();
        logger.info("client channel [{}] is closed", clientChannel.id());
    }

    private void writeAndFlushMessage() {
        if (remoteChannel != null && !clientBuf.isEmpty()) {
            ByteBuf messageBuf = PooledByteBufAllocator.DEFAULT.heapBuffer();
            clientBuf.forEach(byteBuf -> {
                messageBuf.writeBytes(ShadowsocksUtils.readRealBytes(byteBuf));
                ReferenceCountUtil.release(byteBuf);
            });
            remoteChannel.writeAndFlush(messageBuf.retain());
            clientBuf.clear();
        }
    }

}
