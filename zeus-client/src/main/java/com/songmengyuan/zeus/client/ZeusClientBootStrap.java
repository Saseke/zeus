package com.songmengyuan.zeus.client;

import com.songmengyuan.zeus.client.socks5.Socks5CipherInit;
import com.songmengyuan.zeus.client.socks5.Socks5InitHandler;
import com.songmengyuan.zeus.common.config.Config;
import com.songmengyuan.zeus.common.config.ConfigLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

public class ZeusClientBootStrap {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZeusClientBootStrap.class);

	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);

	private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

	private static final ServerBootstrap clientBootstrap = new ServerBootstrap();

	private static final ZeusClientBootStrap zeusClientBootStrap = new ZeusClientBootStrap();

	private ZeusClientBootStrap() {
	}

	public static ZeusClientBootStrap getInstance() {
		return zeusClientBootStrap;
	}

	public void start(String configPath) throws Exception {
		final Config config = ConfigLoader.load(configPath);
		logger.info("load {} config file success", configPath);
		for (Map.Entry<Integer, String> portPassword : config.getPortPassword().entrySet()) {
			start0(config.getServer(), portPassword.getKey(), portPassword.getValue(), config.getLocalAddress(),
					config.getLocalPort(), config.getMethod());
		}
	}

	private static void start0(String socks5ServerAddress, Integer socks5ServerPort, String socks5Password,
			String socks5localAddress, Integer socks5LocalPort, String cipherMethod) throws InterruptedException {
		clientBootstrap.group(bossGroup, workerGroup).childOption(ChannelOption.SO_KEEPALIVE, true)
				.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch) {
						ch.pipeline().addLast("cipher_init", new Socks5CipherInit(cipherMethod, socks5Password))
								.addLast(new Socks5InitHandler(
										new InetSocketAddress(socks5ServerAddress, socks5ServerPort)));
					}
				});
		ChannelFuture future = clientBootstrap.bind(socks5LocalPort).sync();
		logger.info("zeus client [TCP] running at {}", socks5LocalPort);
		future.channel().closeFuture().sync();
	}

}
