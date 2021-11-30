package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Socks5EncipherHandler extends MessageToMessageEncoder<ByteBuf> {
    private static final Logger logger = LoggerFactory.getLogger(Socks5EncipherHandler.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        logger.info("对数据进行编码");
        AbstractCipher cipher = ctx.channel().attr(Socks5ServerConstant.SERVER_CIPHER).get();
        byte[] data = new byte[msg.readableBytes()];
        msg.getBytes(0, data);
        byte[] encodeBytes = cipher.encodeBytes(data);
        if (encodeBytes != null && encodeBytes.length > 0) {
            out.add(Unpooled.buffer().writeBytes(encodeBytes));
        }
    }

}
