package com.songmengyuan.zeus.client.socks5;

import java.net.InetSocketAddress;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.songmengyuan.zeus.common.config.constant.SocksState;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.util.ReferenceCountUtil;

public class Socks5InitHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Socks5InitHandler.class);

    private SocksState state;

    private Channel clientChannel;

    private Channel remoteChannel;

    private Bootstrap remoteBootstrap;

    private final InetSocketAddress proxyAddress;

    public Socks5InitHandler(InetSocketAddress address) {
        this.state = SocksState.INIT;
        this.proxyAddress = address;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.clientChannel = ctx.channel();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ctx.pipeline().addFirst("socks5-init", new Socks5InitialRequestDecoder()).addLast(new Socks5ServerEncoder());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        switch (state) {
            case INIT:
                Socks5InitialResponse response;
                if (msg instanceof Socks5InitialRequest) {
                    response = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
                    ctx.channel().writeAndFlush(response);
                    ctx.pipeline().addBefore(ctx.name(), "socks5-command", new Socks5CommandRequestDecoder());
                    this.state = SocksState.CONNECT;
                } else {
                    response = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
                    ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

                    String message = String.format("[%s] %s init is not socks5InitRequest",
                        Thread.currentThread().getName(), ctx.channel().id());
                    ZeusLog log = ZeusLog.createErrorLog(message, new Date());
                    logger.info(GsonUtil.getGson().toJson(log));
                }
                ReferenceCountUtil.release(msg);
                break;
            case CONNECT:
                if (msg instanceof DefaultSocks5CommandRequest) {
                    DefaultSocks5CommandRequest commandRequest = (DefaultSocks5CommandRequest)msg;
                    InetSocketAddress dstAddress =
                        new InetSocketAddress(commandRequest.dstAddr(), commandRequest.dstPort());
                    ctx.channel().attr(Socks5Constant.DST_ADDRESS).setIfAbsent(dstAddress);
                    connectRemote();
                    this.state = SocksState.FINISHED;
                } else {
                    ctx.channel()
                        .writeAndFlush(
                            new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4))
                        .addListener(ChannelFutureListener.CLOSE);
                    // logger.error("{} is not a commanderRequest", ctx.channel().id());
                    String message = String.format("[%s] %s is not a commanderRequest",
                        Thread.currentThread().getName(), ctx.channel().id());
                    ZeusLog log = ZeusLog.createErrorLog(message, new Date());
                    logger.info(GsonUtil.getGson().toJson(log));
                }
                ReferenceCountUtil.release(msg);
                break;
            case FINISHED:
                ctx.fireChannelRead(msg);
                break;
        }
    }

    private void connectRemote() {
        if (remoteBootstrap == null) {
            remoteBootstrap = new Bootstrap();
            remoteBootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                if (clientChannel.pipeline().get(Socks5DecipherHandler.class) == null) {
                                    clientChannel.pipeline().addFirst(new Socks5DecipherHandler());
                                }
                                clientChannel.writeAndFlush(msg.retain());
                            }
                        });
                    }
                });
            remoteBootstrap.connect(proxyAddress).addListener((ChannelFutureListener)future -> {
                if (future.isSuccess()) {
                    this.remoteChannel = future.channel();
                    this.clientChannel.attr(Socks5Constant.REMOTE_CHANNEL).setIfAbsent(this.remoteChannel);
                    clientChannel.writeAndFlush(
                        new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4));
                    clientChannel.pipeline().addLast(new Socks5MessageHandler());
                    // logger.info("host: {} connect success, client channelId is {}, ",
                    // proxyAddress.getAddress() + ":" + proxyAddress.getPort(), clientChannel.id());
                    String message = String.format("[%s] host: %s connect success, client channelId is %s, ",
                        Thread.currentThread().getName(), proxyAddress.getAddress() + ":" + proxyAddress.getPort(),
                        clientChannel.id());
                    ZeusLog log = ZeusLog.createSystemLog(message, new Date());
                    logger.info(GsonUtil.getGson().toJson(log));
                } else {
                    // logger.error("channelId: {}, cause : {}", future.channel().id(), future.cause().getMessage());
                    String message = String.format("[%s] channelId: %s, cause : %s", Thread.currentThread().getName(),
                        future.channel().id(), future.cause().getMessage());
                    ZeusLog log = ZeusLog.createErrorLog(message, new Date());
                    logger.error(GsonUtil.getGson().toJson(log));
                    closeChannel();
                }
            });
        }
    }

    private void closeChannel() {
        if (remoteChannel != null) {
            remoteChannel.close();
            remoteChannel = null;
        }
        if (clientChannel != null) {
            clientChannel.close();
            clientChannel = null;
        }
        if (remoteBootstrap != null) {
            remoteBootstrap = null;
        }
    }

}
