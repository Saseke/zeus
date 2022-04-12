package com.songmengyuan.zeus.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;
import com.songmengyuan.zeus.common.config.util.ShadowsocksUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Socks5MessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(Socks5MessageHandler.class);

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
        InetSocketAddress remoteAddress = ctx.channel().attr(Socks5ServerConstant.DST_ADDRESS).get();
        InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String token = clientChannel.attr(Socks5ServerConstant.USER_TOKEN).get();
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                clientChannel.writeAndFlush(msg.retain());
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                String message = String.format("[%s] channel id:[%s] ,cause %s",
                                    Thread.currentThread().getName(), ctx.channel().id(), cause.getMessage());
                                ZeusLog zeusLog = ZeusLog.createErrorLog(message, new Date());
                                logger.info(GsonUtil.getGson().toJson(zeusLog));
                                // logger.error("channel id:[{}] ,cause {}", ctx.channel().id(), cause.getMessage());
                                closeClientChannel();
                                closeRemoteChannel();
                            }
                        });
                    }
                });
            bootstrap.connect(remoteAddress).addListener((ChannelFutureListener)future -> {
                // String tmpToken = clientChannel.attr(Socks5ServerConstant.USER_TOKEN).get();
                if (future.isSuccess()) {
                    remoteChannel = future.channel();
                    // logger.info("server : {} connect success, client channelId is {}",
                    // remoteAddress.getHostName() + ":" + remoteAddress.getPort(), clientChannel.id());
                    String logMessage = String.format("server : %s connect success, client channelId is %s",
                        remoteAddress.getHostName() + ":" + remoteAddress.getPort(), clientChannel.id());
                    ZeusLog log = ZeusLog.createRecordLog(new Date(), clientAddress.getAddress().getHostAddress(),
                        String.valueOf(clientAddress.getPort()), clientAddress.getHostName(),
                        ctx.channel().id().toString(), remoteAddress.getAddress().getHostAddress(),
                        String.valueOf(remoteAddress.getPort()), remoteAddress.getHostName(), logMessage, "hello");
                    logger.info(GsonUtil.getGson().toJson(log));
                    clientBuf.add(msg.retain());
                    writeAndFlushMessage(clientAddress, remoteAddress, token);
                } else {
                    String errLog = String.format("[%s] %s : %d connection fail", Thread.currentThread().getName(),
                        remoteAddress.getHostName(), remoteAddress.getPort());
                    ZeusLog zeusLog = ZeusLog.createErrorLog(errLog, new Date());
                    logger.error(GsonUtil.getGson().toJson(zeusLog));
                    // logger.error(remoteAddress.getHostName() + ":" + remoteAddress.getPort() + " connection fail");
                    closeClientChannel();
                }
            });
        }
        clientBuf.add(msg.retain());
        writeAndFlushMessage(clientAddress, remoteAddress, token);
    }

    private void closeRemoteChannel() {
        remoteChannel.close();
        String errLog =
            String.format("[%s] remote channel %s is closed]", Thread.currentThread().getName(), remoteChannel.id());
        ZeusLog zeusLog = ZeusLog.createSystemLog(errLog, new Date());
        logger.error(GsonUtil.getGson().toJson(zeusLog));
    }

    private void closeClientChannel() {
        clientChannel.close();
        String errLog =
            String.format("[%s] client channel %s is closed]", Thread.currentThread().getName(), clientChannel.id());
        ZeusLog zeusLog = ZeusLog.createSystemLog(errLog, new Date());
        logger.error(GsonUtil.getGson().toJson(zeusLog));
        // logger.info("client channel [{}] is closed", clientChannel.id());
    }

    private void writeAndFlushMessage(InetSocketAddress sourceAddress, InetSocketAddress destAddress, String token) {
        if (remoteChannel != null && !clientBuf.isEmpty()) {
            ByteBuf messageBuf = PooledByteBufAllocator.DEFAULT.heapBuffer();
            clientBuf.forEach(byteBuf -> {
                messageBuf.writeBytes(ShadowsocksUtils.readRealBytes(byteBuf));
                ReferenceCountUtil.release(byteBuf);
            });
            ZeusLog.recordTrafficLog(sourceAddress, destAddress, token, messageBuf.readableBytes());
            remoteChannel.writeAndFlush(messageBuf.retain());
            clientBuf.clear();
        }
    }

}
