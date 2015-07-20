package com.mpos.lottery.te.common.encrypt;

import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

/**
 * A RSA utility.
 * <p>
 * Key data may be encoded in three general ways:
 * <ol>
 * <li>Binary DER-encoded format. This is sometimes called ASN.1 BER-encoded (there is a subtle difference between BER-
 * and DER-encodings: DER is just a stricter subset of BER). The most compact form. If you try to view the file with a
 * text editor it is full of "funny" characters. The first character in the file is almost always a '0' character
 * (0x30).</li>
 * <li>PEM or base64 format. This is the same data as the DER-encoded file but it is encoded in base64 with additional
 * header and footer lines: -----BEGIN FOO BAR KEY----- MIIBgjAcBgoqhkiG9w0BDAEDMA4ECKZesfWLQOiDAgID6ASCAWBu7izm8N4V
 * 2puRO/Mdt+Y8ceywxiC0cE57nrbmvaTSvBwTg9b/xyd8YC6QK7lrhC9Njgp/ ... -----END FOO BAR KEY----- These files can be viewed
 * with a text editor and can be easily transmitted as part of an email message.</li>
 * 
 * <li>XML format. There are W3C standards for this, and, er, a .NET way that predates the latest W3C standard. Here is
 * an example of the W3C [XKMS] 2.0 format.
 * 
 * <pre>
 * &lt;RSAKeyPair&gt;
 *   &lt;Modulus&gt;4IlzOY3Y9fXoh3Y5f06wBbtTg94Pt6vcfcd1KQ0FLm0S36aGJtTSb6pYKfyX7PqCUQ8wgL6xUJ5GRPEsu9
 *     gyz8ZobwfZsGCsvu40CWoT9fcFBZPfXro1Vtlh/xl/yYHm+Gzqh0Bw76xtLHSfLfpVOrmZdwKmSFKMTvNXOFd0V18=
 *   &lt;/Modulus&gt;
 *   &lt;Exponent&gt;AQAB&lt;/Exponent&gt;
 *   &lt;P&gt;9tbgIiFMXwpw/yf85bNQap3lD7WFlsZA+qgKtJubDFXCAR35N4KKFMjykw6SzaVmIbk80ga/tFUxydytypgt0Q==&lt;/P&gt;
 *   &lt;Q&gt;6N6wESUJ0gJRAd6K6JhQ9Xd3YaRFk2sIVZZzXfTIWxKTInOLf9Nwf/Wkqrt0/Twiato4kSqGW2wU6K5MnvqOLw==&lt;/Q&gt;
 *   &lt;DP&gt;l0zwh5sXf+4bgxsUtgtqkF+GJ1Hht6B/9eSI41m5+R6b0yl3OCJI1yKxJZi6PVlTt/oeILLIURYjdZNR56vN8Q==&lt;/DP&gt;
 *   &lt;DQ&gt;LPAkW/qgzYUi6tBuT/pszSHTyOTxhERIZHPXKY9+RozsFd7kUbOU5yyZLVVleyTqo2IfPmxNZ0ERO+G+6YMCgw==&lt;/DQ&gt;
 *   &lt;InverseQ&gt;
 *     WIjZoVA4hGqrA7y730v0nG+4tCol+/bkBS9u4oiJIW9LJZ7Qq1CTyr9AcewhJcV/+wLpIZa4M83ixpXub41fKA==
 *   &lt;/InverseQ&gt;
 *   &lt;D&gt;pAPDJ0d2NDRspoa1eUkBSy6K0shissfXSAlqi5H3NvJ11ujNFZBgJzFHNWRNlc1nY860n1asLzduHO4Ovygt9DmQb
 *     zTYbghb1WVq2EHzE9ctOV7+M8v/KeQDCz0Foo+38Y6idjeweVfTLyvehwYifQRmXskbr4saw+yRRKt/IQ==
 *   &lt;/D&gt;
 * &lt;/RSAKeyPair&gt;
 * </pre>
 * 
 * </li>
 * </ol>
 * <p>
 * The white space should not matter, at least for our functions. The .NET version uses <RsaKeyValue> instead, which is
 * strictly only for a public key.
 * <p>
 * Refer to:
 * <ul>
 * <li>http://www.cryptosys.net/pki/rsakeyformats.html</li>
 * <li>
 * http://stackoverflow.com/questions/3243018/how-to-load-rsa-private-key-from -file</li>
 * </ul>
 */
