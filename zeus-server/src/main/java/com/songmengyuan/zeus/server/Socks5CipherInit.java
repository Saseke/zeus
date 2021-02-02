package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import com.songmengyuan.zeus.common.config.cipher.CipherProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class Socks5CipherInit extends ChannelInboundHandlerAdapter {

	private final InternalLogger logger = InternalLoggerFactory.getInstance(Socks5CipherInit.class);

	private final String method;

	private final String password;

	public Socks5CipherInit(String method, String password) {
		this.method = method;
		this.password = password;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		logger.info("------------------cur remote channel id{}----------------", ctx.channel().id());
		logger.info("channel id[{}]: cipher handler is added", ctx.channel().id());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		logger.info("channel read");
		if (ctx.channel().attr(Socks5ServerConstant.SERVER_CIPHER).get() == null) {
			loadCipher(ctx);
			ctx.pipeline().remove("cipher_init");
			logger.info("channel id[{}]: cipher handler is removed ", ctx.channel().id());
		}
		super.channelRead(ctx, msg);
	}

	private void loadCipher(ChannelHandlerContext ctx) {
		AbstractCipher cipher = CipherProvider.getByName(method, password);
		if (cipher == null) {
			ctx.close();
			throw new IllegalArgumentException(method
					+ " The encryption method is not recognized. Please replace it with cha20cha20 or aes-256-cfb");
		}
		logger.info("The [{}] encryption method was loaded successfully", Socks5ServerConstant.SERVER_CIPHER);
		ctx.channel().attr(Socks5ServerConstant.SERVER_CIPHER).set(cipher);
	}

}
