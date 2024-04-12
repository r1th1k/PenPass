package com.github.passdrive.encryptor.Algorithms;

/*
 * UNDER
 * JAVA CRYPTOGRAPHIC PROVIDER
 * FRAMEWORK
 */

import com.github.passdrive.encryptor.Algorithms.interfaces.Algorithm;
import com.github.passdrive.Environment.EnvironmentImpl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES extends Algorithm {

    // Key Generation password based
    private static SecretKey getKeyFromPassword(String masterPassword, String salt) {

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                    .getEncoded(), "AES");
            return secret;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Init Vector
    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Salt generation
    private static String getSalt() {
        SecureRandom secureRandom = new SecureRandom();
        int saltLength = 16;

        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);

        return salt.toString();
    }

    // Storing secret / iv to Environment
    public void init() {
        // Persistant Env

        String masterPassword = (String) EnvironmentImpl.getEnvironmentMap("master");
        String salt = AES.getSalt();
        SecretKey secret = AES.getKeyFromPassword(masterPassword, salt);
        IvParameterSpec iv = AES.generateIv();

        if (EnvironmentImpl.getEnvironmentMap("secret-aes") == null)
        EnvironmentImpl.setEnvironmentMap("secret-aes", secret);

        // Adding secret to Environment
        if (EnvironmentImpl.getEnvironmentMap("iv-aes") == null)
        EnvironmentImpl.setEnvironmentMap("iv-aes", iv);
    }

    // Encryption helper function
    public String encrypt(String algorithm, String input, SecretKey key,
            IvParameterSpec iv) {

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        } catch (InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException
                | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            return null;
        }
    }

    // Decryption helper function
    public String decrypt(String algorithm, String cipherText, SecretKey key,
            IvParameterSpec iv) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            return new String(plainText);
        } catch (InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException
                | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            return null;
        }
    }

    @Override
    public String encrypt(String message) {
        init();
        SecretKey secret = (SecretKey) EnvironmentImpl.getEnvironmentMap("secret-aes");
        IvParameterSpec iv = (IvParameterSpec) EnvironmentImpl.getEnvironmentMap("iv-aes");

        return encrypt("AES/CBC/PKCS5Padding", message, secret, iv);
    }

    @Override
    public String decrypt(String encryptedMessage) {
        SecretKey secret = (SecretKey) EnvironmentImpl.getEnvironmentMap("secret");
        IvParameterSpec iv = (IvParameterSpec) EnvironmentImpl.getEnvironmentMap("iv");

        return decrypt("AES/CBC/PKCS5Padding", encryptedMessage, secret, iv);
    }

    public String getKey() {
        SecretKey secret = (SecretKey) EnvironmentImpl.getEnvironmentMap("secret-aes");
        String key = Base64.getEncoder().encodeToString(secret.getEncoded());
        return key;
        // new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
    }

    public String getIv() {
        IvParameterSpec iv = (IvParameterSpec) EnvironmentImpl.getEnvironmentMap("iv-aes");
        String ivString = Base64.getEncoder().encodeToString(iv.getIV());
        return ivString;
        // Convert ivString to IvParameterSpec again?
        // new IvParameterSpec(Base64.getDecoder().decode(ivString));
    }
}
