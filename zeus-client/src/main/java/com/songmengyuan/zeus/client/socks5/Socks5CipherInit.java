package com.songmengyuan.zeus.client.socks5;

import com.songmengyuan.zeus.common.config.cipher.AbstractCipher;
import com.songmengyuan.zeus.common.config.cipher.CipherProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Socks5CipherInit extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Socks5CipherInit.class);
    private final String method;

    private final String password;

    public Socks5CipherInit(String method, String password) {
        this.method = method;
        this.password = password;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel id[{}]: cipher handler is added", ctx.channel().id());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx.channel().attr(Socks5Constant.CLIENT_CIPHER).get() == null) {
            loadCipher(ctx);
            ctx.pipeline().remove(this);
            logger.info("channel id[{}]: cipher handler is removed ", ctx.channel().id());
        }
        super.channelRead(ctx, msg);
    }

    private void loadCipher(ChannelHandlerContext ctx) {
        AbstractCipher cipher = CipherProvider.getByName(method, password);
        if (cipher == null) {
            ctx.close();
            throw new IllegalArgumentException(method
                    + " The encryption method is not recognized. Please replace it with cha20cha20 or aes-256-cfb");
        }
        logger.info("load cipher success");
        ctx.channel().attr(Socks5Constant.CLIENT_CIPHER).set(cipher);
    }

}
