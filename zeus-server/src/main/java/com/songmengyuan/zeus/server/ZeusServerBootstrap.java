package com.songmengyuan.zeus.server;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.songmengyuan.zeus.common.config.config.Config;
import com.songmengyuan.zeus.common.config.config.ConfigLoader;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ZeusServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ZeusServerBootstrap.class);
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private static final ServerBootstrap serverBootstrap = new ServerBootstrap();

    private static final ZeusServerBootstrap zeusServerBootstrap = new ZeusServerBootstrap();

    private ZeusServerBootstrap() {}

    public static ZeusServerBootstrap getInstance() {
        return zeusServerBootstrap;
    }

    public void start(String configPath) throws Exception {
        final Config config = ConfigLoader.load(configPath);
        String message =
            String.format("[%s] load %s config file success.", Thread.currentThread().getName(), configPath);
        ZeusLog log = ZeusLog.createSystemLog(message, new Date());
        logger.info(GsonUtil.getGson().toJson(log));
        for (Map.Entry<Integer, String> portPassword : config.getPortPassword().entrySet()) {
            start0(config.getServer(), portPassword.getKey(), portPassword.getValue(), config.getMethod());
        }
    }

    private static void start0(String socks5ServerAddress, Integer socks5ServerPort, String socks5Password,
        String cipherMethod) throws InterruptedException {
        serverBootstrap.group(bossGroup, workerGroup).childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(new Socks5CipherInit(cipherMethod, socks5Password))
                        .addLast(new Socks5CipherHandler()).addLast(new Socks5EncipherHandler())
                        .addLast(new Socks5MessageHandler());
                }
            });
        ChannelFuture future = serverBootstrap.bind(socks5ServerPort).sync();
        String message =
            String.format("[%s] zeus server [TCP] running at %d", Thread.currentThread().getName(), socks5ServerPort);
        ZeusLog log = ZeusLog.createSystemLog(message, new Date());
        logger.info(GsonUtil.getGson().toJson(log));
        future.channel().closeFuture().sync();
    }
}