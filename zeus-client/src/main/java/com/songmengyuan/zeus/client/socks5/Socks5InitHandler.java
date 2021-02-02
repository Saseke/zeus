package com.songmengyuan.zeus.client.socks5;

import com.songmengyuan.zeus.common.config.SocksState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;

public class Socks5InitHandler extends ChannelInboundHandlerAdapter {

	private final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());

	private SocksState state;

	private Channel clientChannel;

	private Channel remoteChannel;

	private Bootstrap remoteBootstrap;

	private final InetSocketAddress proxyAddress;

	long startTime;

	long endTime;

	public Socks5InitHandler(InetSocketAddress address) {
		this.state = SocksState.INIT;
		this.proxyAddress = address;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.clientChannel = ctx.channel();
		this.startTime = System.currentTimeMillis();
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
				if (logger.isDebugEnabled()) {
					logger.debug("{} init success", ctx.channel().id());
				}
			}
			else {
				response = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				logger.error("{} init is not socks5InitRequest", ctx.channel().id());
			}
			ReferenceCountUtil.release(msg);
			break;
		case CONNECT:
			if (msg instanceof DefaultSocks5CommandRequest) {
				DefaultSocks5CommandRequest commandRequest = (DefaultSocks5CommandRequest) msg;
				InetSocketAddress dstAddress = new InetSocketAddress(commandRequest.dstAddr(),
						commandRequest.dstPort());
				ctx.channel().attr(Socks5Constant.DST_ADDRESS).setIfAbsent(dstAddress);
				connectRemote();
				this.state = SocksState.FINISHED;
			}
			else {
				ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4))
						.addListener(ChannelFutureListener.CLOSE);
				logger.error("{} is not a commanderRequest", ctx.channel().id());
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
										clientChannel.pipeline().addLast("decryptRemote", new Socks5DecipherHandler());
									}
									clientChannel.writeAndFlush(msg.retain());
									endTime = System.currentTimeMillis();
									logger.info("Time spent :{}", String.valueOf(endTime - startTime));
								}
							});
						}
					});
			remoteBootstrap.connect(proxyAddress).addListener((ChannelFutureListener) future -> {
				if (future.isSuccess()) {
					this.remoteChannel = future.channel();
					this.clientChannel.attr(Socks5Constant.REMOTE_CHANNEL).setIfAbsent(this.remoteChannel);
					clientChannel.writeAndFlush(
							new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4));
					clientChannel.pipeline().addLast(new Socks5MessageHandler());
					logger.info("host: [{}:{}] connect success, client channelId is [{}],  remote channelId is [{}]",
							proxyAddress.getAddress(), proxyAddress.getPort(), clientChannel.id(), remoteChannel.id());
				}
				else {
					logger.error("channelId: {}, cause : {}", future.channel().id(), future.cause().getMessage());
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
