package com.songmengyuan.zeus.common.config.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * description
 */
public class ShadowsocksUtils {
    /**
     * 获取shadowsocks key
     *
     * @param password  密码
     * @param keyLength keyLength
     * @return byte[]
     */
    public static byte[] getShadowsocksKey(String password, int keyLength) {
        byte[] result = new byte[keyLength];
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            for (int hasLength = 0; hasLength < keyLength; hasLength += 16) {
                byte[] passwordBytes = password.getBytes();

                //组合需要摘要的byte[]
                byte[] combineBytes = new byte[hasLength + passwordBytes.length];
                System.arraycopy(result, 0, combineBytes, 0, hasLength);
                System.arraycopy(passwordBytes, 0, combineBytes, hasLength, passwordBytes.length);

                //增加
                byte[] digestBytes = messageDigest.digest(combineBytes);
                int addLength = hasLength + 16 > keyLength ? keyLength - hasLength : 16;
                System.arraycopy(digestBytes, 0, result, hasLength, addLength);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取ip
     *
     * @param msg msg
     * @return InetSocketAddress
     */
    public static InetSocketAddress getIp(ByteBuf msg) {
        SocksAddressType addressType = SocksAddressType.valueOf(msg.readByte());
        String host = null;
        int port = 0;

        switch (addressType) {
            case IPv4: {
                host = SocksIpUtils.intToIp(msg.readInt());
                port = msg.readUnsignedShort();
                break;
            }
            case DOMAIN: {
                int length = msg.readByte();
                host = msg.readBytes(length).toString(Charset.forName("ASCII"));
                port = msg.readUnsignedShort();
                break;
            }
            case IPv6: {
                host = SocksIpUtils.ipv6toStr(ByteBufUtil.getBytes(msg.readBytes(16)));
                port = msg.readUnsignedShort();
                break;
            }
            case UNKNOWN: {
                System.out.println("未知类型");
                break;
            }
            default: {
                System.out.println("unknown addressType");
            }
        }
        return !StringUtil.isNullOrEmpty(host) ? new InetSocketAddress(host, port) : null;
    }

    /**
     * 读取ByteBuf中可read的字符数组
     *
     * @param msg msg
     * @return byte[]
     */
    public static byte[] readRealBytes(ByteBuf msg) {
        byte[] message = new byte[msg.readableBytes()];
        msg.readBytes(message);
        return message;
    }

    /**
     * 获取ByteBuf中可read的字符数组
     *
     * @param msg msg
     * @return byte[]
     */
    public static byte[] getRealBytes(ByteBuf msg) {
        byte[] message = new byte[msg.readableBytes()];
        msg.getBytes(0, message);
        return message;
    }

    /**
     * 打印debugBytes
     *
     * @param data data
     */
    public static void printDebugBytes(byte[] data) {
        for (byte aData : data) {
            System.out.print(aData + ",");
        }
        System.out.println();
    }

}
