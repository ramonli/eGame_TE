package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.charset.Charset;

public class Base64CoderUnitTest {
    private static String input = "abc";

    @Test
    public void testConversion() {
        System.out.println("default charset:" + Charset.defaultCharset().displayName());

        String base64Output = Base64Coder.encodeString(input);
        System.out.println(base64Output);
        String output = Base64Coder.decodeString(base64Output);

        assertEquals(input, output);

        System.out.println(20 / 20);
        System.out.println(21 / 20);
        System.out.println(60 / 20);
        System.out.println(63 / 20);
    }
}
