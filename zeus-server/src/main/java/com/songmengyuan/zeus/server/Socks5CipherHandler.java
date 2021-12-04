package com.songmengyuan.zeus.server;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;
import com.songmengyuan.zeus.common.config.util.ShadowsocksUtils;
import com.songmengyuan.zeus.common.config.util.TokenUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class Socks5CipherHandler extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(Socks5CipherHandler.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        ZeusLog log;
        String message = String.format("[%s] 对数据进行了解密", Thread.currentThread().getName());
        log = ZeusLog.createSystemLog(message, new Date());
        logger.info(GsonUtil.getGson().toJson(log));
        AbstractCipher cipher = ctx.channel().attr(Socks5ServerConstant.SERVER_CIPHER).get();
        if (cipher == null) {
            ctx.close();
            String errMessage = String.format("[%s] not found cipher method", Thread.currentThread().getName());
            log = ZeusLog.createErrorLog(errMessage, new Date());
            logger.error(GsonUtil.getGson().toJson(log));
            return;
        }
        byte[] bytes = cipher.decodeBytes(msg);
        msg.clear().writeBytes(bytes);
        if (ctx.channel().attr(Socks5ServerConstant.DST_ADDRESS).get() == null) {
            InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
            InetSocketAddress inetSocketAddress = ShadowsocksUtils.getIp(msg);
            if (inetSocketAddress == null) {
                ctx.channel().close();
                String errMessage =
                    String.format("[%s] destination IP address not found", Thread.currentThread().getName());
                log = ZeusLog.createErrorLog(errMessage, new Date());
                logger.error(GsonUtil.getGson().toJson(log));
                return;
            }
            ctx.channel().attr(Socks5ServerConstant.DST_ADDRESS).set(inetSocketAddress);
            String token = TokenUtil.getToken(msg);
            ctx.channel().attr(Socks5ServerConstant.USER_TOKEN).setIfAbsent(token);
            // logger.info("client ip is {}. request server {}", clientAddress.getAddress(),
            // inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort());
            String logMessage = String.format("[%s] receive message from client", Thread.currentThread().getName());
            log = ZeusLog.createRecordLog(new Date(), clientAddress.getAddress().getHostAddress(),
                String.valueOf(clientAddress.getPort()), clientAddress.getHostName(), ctx.channel().id().toString(),
                inetSocketAddress.getAddress().getHostAddress(), String.valueOf(inetSocketAddress.getPort()),
                inetSocketAddress.getHostName(), logMessage, token);
            logger.info(GsonUtil.getGson().toJson(log));
            // 计算流量
            ZeusLog.recordTrafficLog(clientAddress, inetSocketAddress, token, bytes.length);
        }
        out.add(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        String message = String.format("[%s] a serious error occurred,error info :%s", Thread.currentThread().getName(),
            cause.getMessage());
        ZeusLog log = ZeusLog.createErrorLog(message, new Date());
        logger.error(GsonUtil.getGson().toJson(log));
    }
}
