package com.mpos.lottery.te.common.encrypt;

import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.common.util.HexCoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * DESede/3DES/TriperDES. There are at least five common conventions of padding:
 * <ul>
 * <li>1. Pad with bytes all of the same value as the number of padding bytes</li>
 * <li>2. Pad with 0x80 followed by zero bytes</li>
 * <li>3. Pad with zeroes except make the last byte equal to the number of padding bytes</li>
 * <li>4. Pad with zero (null) characters</li>
 * <li>5. Pad with space characters Method one is the method described in PKCS#5, PKCS#7 and RFC 3852 Section 6.3
 * (formerly RFC 3369 and RFC 2630). It is the most commonly used. Refer to: http://www.di-mgt.com.au/cryptopad.html</li>
 * </ul>
 */
public class TriperDESCipher {
    protected static Log logger = LogFactory.getLog(TriperDESCipher.class);
    // define the cipher algorithm
    private static final String algorithm = "DESede";
    private static final String transformation = "DESede/CBC/PKCS5Padding";
    public static final byte[] IV = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    public static final String STR_IV = "0000000000000000";

    /**
     * Generate a 24-bytes data key, it is encoded by 'Base64'.
     * 
     * @return a base64 representation of data key.
     */
    public static String generateDataKey() throws Exception {
        int keySize = 24;
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(random(25).getBytes());
        String tmpPass = md.digest().toString();
        String key = tmpPass + random(keySize - tmpPass.length());

        String dataKey = new String(Base64Coder.encode(key.getBytes()));
        if (logger.isDebugEnabled()) {
            logger.debug("Generated Data Key(base64): " + dataKey);
        }
        return dataKey;
    }

    /**
     * Encryption...
     * 
     * @param base64Key
     *            The base64 representation of secret key
     * @param input
     *            The original input string.
     * @param hexIv
     *            The hex representation of IV.
     * @return a base64 representation of encrypted output.
     * @throws Exception
     *             when encounter any exception.
     */
    public static String encrypt(String base64Key, String input, String hexIv) throws Exception {
        byte[] keyBytes = Base64Coder.decode(base64Key);
        // use default
        // Charset.getDefaultEncoding()
        byte[] inputs = input.getBytes();
        byte[] ivBytes = HexCoder.hexToBuffer(hexIv);
        byte[] output = encrypt(keyBytes, inputs, ivBytes);
        return new String(Base64Coder.encode(output));
    }

    public static byte[] encrypt(byte[] keyBytes, byte[] src, byte[] ivBytes) throws Exception {
        SecretKey deskey = new SecretKeySpec(keyBytes, algorithm);
        AlgorithmParameterSpec iv = new IvParameterSpec(ivBytes);

        // do encryption
        Cipher c1 = Cipher.getInstance(transformation);
        c1.init(Cipher.ENCRYPT_MODE, deskey, iv, new SecureRandom());
        return c1.doFinal(src);
    }

    /**
     * Decryption...
     * 
     * @param base64Key
     *            The base64 representation of secret key.
     * @param cipher
     *            The base64 representation of encrypted output.
     * @param hexIv
     *            The hex representation of IV.
     * @return original input string.
     */
    public static String decrypt(String base64Key, String cipher, String hexIv) throws Exception {
        byte[] keyBytes = Base64Coder.decode(base64Key);
        byte[] ciphers = Base64Coder.decode(cipher);
        byte[] ivBytes = HexCoder.hexToBuffer(hexIv);
        return new String(decrypt(keyBytes, ciphers, ivBytes));
    }

    public static byte[] decrypt(byte[] keyBytes, byte[] cipher, byte[] ivBytes) throws Exception {
        SecretKey deskey = new SecretKeySpec(keyBytes, algorithm);
        // length: 8 bytes
        AlgorithmParameterSpec iv = new IvParameterSpec(ivBytes);

        // do decryption
        Cipher c1 = Cipher.getInstance(transformation);
        c1.init(Cipher.DECRYPT_MODE, deskey, iv, new SecureRandom());
        return c1.doFinal(cipher);
    }

    private static String random(int length) {
        UUID uuid = UUID.randomUUID();
        String myRandom = uuid.toString();
        return myRandom.substring(0, length);
    }
}
