package com.songmengyuan.zeus.client.socks5;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.Objects;

public class Socks5Constant {

    public static final AttributeKey<Channel> REMOTE_CHANNEL = AttributeKey.valueOf("remoteChannel");

    public static AttributeKey<Boolean> FIRST_ENCODING = AttributeKey.valueOf("firstEncoding");

    public static AttributeKey<InetSocketAddress> DST_ADDRESS = AttributeKey.valueOf("dstAddress");

    public static AttributeKey<AbstractCipher> CLIENT_CIPHER = AttributeKey.valueOf("clientCipher");
    public static AttributeKey<String> TOKEN = AttributeKey.valueOf("token");
    // public static String confPath =
    // Objects.requireNonNull(Socks5Constant.class.getClassLoader().getResource("config.json")).getFile();
    public static String confPath = "config.json";

}
