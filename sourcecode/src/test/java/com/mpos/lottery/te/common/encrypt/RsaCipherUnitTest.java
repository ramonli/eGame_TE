package com.mpos.lottery.te.common.encrypt;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.test.unittest.BaseUnitTest;

import org.junit.Test;

public class RsaCipherUnitTest {

    @Test
    public void testCipher() {
        String input = "02009113000000000008";
        String output = RsaCipher.encrypt(BaseUnitTest.RSA_PUBLIC_KEY, input);
        System.out.println("Length:" + output.length() + ",Output:" + output);
        String original = RsaCipher.decrypt(BaseUnitTest.RSA_PRIVATE_KEY, output);
        assertEquals(input, original);
    }

}
