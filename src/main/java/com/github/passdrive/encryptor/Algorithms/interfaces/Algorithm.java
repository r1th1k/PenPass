package com.github.passdrive.encryptor.Algorithms.interfaces;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * The encryption algorithm has to follow
 * common interface
 * 
 * Constructor
 * Encrypt
 * Decrypt
 */

public abstract class Algorithm {
    // Overridable
    abstract public void init();

    abstract public String encrypt(String message);

    abstract public String decrypt(String encryptedMessage);

    abstract public String encrypt(String algorithm, String input, SecretKey key,
    IvParameterSpec iv);

    abstract public String decrypt(String algorithm, String cipherText, SecretKey key,
            IvParameterSpec iv);

    abstract public String getKey();

    abstract public String getIv();
}
