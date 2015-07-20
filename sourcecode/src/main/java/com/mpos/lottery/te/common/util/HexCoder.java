package com.mpos.lottery.te.common.util;

/**
 * A utility to transfer byte[] into hex string, or transfer hex string into byte[].
 */
public class HexCoder {
    private static final char kHexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    // /**
    // * Translate byte[] into hex string.
    // */
    // public static String toHexString(byte[] in) {
    // BigInteger temp = new BigInteger(in);
    // return temp.toString(16);
    // }
    //
    // /**
    // * Translate hex string into byte[].
    // */
    // public static byte[] fromHexString(String in) {
    // BigInteger temp = new BigInteger(in, 16);
    // return temp.toByteArray();
    // }

    public static String stringToHex(String s) {
        byte[] stringBytes = s.getBytes();
        return HexCoder.bufferToHex(stringBytes);
    }

    public static String bufferToHex(byte buffer[]) {
        return HexCoder.bufferToHex(buffer, 0, buffer.length);
    }

    public static String bufferToHex(byte buffer[], int startOffset, int length) {
        StringBuffer hexString = new StringBuffer(2 * length);
        int endOffset = startOffset + length;
        for (int i = startOffset; i < endOffset; i++)
            HexCoder.appendHexPair(buffer[i], hexString);
        return hexString.toString();
    }

    public static String hexToString(String hexString) throws NumberFormatException {
        byte[] bytes = HexCoder.hexToBuffer(hexString);
        return new String(bytes);
    }

    public static byte[] hexToBuffer(String hexString) throws NumberFormatException {
        int length = hexString.length();
        byte[] buffer = new byte[(length + 1) / 2];
        boolean evenByte = true;
        byte nextByte = 0;
        int bufferOffset = 0;

        if ((length % 2) != 0)
            evenByte = false;
        for (int i = 0; i < length; i++) {
            char c = hexString.charAt(i);
            int nibble;
            if ((c >= '0') && (c <= '9'))
                nibble = c - '0';
            else if ((c >= 'A') && (c <= 'F'))
                nibble = c - 'A' + 0x0A;
            else if ((c >= 'a') && (c <= 'f'))
                nibble = c - 'a' + 0x0A;
            else
                throw new NumberFormatException("Invalid hex digit '" + c + "'.");

            if (evenByte) {
                nextByte = (byte) (nibble << 4);
            } else {
                nextByte += (byte) nibble;
                buffer[bufferOffset++] = nextByte;
            }
            evenByte = !evenByte;
        }
        return buffer;
    }

    private static void appendHexPair(byte b, StringBuffer hexString) {
        char highNibble = kHexChars[(b & 0xF0) >> 4];
        char lowNibble = kHexChars[b & 0x0F];
        hexString.append(highNibble);
        hexString.append(lowNibble);
    }

}