public class RsaCipher {
    private static final String TOKEN_MOD_BEGIN = "<Modulus>";
    private static final String TOKEN_MOD_END = "</Modulus>";
    private static final String TOKEN_EXP_BEGIN = "<Exponent>";
    private static final String TOKEN_EXP_END = "</Exponent>";
    private static final String ALG_NAME = "RSA";
    private static final String PROVIDER = "SunRsaSign"; // refer to
                                                         // $jre_home/lib/security/java.security
    // private static final String ENCODING = "UTF-8";
    private static final String ENCODING = "ISO-8859-1";
    // transformation: algorithm/mode/padding
    private static final String ALG_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    // private static SecureRandom randomSeed = new SecureRandom(new byte[1]);
    private static Log logger = LogFactory.getLog(RsaCipher.class);

    /**
     * Generate RSA key pair, refer to http://stackoverflow.com/questions/3171481/publickey-vs-rsapublickeyspec
     * 
     * @return The public/private KeySpec array. The first element is <code>RSAPublicKeySpec</code>, the second is
     *         </code>RSAPrivateKeySpec</code>.
     * @throws Exception
     *             when encounter any exceptions.
     */
    public static KeySpec[] generateKeyPair(int keySize) throws Exception {
        // Get the public/private key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALG_NAME);
        keyGen.initialize(keySize);
        KeyPair keyPair = keyGen.genKeyPair();

        // For public keys, it doesn't make much difference with KeySpec. For
        // private keys, getEncoded() returns much more information than the
        // private key.
        // PrivateKey privateKey = keyPair.getPrivate();
        // PublicKey publicKey = keyPair.getPublic();

        KeyFactory fact = KeyFactory.getInstance(ALG_NAME);
        RSAPublicKeySpec pub = fact.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
        RSAPrivateKeySpec priv = fact.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);

