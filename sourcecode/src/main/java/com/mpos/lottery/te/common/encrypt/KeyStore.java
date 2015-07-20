package com.mpos.lottery.te.common.encrypt;

import com.mpos.lottery.te.common.util.Base64Coder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class KeyStore {
    public static final String DES_FILE = "DES.key";
    public static final String RSA_PUBLIC_FILE = "RSA_public.key";
    public static final String RSA_PRIVATE_FILE = "RSA_private.key";

    /**
     * Encrypt a DES key by RSA private key, and write it to a file.
     */
    public static String writeKey(File keyDir) throws Exception {
        // Generate RSA key
        String[] rsa = RsaCipher.generateXmlKeyPair(1024);
        // Generate DES key
        String base64Key = TriperDESCipher.generateDataKey();
        // Encrypt DES key
        byte[] output = RsaCipher.encrypt(rsa[1], Base64Coder.decode(base64Key));
        // Write the DES key(protected by RSA) to the file
        FileOutputStream out = new FileOutputStream(new File(keyDir, DES_FILE));
        out.write(output);
        out.close();
        // Write RSA public key
        PrintStream ps = new PrintStream(new FileOutputStream(new File(keyDir, RSA_PUBLIC_FILE)));
        ps.println(rsa[0]);
        ps.close();

        ps = new PrintStream(new FileOutputStream(new File(keyDir, RSA_PRIVATE_FILE)));
        ps.println(rsa[1]);
        ps.close();

        return base64Key;
    }

    /**
     * Read data from file, and decrypt it by RSA public key to retrieve DES key.
     */
    public static byte[] readKey(String xmlPublicKey, File desKeyFile) throws Exception {
        // Read the raw bytes from the keyfile
        DataInputStream in = new DataInputStream(new FileInputStream(desKeyFile));
        byte[] keys = new byte[(int) desKeyFile.length()];
        in.readFully(keys);
        in.close();

        return RsaCipher.decrypt(xmlPublicKey, keys);
    }

    /**
     * Decrypt DES key from a file which is encrypted by RSA private key.
     */
    public static byte[] readKey(File publicKeyFile, File desKeyFile) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(publicKeyFile)));
        StringBuffer buffer = new StringBuffer();
        for (String tmp = br.readLine(); tmp != null;) {
            buffer.append(tmp);
            tmp = br.readLine();
        }
        String xmlPublicKey = buffer.toString();
        return readKey(xmlPublicKey, desKeyFile);
    }

    public static void main(String args[]) throws Exception {
        // generate key files
        // File dir = new File("./SERIAL_KEYS/");
        // if (!dir.exists()) {
        // dir.mkdir();
        // }
        // System.out.println("** Start to generate keys...");
        // KeyStore.writeKey(dir);
        // System.out.println("** Done! Find key files in directory '" + dir.getName() + "'.");
        String serialNo = "uS+9yO66OaWPRIwvVuuTZ5ud1eIcPfrY";
        byte[] desKey = KeyStore.readKey(new File("F:/tmp/SriLanka_Key/RSA_public.key"), new File(
                "F:/tmp/SriLanka_Key/DES.key"));
        String raw = new String(TriperDESCipher.decrypt(desKey, Base64Coder.decode(serialNo), TriperDESCipher.IV));
        System.out.println(serialNo + " : " + raw);
    }
}
