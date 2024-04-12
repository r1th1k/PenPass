package com.github.passdrive.encryptor.Algorithms;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.github.passdrive.encryptor.Algorithms.interfaces.Algorithm;

public class TripleDES extends Algorithm {

    @Override
    public String encrypt(String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encrypt'");
    }

    @Override
    public String decrypt(String encryptedMessage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decrypt'");
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }

    @Override
    public String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encrypt'");
    }

    @Override
    public String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decrypt'");
    }

    @Override
    public String getKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKey'");
    }

    @Override
    public String getIv() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIv'");
    }
    
}
