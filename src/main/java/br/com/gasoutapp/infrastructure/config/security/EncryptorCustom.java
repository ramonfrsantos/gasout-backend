package br.com.gasoutapp.infrastructure.config.security;

import br.com.gasoutapp.domain.exception.EncryptionException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Ramon Santos
 */
@Slf4j
@UtilityClass
public class EncryptorCustom {
    private static final SecretKey key;
    private static final Cipher cipher;
    private static final Base64 coder;
    private static final String SECRET;

    static {
        SECRET = "GasoutappCONFIG9";

        try {
            key = new SecretKeySpec(SECRET.getBytes(), "AES");

            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalArgumentException e) {
            throw new EncryptionException(e.getMessage());
        }
        coder = new Base64();
    }

    public static synchronized String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return "";
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
            String s = new String(coder.encode(cipherText));
            return s.replace("\n", "")
                    .replace("\r", "");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionException(e.getMessage());
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
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

}