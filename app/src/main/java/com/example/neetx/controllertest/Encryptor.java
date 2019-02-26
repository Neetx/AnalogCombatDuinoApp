package com.example.neetx.controllertest;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class Encryptor {
    Key key;
    GCMParameterSpec ivSpec;
    byte[] iv = new byte[12];

    Encryptor(byte[] key, byte[] iv) {
        if (key.length != 32) throw new IllegalArgumentException();
        this.key = new SecretKeySpec(key, "AES");
        assert iv.length == 12;
        this.iv = iv;
        this.ivSpec = new GCMParameterSpec(16 * Byte.SIZE, iv);
    }

    Encryptor(){};

    byte[] encrypt(byte[] src) throws Exception {
        //GCMParameterSpec ivSpec = new GCMParameterSpec(16 * Byte.SIZE, iv);
        //IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, this.ivSpec);
        byte[] cipherText = cipher.doFinal(src);
        //System.out.println( javax.xml.bind.DatatypeConverter.printHexBinary(cipherText));
        assert cipherText.length == src.length + 16; // See question #3
        byte[] message = new byte[12 + src.length + 16]; // See question #4
        System.arraycopy(this.iv, 0, message, 0, 12);
        System.arraycopy(cipherText, 0, message, 12, cipherText.length);
        return message;
    }

    // the input comes from users
    byte[] decrypt(byte[] message) throws Exception {
        if (message.length < 12 + 16) throw new IllegalArgumentException();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher.doFinal(message, 12, message.length - 12);
    }

    public static byte[] encode(byte[] key, byte[] data) throws Exception {
          Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
          SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
          sha256_HMAC.init(secret_key);

          return sha256_HMAC.doFinal(data);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}