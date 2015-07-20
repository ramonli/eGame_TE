package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.charset.Charset;

public class HexCoderUnitTest {

    @Test
    public void testHex() {
        // private static String input = "hello,~!@#$%^&*()_";
        String input = "1";
        System.out.println("default charset:" + Charset.defaultCharset().displayName());
        byte[] b = input.getBytes();
        String hexOutput = HexCoder.bufferToHex(b);
        String output = new String(HexCoder.hexToBuffer(hexOutput));
        System.out.println("Hex representation:" + hexOutput);
        assertEquals(input, output);
    }

    @Test
    public void testHex_1() {
        String input = "157823119021";
        System.out.println("default charset:" + Charset.defaultCharset().displayName());
        byte[] b = input.getBytes();
        String hexOutput = HexCoder.bufferToHex(b);
        String output = new String(HexCoder.hexToBuffer(hexOutput));
        System.out.println("Hex representation:" + hexOutput);
        assertEquals(input, output);

        System.out
                .println(HexCoder
                        .hexToString("6C68495141376B59514447715A5751685A416A476253444751565430716778434C532B633346615938554E4970354C776F356E4A334A622F4851417877753478"));
    }
}
