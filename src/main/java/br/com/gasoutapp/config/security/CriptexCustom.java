package br.com.gasoutapp.config.security;


import java.util.logging.Level;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import lombok.extern.java.Log;

/**
 * @author Ramon Santos
 */
@Log
public class CriptexCustom {

    private static final String secret = "GasoutappCONFIG9"; // secret key length must be 16

    private static SecretKey key;

    private static Cipher cipher;

    private static Base64 coder;

    static {
        try {
            key = new SecretKeySpec(secret.getBytes(), "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            coder = new Base64();
        } catch (Throwable t) {
            throw new RuntimeException("Erro ao configurar classe CriptexCustom.", t);
        }
    }

    public static synchronized String encrypt(Long number) {
        return encrypt(Long.toString(number));
    }

    public static synchronized String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return "";
        }

        try {

            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            String s = new String(coder.encode(cipherText));
            return s.replaceAll("\n", "")
                    .replaceAll("\r", "");

        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }

    public static synchronized String decrypt(String codedText) {
        if (StringUtils.isBlank(codedText)) {
            return "";
        }

        try {

            byte[] encypted = coder.decode(codedText.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(encypted);
            return new String(decrypted);

        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return "";
    }

}