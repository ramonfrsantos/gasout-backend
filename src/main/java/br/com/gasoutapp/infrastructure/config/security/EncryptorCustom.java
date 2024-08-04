package br.com.gasoutapp.infrastructure.config.security;

import br.com.gasoutapp.domain.exception.EncryptionException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author Ramon Santos
 */
@Slf4j
@UtilityClass
public class EncryptorCustom {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final byte[] nonce = new byte[12];
    private static final String KEY_FILE = "secret.key";

    private static final SecretKey secretKey;

    static {
        Path path = Paths.get(KEY_FILE);
        secretKey = getSecretFromPath(path);
    }

    public static String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    public static String decrypt(String ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decodedCiphertext = Base64.getDecoder().decode(ciphertext);
            byte[] plaintext = cipher.doFinal(decodedCiphertext);
            return new String(plaintext);
        } catch (Exception e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    private static SecretKey getSecretFromPath(Path path) {
        try {
            if (!Files.exists(path)) {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256);
                var newKey = keyGen.generateKey();
                Files.write(path, newKey.getEncoded(), StandardOpenOption.CREATE);

                return newKey;
            }

            byte[] keyBytes = Files.readAllBytes(path);
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new EncryptionException(e.getMessage());
        }
    }
}