package com.mpos.lottery.te.common.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * SecurityMeasurements
 */
public class SecurityMeasurements {

    private final static IvParameterSpec IvParameters = new IvParameterSpec(new byte[] { Byte.parseByte("00", 16),
            Byte.parseByte("00", 16), Byte.parseByte("00", 16), Byte.parseByte("00", 16), Byte.parseByte("00", 16),
            Byte.parseByte("00", 16), Byte.parseByte("00", 16), Byte.parseByte("00", 16) });

    public static byte[] RSAEncryptWithPublicKeyToByteArray(byte[] strPlainText, Key pPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pPublicKey);
        return cipher.doFinal(strPlainText);
    }

    public static byte[] RSAEncryptWithPublicKeyToByteArray(byte[] strPlainText, String strPublicKeyBase64)
            throws Exception {
        return RSAEncryptWithPublicKeyToByteArray(strPlainText, GetRSAKeyFromBase64String(strPublicKeyBase64, true));
    }

    public static byte[] RSAEncryptWithPublicKeyToByteArray(byte[] strPlainText, String strModulus, String strExponent)
            throws Exception {
        return RSAEncryptWithPublicKeyToByteArray(strPlainText,
                GetRSAKeyFromModulusExponentFormat(strModulus, strExponent, true));
    }

    /**
     * Encrypt a byte array with RSA public key in base64 format
     */
    public static String RSAEncryptWithPublicKeyToBase64String(byte[] strPlainText, String strPublicKeyBase64)
            throws Exception {
        return Utilities.Byte2Base64String(RSAEncryptWithPublicKeyToByteArray(strPlainText, strPublicKeyBase64));
    }

    public static String RSAEncryptWithPublicKeyToBase64String(byte[] strPlainText, String strModulus,
            String strExponent) throws Exception {
        return Utilities.Byte2Base64String(RSAEncryptWithPublicKeyToByteArray(strPlainText, strModulus, strExponent));
    }

    public static byte[] RSADecryptWithPublicKeyToByteArray(byte[] strEncryptedText, Key pPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // System.out.println("nProvider is: " + cipher.getProvider().getInfo());
        cipher.init(Cipher.DECRYPT_MODE, pPublicKey);
        return cipher.doFinal(strEncryptedText);
    }

    public static byte[] RSADecryptWithPublicKeyToByteArray(byte[] strEncryptedText, String strPublicKeyBase64)
            throws Exception {
        return RSADecryptWithPublicKeyToByteArray(strEncryptedText, GetRSAKeyFromBase64String(strPublicKeyBase64, true));
    }

    public static byte[] RSADecryptWithPublicKeyToByteArray(byte[] strPlainText, String strModulus, String strExponent)
            throws Exception {
        return RSADecryptWithPublicKeyToByteArray(strPlainText,
                GetRSAKeyFromModulusExponentFormat(strModulus, strExponent, true));
    }

    /**
     * Decrypt a byte array with RSA public key
     */
    public static String RSADecryptWithPublicKeyToBase64String(byte[] strEncryptedText, String strPublicKeyBase64)
            throws Exception {
        return Utilities.Byte2Base64String(RSADecryptWithPublicKeyToByteArray(strEncryptedText, strPublicKeyBase64));
    }

    public static String RSADecryptWithPublicKeyToBase64String(byte[] strEncryptedText, String strModulus,
            String strExponent) throws Exception {
        return Utilities
                .Byte2Base64String(RSADecryptWithPublicKeyToByteArray(strEncryptedText, strModulus, strExponent));
    }

    public static byte[] RSAEncryptWithPrivateKeyToByteArray(byte[] strPlainText, Key pPrivateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // System.out.println("nProvider is: " + cipher.getProvider().getInfo());
        cipher.init(Cipher.ENCRYPT_MODE, pPrivateKey);
        return cipher.doFinal(strPlainText);
    }

    public static byte[] RSAEncryptWithPrivateKeyToByteArray(byte[] strPlainText, String strPrivateKeyBase64)
            throws Exception {
        return RSAEncryptWithPrivateKeyToByteArray(strPlainText, GetRSAKeyFromBase64String(strPrivateKeyBase64, false));
    }

    public static byte[] RSAEncryptWithPrivateKeyToByteArray(byte[] strPlainText, String strModulus, String strExponent)
            throws Exception {
        return RSAEncryptWithPrivateKeyToByteArray(strPlainText,
                GetRSAKeyFromModulusExponentFormat(strModulus, strExponent, false));
    }

    public static String RSAEncryptWithPrivateKeyToBase64String(byte[] strPlainText, String strPrivateKeyBase64)
            throws Exception {
        return Utilities.Byte2Base64String(RSAEncryptWithPrivateKeyToByteArray(strPlainText, strPrivateKeyBase64));
    }

    public static String RSAEncryptWithPrivateKeyToBase64String(byte[] strPlainText, String strModulus,
            String strExponent) throws Exception {
        return Utilities.Byte2Base64String(RSAEncryptWithPrivateKeyToByteArray(strPlainText, strModulus, strExponent));
    }

    public static byte[] RSADecryptWithPrivateKeyToByteArray(byte[] strEncryptedText, Key pPrivateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        // System.out.println("nProvider is: " + cipher.getProvider().getInfo());
        cipher.init(Cipher.DECRYPT_MODE, pPrivateKey);
        return cipher.doFinal(strEncryptedText);
    }

    public static byte[] RSADecryptWithPrivateKeyToByteArray(byte[] strEncryptedText, String strPrivateKeyBase64)
            throws Exception {
        return RSADecryptWithPrivateKeyToByteArray(strEncryptedText,
                GetRSAKeyFromBase64String(strPrivateKeyBase64, false));
    }

    public static byte[] RSADecryptWithPrivateKeyToByteArray(byte[] strEncryptedText, String strModulus,
            String strExponent) throws Exception {
        return RSADecryptWithPrivateKeyToByteArray(strEncryptedText,
                GetRSAKeyFromModulusExponentFormat(strModulus, strExponent, false));
    }

    public static String RSADecryptWithPrivateKeyToBase64String(byte[] strEncryptedText, String strPrivateKeyBase64)
            throws Exception {
        return Utilities.Byte2Base64String(RSADecryptWithPrivateKeyToByteArray(strEncryptedText, strPrivateKeyBase64));
    }

    public static String RSADecryptWithPrivateKeyToBase64String(byte[] strEncryptedText, String strModulus,
            String strExponent) throws Exception {
        return Utilities.Byte2Base64String(RSADecryptWithPrivateKeyToByteArray(strEncryptedText, strModulus,
                strExponent));
    }

    public static Key GetRSAKeyFromBase64String(String key, boolean bIsPublicKey) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        KeySpec keySpec = null;
        Key pKey = null;
        KeyFactory pKeyFactory = KeyFactory.getInstance("RSA");

        if (bIsPublicKey == true) {
            keySpec = new X509EncodedKeySpec(Utilities.Base64String2Byte(key));
            pKey = pKeyFactory.generatePublic(keySpec);

            // RSAPublicKey secretKey = (RSAPublicKey)pKey;
            // System.out.println("Modulus = " + secretKey.getModulus().toString(16).toUpperCase());
            // System.out.println("Public Exponent = " +
            // secretKey.getPublicExponent().toString(16).toUpperCase());
        } else {
            keySpec = new PKCS8EncodedKeySpec(Utilities.Base64String2Byte(key));
            pKey = pKeyFactory.generatePrivate(keySpec);

            // RSAPrivateKey secretKey = (RSAPrivateKey)pKey;
            // System.out.println("Modulus = " + secretKey.getModulus().toString(16).toUpperCase());
            // System.out.println("Private Exponent = " +
            // secretKey.getPrivateExponent().toString(16).toUpperCase());
        }
        // System.out.println("public format: " + pKey.getFormat());
        // System.out.println("public algorithm: " + pKey.getAlgorithm());
        // System.out.println("public encoded: " + Utilities.DumpBytes(pKey.getEncoded()));
        return pKey;
    }

    public static Key GetRSAKeyFromModulusExponentFormat(String strModulus, String strExponent, boolean bIsPublicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory pKeyFac = KeyFactory.getInstance("RSA");
        Key pKey = null;
        if (bIsPublicKey == true) {
            RSAPublicKeySpec pKeySpec = new RSAPublicKeySpec(new BigInteger(strModulus, 16), new BigInteger(
                    strExponent, 16));
            pKey = (Key) pKeyFac.generatePublic(pKeySpec);
        } else {
            RSAPrivateKeySpec pKeySpec = new RSAPrivateKeySpec(new BigInteger(strModulus, 16), new BigInteger(
                    strExponent, 16));
            pKey = (Key) pKeyFac.generatePrivate(pKeySpec);
        }
        return pKey;
    }

    public static byte[] HMACMD5ToByteArray(byte[] strPlainText, String pKeyBase64) throws Exception {
        SecretKey sk = new SecretKeySpec(Utilities.Base64String2Byte(pKeyBase64), "HmacMD5");
        // System.out.println("Algorithm = " +sk.getAlgorithm());
        // System.out.println("Format = " + sk.getFormat());
        // System.out.println(Utilities.DumpBytes(Utilities.Base64String2Byte(pKeyBase64)));
        // System.out.println(Utilities.DumpBytes(sk.getEncoded()));
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(sk);
        return mac.doFinal(strPlainText);
    }

    /**
     * To get the HMAC-MD5 hex string.
     */
    public static String HMACMD5ToHexString(byte[] strPlainText, String pKeyBase64) throws Exception {
        return Utilities.DumpBytes(HMACMD5ToByteArray(strPlainText, pKeyBase64));
    }

    /**
     * Get the Triple DES of a plain text in form of byte array.
     */
    public static byte[] TripleDESCBCEncryptToByteArray(byte[] strPlainText, String pKeyBase64) throws Exception {
        // DESedeKeySpec keySpec = new DESedeKeySpec(Utilities.Base64String2Byte(pKeyBase64));
        // SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        // SecretKey sk = keyFactory.generateSecret(keySpec);
        SecretKey sk = new SecretKeySpec(Utilities.Base64String2Byte(pKeyBase64), "DESede");
        // System.out.println("Algorithm = " +sk.getAlgorithm());
        // System.out.println("Format = " + sk.getFormat());
        // System.out.println(Utilities.DumpBytes(Utilities.Base64String2Byte(pKeyBase64)));
        // System.out.println(Utilities.DumpBytes(sk.getEncoded()));
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, sk, IvParameters);
        return cipher.doFinal(strPlainText);
    }

    /**
     * Get the Triple DES of a plain text in form of base64 string.
     */
    public static String TripleDESCBCEncryptToBase64String(byte[] strPlainText, String pKeyBase64) throws Exception {
        return Utilities.Byte2Base64String(TripleDESCBCEncryptToByteArray(strPlainText, pKeyBase64));
    }

    /**
     * Get the plain text from a triple des base64 string.
     */
    public static byte[] TripleDESCBCDecryptToByteArray(String pEncryptedText, String pKeyBase64) throws Exception {
        SecretKey sk = new SecretKeySpec(Utilities.Base64String2Byte(pKeyBase64), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        // Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, sk, IvParameters);
        return cipher.doFinal(Utilities.Base64String2Byte(pEncryptedText));
    }

    /**
     * Get the plain text from a triple des base64 string.
     */
    public static String TripleDESCBCDecryptToString(String pEncryptedText, String pKeyBase64) throws Exception {
        return new String(TripleDESCBCDecryptToByteArray(pEncryptedText, pKeyBase64));
    }

    /**
     * Generate a MD5 byte array with input string.
     */
    public static byte[] MD5PlainTextToByteArray(String pPlainText) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(pPlainText.getBytes());
    }

    /**
     * Generate a MD5 hex string with input string.
     */
    public static String MD5PlainTextToHexString(String pPlainText) throws Exception {
        return Utilities.DumpBytes(MD5PlainTextToByteArray(pPlainText));
    }

    public static RSAPublicKey ReadPEM(String strFileName) throws FileNotFoundException, IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        String pKeyString = "";
        // StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(strFileName));
        char[] buf = new char[1024];
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            if (!tmp.startsWith("-----")) {
                pKeyString += tmp;
            }
        }
        reader.close();
        return (RSAPublicKey) GetRSAKeyFromBase64String(pKeyString, true);
    }

}
