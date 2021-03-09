package com.songmengyuan.zeus.common.config.cipher.stream;

import com.songmengyuan.zeus.common.config.cipher.LocalStreamCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;

public class Aes128CfbCipher extends LocalStreamCipher {

    /**
     * localStreamCipher
     *
     * @param password password
     */
    public Aes128CfbCipher(String password) {
        super("aes-128-cfb", password);
    }

    @Override
    public StreamCipher getNewCipherInstance() {
        return new CFBBlockCipher(new AESEngine(), getVILength() * 8);
    }

    @Override
    public int getVILength() {
        return 16;
    }

    @Override
    public int getKeyLength() {
        return 16;
    }

}
