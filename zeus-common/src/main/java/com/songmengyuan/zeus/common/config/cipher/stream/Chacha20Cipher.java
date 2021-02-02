package com.songmengyuan.zeus.common.config.cipher.stream;

import com.songmengyuan.zeus.common.config.cipher.LocalStreamCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaChaEngine;

public class Chacha20Cipher extends LocalStreamCipher {
    /**
     * localStreamCipher
     *
     * @param password password
     */
    public Chacha20Cipher(String password) {
        super("chacha20", password);
    }

    @Override
    public StreamCipher getNewCipherInstance() {
        return new ChaChaEngine();
    }

    @Override
    public int getVILength() {
        return 8;
    }

    @Override
    public int getKeyLength() {
        return 32;
    }
}
