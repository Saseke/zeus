package com.songmengyuan.zeus.common.config.cipher.stream;

import com.songmengyuan.zeus.common.config.cipher.LocalStreamCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Rc4Md5Cipher extends LocalStreamCipher {

    /**
     * localStreamCipher
     *
     * @param password password
     */
    public Rc4Md5Cipher(String password) {
        super("rc4-md5", password);
    }

    @Override
    public StreamCipher getNewCipherInstance() {
        return new RC4Engine();
    }

    @Override
    public int getVILength() {
        return 16;
    }

    @Override
    public int getKeyLength() {
        return 16;
    }

    @Override
    public byte[] decodeBytes(byte[] secretBytes) {
        int offset = 0;
        if (!decodeInit) {
            //parameter
            byte[] ivBytes = new byte[getVILength()];
            System.arraycopy(secretBytes, 0, ivBytes, 0, offset = getVILength());

            CipherParameters keyParameter = new KeyParameter(getRc4Keys(ivBytes));

            //init
            decodeStreamCipher.init(false, keyParameter);
            decodeInit = true;
        }

        byte[] target = new byte[secretBytes.length - offset];
        decodeStreamCipher.processBytes(secretBytes, offset, secretBytes.length - offset, target, 0);
        return target;
    }

    @Override
    public byte[] encodeBytes(byte[] originBytes) {
        byte[] target;
        target = encodeIVBytes == null ? new byte[getVILength() + originBytes.length] : new byte[originBytes.length];
        int outOff = 0;

        if (encodeIVBytes == null) {
            encodeIVBytes = getRandomBytes(getVILength());
            System.arraycopy(encodeIVBytes, 0, target, 0, encodeIVBytes.length);
            outOff = getVILength();

            CipherParameters keyParameter = new KeyParameter(getRc4Keys(encodeIVBytes));
            encodeStreamCipher.init(true, keyParameter);
        }

        encodeStreamCipher.processBytes(originBytes, 0, originBytes.length, target, outOff);
        return target;
    }

    /**
     * 获取rc4-md5 parameterKey
     *
     * @param ivBytes ivBytes
     * @return keyParameterBytes
     */
    private byte[] getRc4Keys(byte[] ivBytes) {
        byte[] result = new byte[getKeyLength() + getVILength()];
        System.arraycopy(getKey(), 0, result, 0, getKeyLength());
        System.arraycopy(ivBytes, 0, result, getKeyLength(), ivBytes.length);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            return messageDigest.digest(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }
}
