package br.com.gasoutapp.infrastructure.config.security;

import br.com.gasoutapp.domain.exception.EncryptionException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author Ramon Santos
 */
@Slf4j
@UtilityClass
public class EncryptorCustom {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_NONCE_LENGTH = 12;
    private static final String KEY_FILE_PATH = "secretKey.key";
    private static final String NONCE_FILE_PATH = "nonce.key";

    private static SecretKey secretKey;
    private static byte[] nonce;

    static {
        File keyFile = new File(KEY_FILE_PATH);
        if (keyFile.exists()) {
            getSecretFromFile(keyFile);
        } else {
            generateNewKeySecretFile(keyFile);
        }

        File nonceFile = new File(NONCE_FILE_PATH);
        if (nonceFile.exists()) {
            getNonceFromFile(nonceFile);
        } else {
            generateNewNonceFile(nonceFile);
        }
    }

    private static void generateNewKeySecretFile(File keyFile) {
        try (FileOutputStream fos = new FileOutputStream(keyFile)) {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            secretKey = keyGen.generateKey();

            fos.write(secretKey.getEncoded());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    static void getSecretFromFile(File keyFile) {
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            byte[] keyBytes = new byte[(int) keyFile.length()];
            int bytesRead = fis.read(keyBytes);
            if(bytesRead > 0)
                secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
        } catch (IOException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    private static void generateNewNonceFile(File nonceFile) {
        try (FileOutputStream fos = new FileOutputStream(nonceFile)) {
            nonce = new byte[GCM_NONCE_LENGTH];
            new SecureRandom().nextBytes(nonce);

            fos.write(nonce);
        } catch (IOException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    static void getNonceFromFile(File nonceFile) {
        try (FileInputStream fis = new FileInputStream(nonceFile)) {
            nonce = new byte[(int) nonceFile.length()];
            int bytesRead = fis.read(nonce);
            if(bytesRead > 0)
                nonce = new byte[GCM_NONCE_LENGTH];
        } catch (IOException e) {
            throw new EncryptionException(e.getMessage());
        }
    }


    public static String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e){
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
        } catch (Exception e){
            throw new EncryptionException(e.getMessage());
        }
    }

}