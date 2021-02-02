package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public class Socks5ServerConstant {

	public static AttributeKey<AbstractCipher> SERVER_CIPHER = AttributeKey.valueOf("serverCipher");

	public static AttributeKey<InetSocketAddress> DST_ADDRESS = AttributeKey.valueOf("dstAddress");

}
