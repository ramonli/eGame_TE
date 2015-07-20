package com.mpos.lottery.te.common.encrypt;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.config.MLotteryContext;

import org.junit.Test;

public class TriperDesCipherUnitTest {
    private byte[] ivBytes = { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
    private MLotteryContext prop = MLotteryContext.getInstance();

    @Test
    public void testGenerateDataKey() throws Exception {
        String keyString = TriperDESCipher.generateDataKey();
        assertEquals(24, Base64Coder.decode(keyString).length);

        String keyString2 = TriperDESCipher.generateDataKey();
        assertEquals(false, keyString.equals(keyString2));
    }

    @Test
    public void testEncryption() throws Exception {
        // String key = TriperDESCipher.generateDataKey();
        String key = "W0JAMTk4NDZmZGVlYzA4NTFkLTAyNWMt";
        String src = "hello~!@#$%^&*()_+";
        byte[] cipher = TriperDESCipher.encrypt(Base64Coder.decode(key), src.getBytes(), ivBytes);
        byte[] output = TriperDESCipher.decrypt(Base64Coder.decode(key), cipher, ivBytes);
        assertEquals(src, new String(output));

        String encryption = TriperDESCipher.encrypt(key, src, prop.getTriperDesIV());
        // String base64 = Base64Coder.decodeString(encryption);
        String original = TriperDESCipher.decrypt(key, encryption, prop.getTriperDesIV());
        assertEquals(src, original);
    }

    @Test
    public void testDecryption() throws Exception {
        String key = "W0JAZGQyMGI2MTBhOGUyMjQtNTNlMy00";
        String cipher = "xUFopjmNlolMycE/S91QPvVh+0cI7iI4uyWkFqm+iEO5eq9CZyls5jrk6UEe43cTK8PfWYyBTQOsi47qocviLlB2Wy7zWfTJnv/HMM2ciYU=";
        String orignial = TriperDESCipher.decrypt(key, cipher, prop.getTriperDesIV());
        System.out.println(orignial);
    }

    @Test
    public void testDecryption_1() throws Exception {
        String key = "W0JAMWQ1MGZkMjc2N2U2M2Y2LWVkYTIt";
        String cipher = "pq3FqO8HCcAnaobA+xpVnADmrnwj1vrK59BGhqvtskYFWTsa2cbe6ttFDFcpmi1MamgAomcAmY4LSHhACZQncsrmjMbtKx4YGT4+vhRqcOkv3jySVnwlqaGx5rBfd7Qs2uqHrn61ymOnsckJ6nYITMyA3/Ua6amd/PrixpPI5CqYid/Cr2l8wH6c0SXIMMoWPv8NllisK7aaJ66IZFQhHkMGMyqG/8SA";
        String orignial = TriperDESCipher.decrypt(key, cipher, prop.getTriperDesIV());
        System.out.println(orignial);
    }
}
