package com.songmengyuan.zeus.common.config.cipher.stream;

import com.songmengyuan.zeus.common.config.cipher.LocalStreamCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaCha7539Engine;

public class Chacha20IetfCipher extends LocalStreamCipher {
    /**
     * localStreamCipher
     *
     * @param password password
     */
    public Chacha20IetfCipher(String password) {
        super("chacha20-ietf", password);
    }

    @Override
    public StreamCipher getNewCipherInstance() {
        return new ChaCha7539Engine();
    }

    @Override
    public int getVILength() {
        return 12;
    }

    @Override
    public int getKeyLength() {
        return 32;
    }
}
