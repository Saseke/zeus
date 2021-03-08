package com.songmengyuan.zeus.client.socks5;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class Socks5DecipherHandler extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        AbstractCipher cipher = ctx.channel().attr(Socks5Constant.CLIENT_CIPHER).get();
        out.writeBytes(cipher.decodeBytes(msg.retain()));
    }
}
