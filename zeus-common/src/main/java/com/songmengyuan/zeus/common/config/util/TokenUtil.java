package com.songmengyuan.zeus.common.config.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class TokenUtil {
    public static String getToken(ByteBuf msg) {
        int length = msg.readInt();
        ByteBuf buf = msg.readBytes(length);
        return buf.toString(StandardCharsets.UTF_8);
    }
}
