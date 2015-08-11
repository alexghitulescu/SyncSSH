package com.adg.sync.ssh.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * Created by adg on 11/06/2015.
 */
public class AESEncrypt {

    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't',
            'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    public static String encrypt(String Data) throws GeneralSecurityException {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        return new BASE64Encoder().encode(encVal);
    }

    public static String decrypt(String encryptedData) throws GeneralSecurityException, IOException {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }

    private static Key generateKey() {
        return new SecretKeySpec(keyValue, ALGO);
    }
}
