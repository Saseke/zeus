package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.Config;
import com.songmengyuan.zeus.common.config.ConfigLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Map;

public class ZeusServerBootstrap {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZeusServerBootstrap.class);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private static final ServerBootstrap serverBootstrap = new ServerBootstrap();

    private static final ZeusServerBootstrap zeusServerBootstrap = new ZeusServerBootstrap();

    private ZeusServerBootstrap() {
    }

    public static ZeusServerBootstrap getInstance() {
        return zeusServerBootstrap;
    }

    public void start(String configPath) throws Exception {
        final Config config = ConfigLoader.load(configPath);
        logger.info("load {} config file success", configPath);
        for (Map.Entry<Integer, String> portPassword : config.getPortPassword().entrySet()) {
            start0(config.getServer(), portPassword.getKey(), portPassword.getValue(), config.getMethod());
        }
    }

    private static void start0(String socks5ServerAddress, Integer socks5ServerPort, String socks5Password,
                               String cipherMethod) throws InterruptedException {
        serverBootstrap.group(bossGroup, workerGroup).childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new Socks5CipherInit(cipherMethod, socks5Password))
                        .addLast(new Socks5CipherHandler())
                        .addLast(new Socks5EncipherHandler())
                        .addLast(new Socks5MessageHandler());
            }
        });
        ChannelFuture future = serverBootstrap.bind(socks5ServerPort).sync();
        logger.info("zeus server [TCP] running at {}", socks5ServerPort);
        future.channel().closeFuture().sync();
    }

}
