package com.songmengyuan.zeus.server;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class Socks5EncipherHandler extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        System.out.println("对数据进行编码");
        AbstractCipher cipher = ctx.channel().attr(Socks5ServerConstant.SERVER_CIPHER).get();
        byte[] data = new byte[msg.readableBytes()];
        msg.getBytes(0, data);
        byte[] encodeBytes = cipher.encodeBytes(data);
        if (encodeBytes != null && encodeBytes.length > 0) {
            out.add(Unpooled.buffer().writeBytes(encodeBytes));
        }
    }

}
