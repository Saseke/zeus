package com.songmengyuan.zeus.common.config.cipher;

import com.songmengyuan.zeus.common.config.cipher.stream.Aes256CfbCipher;
import com.songmengyuan.zeus.common.config.cipher.stream.Chacha20Cipher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * cipherProvider
 *
 */
public class CipherProvider {

	private static Map<String, Class<? extends AbstractCipher>> cipherMap = new HashMap<>();

	static {
		/* aes */
		cipherMap.put("aes-256-cfb", Aes256CfbCipher.class);

		/* chacha20 */
		cipherMap.put("chacha20", Chacha20Cipher.class);
	}

	/**
	 * get Cipher by standard cipherName
	 * @param cipherName cipherName
	 * @param password password
	 * @return new cipher instance
	 */
	public static AbstractCipher getByName(String cipherName, String password) {
		if (cipherMap.containsKey(cipherName)) {
			Class<? extends AbstractCipher> cipherClazz = cipherMap.get(cipherName);
			try {
				Constructor<? extends AbstractCipher> cipherClazzConstructor = cipherClazz.getConstructor(String.class);
				return cipherClazzConstructor.newInstance(password);
			}
			catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException
					| InstantiationException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
