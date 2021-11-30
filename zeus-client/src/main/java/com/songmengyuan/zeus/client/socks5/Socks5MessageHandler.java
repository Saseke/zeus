package com.songmengyuan.zeus.client.socks5;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import sun.net.util.IPAddressUtil;

public class Socks5MessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private static final Logger logger = LoggerFactory.getLogger(Socks5MessageHandler.class);


	private Channel clientChannel;

	private Channel remoteChannel;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		if (clientChannel == null) {
			clientChannel = ctx.channel();
		}
		if (remoteChannel == null) {
			remoteChannel = ctx.channel().attr(Socks5Constant.REMOTE_CHANNEL).get();
			logger.info("------------------cur remote channel id{}----------------", remoteChannel.id());
		}
		logger.info("client channel id[{}]: receive info from client {} bytes", clientChannel.id(),
				msg.readableBytes());

		System.out.println("对数据进行了编码");
		remoteChannel.writeAndFlush(getCryptMessage(msg.retain()));
	}

	private ByteBuf getCryptMessage(ByteBuf message) throws Exception {
		try {
			ByteBuf msg = clientChannel.alloc().heapBuffer();

			boolean isFirstEncoding = clientChannel.attr(Socks5Constant.FIRST_ENCODING).get() == null
					|| clientChannel.attr(Socks5Constant.FIRST_ENCODING).get();
			if (isFirstEncoding) {
				InetSocketAddress queryAddress = clientChannel.attr(Socks5Constant.DST_ADDRESS).get();
				if (queryAddress == null) {
					close();
					throw new IllegalArgumentException("no remote address");
				}
				String queryHost = queryAddress.getHostName();
				if (IPAddressUtil.isIPv4LiteralAddress(queryHost)) {
					msg.writeByte(0x01); // ipv4
					msg.writeBytes(IPAddressUtil.textToNumericFormatV4(queryHost));
					msg.writeShort(queryAddress.getPort());
				}
				else if (IPAddressUtil.isIPv6LiteralAddress(queryHost)) {
					msg.writeByte(0x04);// ipv6
					msg.writeBytes(IPAddressUtil.textToNumericFormatV6(queryHost));
					msg.writeShort(queryAddress.getPort());
				}
				else {
					msg.writeByte(0x03);// domain type
					byte[] asciiHost = AsciiString.of(queryHost).array();
					msg.writeByte(asciiHost.length);// domain type
					msg.writeBytes(asciiHost);
					msg.writeShort(queryAddress.getPort());
				}
			}

			byte[] payload = new byte[message.readableBytes()];
			message.readBytes(payload);
			msg.writeBytes(payload);
			return crypt(msg);
		}
		finally {
			clientChannel.attr(Socks5Constant.FIRST_ENCODING).setIfAbsent(false);
		}
	}

	private ByteBuf crypt(ByteBuf message) throws Exception {
		try {
			ByteBuf byteBuf = clientChannel.alloc().heapBuffer();
			AbstractCipher cipher = clientChannel.attr(Socks5Constant.CLIENT_CIPHER).get();
			if (cipher == null) {
				close();
				throw new Exception("The current encryption mode is incorrect");
			}
			byte[] info = new byte[message.readableBytes()];
			message.readBytes(info);
			byte[] encryptInfo = cipher.encodeBytes(info);
			byteBuf.writeBytes(encryptInfo);
			return byteBuf;
		}
		finally {
			ReferenceCountUtil.release(message);
		}
	}

	private void close() {
		if (clientChannel != null) {
			clientChannel.close();
		}
		if (remoteChannel != null) {
			remoteChannel.close();
		}
	}

}
