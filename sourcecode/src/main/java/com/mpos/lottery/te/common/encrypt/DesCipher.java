package com.mpos.lottery.te.common.encrypt;

import com.mpos.lottery.te.common.util.Base64Coder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DesCipher {

    /**
     * Decrypt by DES.
     * 
     * @param cipherBytes
     *            THe cipher byte array.
     * @param keyBytes
     *            THe DES key in byte array representation.
     * @return bytes of encryption.
     */
    public static byte[] decrypt(byte[] cipherBytes, byte[] keyBytes) throws Exception {
        SecretKey desKey = new SecretKeySpec(keyBytes, "DES");

        Cipher desCipher;
        // Create the cipher
        // desCipher = Cipher.getInstance("DES/CFB8/NoPadding");
        desCipher = Cipher.getInstance("DES");
        desCipher.init(Cipher.DECRYPT_MODE, desKey);
        byte[] decryptedBytes = desCipher.doFinal(cipherBytes);
        return decryptedBytes;
    }

    /**
     * Encrypt by DES.
     * 
     * @param input
     *            The row input, will be translated into bytes by UTF-8.
     * @param keys
     *            THe DES key in byte array representation.
     * @return bytes of encryption.
     */
    public static byte[] encrypt(String input, byte[] keys) throws Exception {
        SecretKey desKey = new SecretKeySpec(keys, "DES");

        Cipher desCipher;
        // Create the cipher
        // desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        desCipher = Cipher.getInstance("DES");
        desCipher.init(Cipher.ENCRYPT_MODE, desKey);
        byte[] encryptedBytes = desCipher.doFinal(input.getBytes("UTF-8"));
        return encryptedBytes;
    }

    /**
     * metfone key - 1B5A57676A56676E. smart PIN - AAAA009DF888CCCC.
     */
    public static void main(String[] args) throws Exception {
        String[] inputs = new String[] { "V-1-PIN", "V-2-PIN", "V-3-PIN", "V-4-PIN", "V-101-PIN", "V-102-PIN" };
        for (String i : inputs) {
            System.out.println(i + " : "
                    + new String(Base64Coder.encode(encrypt(i, Base64Coder.decode("KmvjzpgmPlI=")))));
        }

        System.out.println("U+HBKjFw5pM= : "
                + new String(decrypt(Base64Coder.decode("U+HBKjFw5pM="), Base64Coder.decode("KmvjzpgmPlI="))));
    }
}
