package com.jpmc.hlt.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class CipherUtils {

	static Logger log = Logger.getLogger(CipherUtils.class);
	
	private static byte[] key = {0x74, 0x68, 0x69, 0x68, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65,
			 0x74, 0x4b, 0x65, 0x79};
	
	public static String encrypt(String text) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			final String encryptedString = Base64.encodeBase64String(cipher.doFinal(text.getBytes()));
			return encryptedString;
		} catch (Exception e) {
			log.error("Exception:" + System.lineSeparator());
		}
		return null;
	}
	
	public static String decrypt(String text) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(text)));
			return decryptedString;
		} catch (Exception e) {
			log.error("Exception:" + System.lineSeparator());
		}
		return null;
	}
	
	
}
