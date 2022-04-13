package com.songmengyuan.zeus.client;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;

import com.songmengyuan.zeus.client.socks5.Socks5Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.songmengyuan.zeus.client.socks5.Socks5CipherInit;
import com.songmengyuan.zeus.client.socks5.Socks5InitHandler;
import com.songmengyuan.zeus.common.config.config.Config;
import com.songmengyuan.zeus.common.config.config.ConfigLoader;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ZeusClientBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(ZeusClientBootStrap.class);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private static final ServerBootstrap clientBootstrap = new ServerBootstrap();

    private static final ZeusClientBootStrap zeusClientBootStrap = new ZeusClientBootStrap();

    private ZeusClientBootStrap() {}

    public static ZeusClientBootStrap getInstance() {
        return zeusClientBootStrap;
    }

    public void start(String configPath) throws Exception {
        final Config config = ConfigLoader.load(configPath);
        String message =
            String.format("[%s] load %s config file success", Thread.currentThread().getName(), configPath);
        ZeusLog log = ZeusLog.createSystemLog(message, new Date());
        logger.info(GsonUtil.getGson().toJson(log));
        for (Map.Entry<Integer, String> portPassword : config.getPortPassword().entrySet()) {
            start0(config.getServer(), portPassword.getKey(), portPassword.getValue(), config.getLocalAddress(),
                config.getLocalPort(), config.getMethod(), config.getToken());
        }
    }

    private static void start0(String socks5ServerAddress, Integer socks5ServerPort, String socks5Password,
        String socks5localAddress, Integer socks5LocalPort, String cipherMethod, String token)
        throws InterruptedException {
        clientBootstrap.group(bossGroup, workerGroup).childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.attr(Socks5Constant.TOKEN).set(token);
                    ch.pipeline().addLast(new Socks5CipherInit(cipherMethod, socks5Password))
                        .addLast(new Socks5InitHandler(new InetSocketAddress(socks5ServerAddress, socks5ServerPort)));
                }
            });
        ChannelFuture future = clientBootstrap.bind(socks5LocalPort).sync();
        String message =
            String.format("[%s] zeus client [TCP] running at %d", Thread.currentThread().getName(), socks5LocalPort);
        ZeusLog log = ZeusLog.createSystemLog(message, new Date());
        logger.info(GsonUtil.getGson().toJson(log));
        // logger.info("zeus client [TCP] running at {}", socks5LocalPort);
        future.channel().closeFuture().sync();
    }

}