        return new KeySpec[] { pub, priv };
    }

    /**
     * This method will encode modulus and publicExponent by Base64, and return them in XML representation like below:
     * <p>
     * <RSAKeyValue><Modulus>u4hzNOuqRWaJVvH6C+w/h8/IzqtPaAZE1+2+0Aktj/
     * UL0bm9PiuYZH7VZCrGsQCS89ZOuV96tHXPCWS6jr0blRWkkZqt1CMY +4AjZevXM6VXK2ZohrtbSZ95n6XQZu2Zwflq
     * +qqZTcyCbLUrkT61eSIHVIgdoU/aKGpMH+PeKU =</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>
     * 
     * @param keySize
     *            The size of RSA key.
     * @return a String array, the first one is XML representation of public key, and the second is XML representation
     *         of private key.
     * @throws Exception
     *             when encounter any exceptions.
     */
    public static String[] generateXmlKeyPair(int keySize) throws Exception {
        KeySpec[] keySpecs = generateKeyPair(keySize);
        RSAPublicKeySpec pub = (RSAPublicKeySpec) keySpecs[0];
        String keys[] = new String[2];
        keys[0] = assembleXMLKeyPair(pub.getModulus(), pub.getPublicExponent());
        if (logger.isDebugEnabled()) {
            logger.debug("Generate RSA public key:" + keys[0]);
        }
        RSAPrivateKeySpec priv = (RSAPrivateKeySpec) keySpecs[1];
        keys[1] = assembleXMLKeyPair(priv.getModulus(), priv.getPrivateExponent());
        if (logger.isDebugEnabled()) {
            logger.debug("Generate RSA private key:" + keys[1]);
        }
        return keys;
    }

    private static String assembleXMLKeyPair(BigInteger modulus, BigInteger publicExponent) {
        StringBuffer buffer = new StringBuffer("<RSAKeyValue>");
        buffer.append(TOKEN_MOD_BEGIN).append(new String(Base64Coder.encode(modulus.toByteArray())));
        buffer.append(TOKEN_MOD_END).append(TOKEN_EXP_BEGIN);
        buffer.append(new String(Base64Coder.encode(publicExponent.toByteArray())));
        buffer.append(TOKEN_EXP_END).append("</RSAKeyValue>");
        return buffer.toString();
    }

    /**
     * Encrypt raw input string by public key.
     * 
     * @param publicKeyFile
     *            The file path of public key.
     * @param input
     *            The raw input string.
     * @return a base64 encoded output string.
     */
    public static String encrypt(String publicKeyFile, String input) {
        PublicKey publicKey = (PublicKey) getKeyFromFile(new File(publicKeyFile), true);
        byte[] inputByte = input.getBytes(Charset.forName(ENCODING));
        byte[] outputByte = encrypt(publicKey, inputByte);
        String encryption = new String(Base64Coder.encode(outputByte));
        // if (logger.isDebugEnabled()){
        // logger.debug("Input:" + input + ",Encrytpion:" + encryption);
        // }
        return encryption;
    }

    /**
     * Encrypt by public key
     * 
     * @param xmlKey
     *            The XMl formatted public key
     * @param input
     *            The bytes to be encrypted.
     * @return The encrypted bytes.
     */
    public static byte[] encrypt(String xmlKey, byte[] input) {
        PublicKey publicKey = (PublicKey) getKeyFromXml(xmlKey, true);
        return encrypt(publicKey, input);
    }

    public static byte[] encrypt(Key publicKey, byte input[]) {
        try {
            // get an RSA cipher object and print the
            // provider..."algorithm/mode/padding"
            Cipher cipher = Cipher.getInstance(ALG_TRANSFORMATION);

            SecureRandom randomSeed = new SecureRandom(new byte[] { 1 });
            // encrypt the plain-text using the public key and a specified
            // random seed. If you don't
            // specify a fixed random seed, the same input and same public key
            // will generate different
            // output each time when you do encryption. Besides the SecureRandom
            // must not be a instance
            // variable, looks like the 'init' method will manipulate the random
            // seed. If the seed
            // is a instance variable, different seed will be passed to 'init'
            // method each time.
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, randomSeed);
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * Decrypt a cipher back into original data.
     * 
     * @param privateKeyPath
     *            The file path of private key.
     * @param base64Input
     *            The base64 encoded cipher.
     * @return the original raw text.
     */
    public static String decrypt(String privateKeyPath, String base64Input) {
        PrivateKey privateKey = (PrivateKey) getKeyFromFile(new File(privateKeyPath), false);
        byte[] input = Base64Coder.decode(base64Input);
        byte[] output = decrypt(privateKey, input);
        return new String(output);
    }

    /**
     * Decrypt by private key
     * 
     * @param xmlKey
     *            The XMl formatted private key
     * @param cipher
     *            The encrypted bytes.
     * @return The raw bytes.
     */
    public static byte[] decrypt(String xmlKey, byte[] cipher) {
        PrivateKey privateKey = (PrivateKey) getKeyFromXml(xmlKey, false);
        return decrypt(privateKey, cipher);
    }

    public static byte[] decrypt(Key privateKey, byte input[]) {
        try {
            Cipher cipher = Cipher.getInstance(ALG_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * Retrieve public/private key from file, which is a XML file.
     */
    private static Key getKeyFromFile(File keyFilePath, boolean isPublic) {
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(keyFilePath)));
            for (String line = br.readLine(); line != null;) {
                buffer.append(line);
                line = br.readLine();
            }
            br.close();
            return getKeyFromXml(buffer.toString(), isPublic);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * Retrieve public/private key from file, which is XML formatted.
     */
    private static Key getKeyFromXml(String xmlKeyString, boolean isPublic) {
        try {
            String keyString = xmlKeyString;
            String base64Mod = getKeyComponent(keyString, true);
            String base64Exp = getKeyComponent(keyString, false);
            // if (logger.isDebugEnabled()){
            // logger.debug("MOD:" + base64Mod + ",EXP:" + base64Exp);
            // }
            // convert base64 into BigInteger
            BigInteger mod = new BigInteger(Base64Coder.decode(base64Mod));
            if (mod.compareTo(new BigInteger("0")) < 0) {
                /**
                 * javax.crypto.BadPaddingException: Message is larger than modulus ... You just need to add a check for
                 * a negative modulus coming from the card. In RSA encryption, you use large integers for the
                 * calculation. Even the message is converted to a large integer. For the equation to work, the message
                 * has to be smaller than the modulus. Luckily the solution is simple. If the first bit of your modulus
                 * from the card is set, add a 0x00 byte to front before setting the modulus in your key spec instance.
                 * Refer to http://forums.oracle.com/forums/thread.jspa?threadID=1749350
                 */
                mod = mod.multiply(new BigInteger("-1"));
            }
            BigInteger exp = new BigInteger(Base64Coder.decode(base64Exp));

            Key key = null;
            KeyFactory fac = KeyFactory.getInstance(ALG_NAME, PROVIDER);
            // if (logger.isDebugEnabled()){
            // logger.debug("The provider for " + ALG_NAME + ":" +
            // fac.getProvider().getName());
            // // showProvider(fac.getProvider());
            // }
            if (isPublic) {
                RSAPublicKeySpec pub = new RSAPublicKeySpec(mod, exp);
                key = fac.generatePublic(pub);
            } else {
                RSAPrivateKeySpec pri = new RSAPrivateKeySpec(mod, exp);
                key = fac.generatePrivate(pri);
            }
            return key;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * Read private/public key from a .pem file which often generated from openssl. In general, there are three things
     * to construct a <code>Key</code> .
     * <ol>
     * <li>Filter those comment lines, for example '-----BEGIN RSA PRIVATE KEY-----' in a non-encrypted .pem file.</li>
     * <li>Base64 decode the content.</li>
     * <li>The openssl private key format is non-standard and is not compatible with <code>PKCS8EncodedKeySpec.</code></li>
     * </ol>
     * NOTE: refer to http://stackoverflow.com/questions/3243018/how-to-load-rsa- private-key-from-file.
     * <p>
     * As the .pem file generate by openssl is not compatible with <code>PKCS8EncodedKeySpec.</code>, You need to
     * convert your private key to PKCS8 format using following command:
     * <p>
     * <code>openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key_file  -nocrypt > pkcs8_key</code>
     * <p>
     * This command will convert .pem into a Binary DER-encoded format(refer to
     * http://www.cryptosys.net/pki/rsakeyformats.html).
     * <p>
     * Also if you wanna handle with .pem, there is a convenient alternative: <a
     * href="http://www.bouncycastle.org/">bouncycastle</a>
     * 
     * @param pemKeyFile
     *            The path of .pem file.
     * @param isPublic
     *            Is this a public key file?
     * @return a RSA key, public or private
     */
    public static Key getKeyFromPKCS8(String pemKeyFile, boolean isPublic) {
        Key key = null;
        // read key file
        BufferedInputStream bis = null;
        try {
            File keyFile = new File(pemKeyFile);
            bis = new BufferedInputStream(new FileInputStream(keyFile));
            byte[] keyBytes = new byte[(int) keyFile.length()];
            bis.read(keyBytes);
            bis.close();

            // System.out.println(Base64Coder.encode(keyBytes));
            // StringBuffer buffer = new StringBuffer();
            // BufferedReader br = new BufferedReader(new InputStreamReader(new
            // FileInputStream(
            // pemKeyFile)));
            // for (String line = br.readLine(); line != null;) {
            // buffer.append(line);
            // line = br.readLine();
            // }
            // br.close();
            // // remove comments
            // String pemStr = buffer.toString();
            // String keyPEM = pemStr.replace("-----BEGIN RSA PRIVATE KEY-----",
            // "");
            // keyPEM = keyPEM.replace("-----END RSA PRIVATE KEY-----", "");
            // byte[] keyBytes = Base64Coder.decode(keyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec ks = new PKCS8EncodedKeySpec(keyBytes);
            if (isPublic) {
                key = keyFactory.generatePublic(ks);
            } else {
                key = (RSAPrivateKey) keyFactory.generatePrivate(ks);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return key;
    }

    private static String getKeyComponent(String keyString, boolean isModulus) {
        int indexBegin = -1;
        int indexEnd = -1;
        if (isModulus) {
            indexBegin = keyString.indexOf(TOKEN_MOD_BEGIN) + TOKEN_MOD_BEGIN.length();
            indexEnd = keyString.indexOf(TOKEN_MOD_END);
        } else {
            indexBegin = keyString.indexOf(TOKEN_EXP_BEGIN) + TOKEN_EXP_BEGIN.length();
            indexEnd = keyString.indexOf(TOKEN_EXP_END);
        }
        if (indexBegin == -1 || indexEnd == -1) {
            throw new SystemException("Wrong key format:" + keyString);
        }
        return keyString.substring(indexBegin, indexEnd).trim();
    }

    @SuppressWarnings("unused")
    private static void showProvider(Provider provider) {
        if (logger.isDebugEnabled()) {
            logger.debug("\tProvider: " + provider.getName());
            for (java.util.Iterator itr = provider.keySet().iterator(); itr.hasNext();) {
                String key = (String) itr.next();
                String value = (String) provider.get(key);
                logger.debug("\t" + key + " = " + value);
            }
        }
    }
}
