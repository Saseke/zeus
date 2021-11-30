package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import com.songmengyuan.zeus.common.config.util.ShadowsocksUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class Socks5CipherHandler extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(Socks5CipherHandler.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        System.out.println("对数据进行了解密");
        AbstractCipher cipher = ctx.channel().attr(Socks5ServerConstant.SERVER_CIPHER).get();
        if (cipher == null) {
            ctx.close();
            logger.error("not found cipher method");
            return;
        }
        byte[] bytes = cipher.decodeBytes(msg);
        msg.clear().writeBytes(bytes);
        if (ctx.channel().attr(Socks5ServerConstant.DST_ADDRESS).get() == null) {
            InetSocketAddress clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            InetSocketAddress inetSocketAddress = ShadowsocksUtils.getIp(msg);
            if (inetSocketAddress == null) {
                ctx.channel().close();
                logger.error("destination IP address not found");
                return;
            }
            ctx.channel().attr(Socks5ServerConstant.DST_ADDRESS).set(inetSocketAddress);
            logger.info("client ip is {}. request server {}", clientAddress.getAddress(), inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());
        }
        out.add(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.error("a serious error occurred");
    }

}
