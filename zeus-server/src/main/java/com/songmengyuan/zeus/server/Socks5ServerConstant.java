package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.Objects;

public class Socks5ServerConstant {

    public static AttributeKey<AbstractCipher> SERVER_CIPHER = AttributeKey.valueOf("serverCipher");

    public static AttributeKey<InetSocketAddress> DST_ADDRESS = AttributeKey.valueOf("dstAddress");
    // 开发时用的path
//    public static String confPath = Objects.requireNonNull(Socks5ServerConstant.class.getClassLoader().getResource("config.json")).getPath();
    public static String confPath = "config.json";

}
