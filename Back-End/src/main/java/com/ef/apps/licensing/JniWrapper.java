package com.ef.apps.licensing;

public class JniWrapper {
    public native static String encrypt(String key, String plainText);

    public native static String encrypt(String plainText);

    public native static String decrypt(String key, String cipherText);

    public native static String decrypt(String cipherText);
}