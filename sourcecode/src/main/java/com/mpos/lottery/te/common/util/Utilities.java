/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mpos.lottery.te.common.util;

import java.util.UUID;

//import java.io.UnsupportedEncodingException;

//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 * 
 * @author Eric
 */
public class Utilities {

    /**
     * Convert a HEX string to byte array
     * 
     * @param strContent
     *            The HEX string to be converted
     * @return byte[] The byte array.
     */
    public static byte[] ConvertStringToBytes(String strContent) {
        int nLen = strContent.length();
        int nLastChar = (nLen % 2 == 0) ? nLen : nLen - 1;
        int nPos = 0;
        // byte bt;
        byte[] btOut = new byte[(nLen % 2 == 0) ? nLen / 2 : (nLen + 1) / 2];// new byte[512];
        String strTemp = "";

        for (int i = 0; i < nLastChar; i += 2) {
            strTemp = strContent.substring(i, i + 2);
            btOut[nPos++] = (byte) Integer.parseInt(strTemp, 16);
        }

        if (nLastChar < nLen) {
            strTemp = strContent.substring(nLen - 1, nLen) + "0";
            btOut[nPos++] = (byte) Integer.parseInt(strTemp, 16);
        }
        return btOut;
    }

    public static char Hex2Char(String strHex2bit) {
        int iTmp = 0;
        char ar;

        iTmp = Integer.parseInt(strHex2bit, 16);
        ar = (char) iTmp;
        // System.out.println(iTmp);
        // System.out.println(ar);
        return ar;
    }

    public static String HexStr2AscStr(String strHex) {

        int iLen, i;
        char ar;
        String strTmp = new String("");
        String strResult = new String("");

        iLen = strHex.length();
        if ((iLen - (iLen / 2) * 2) == 1) {
            strHex = "0" + strHex;
        }
        for (i = 0; i < iLen; i = i + 2) {
            strTmp = strTmp + strHex.substring(i, i + 1);
            strTmp = strTmp + strHex.substring(i + 1, i + 2);
            ar = Hex2Char(strTmp);
            strResult = strResult + String.valueOf(ar);
            strTmp = "";
        }
        return strResult;
    }

    public static String Str2HexStr(String strStr) {
        if (strStr == null) {
            return "";
        }
        StringBuffer strbLine = new StringBuffer("");
        char[] cLine = strStr.toCharArray();
        int i;
        String strLine = new String("");

        for (i = 0; i < cLine.length; i++) {
            strLine = Integer.toHexString((int) cLine[i]);
            if (strLine.length() == 1) {
                strbLine.append("0");
                strbLine.append(strLine);
            } else {
                strbLine.append(strLine);
            }
        }
        // System.out.println(strbLine.toString());

        return strbLine.toString();
    }

    public static String Str2HexStr(String strStr, int final_lenght) {
        if (strStr == null) {
            return "";
        }
        StringBuffer strbLine = new StringBuffer("");
        char[] cLine = strStr.toCharArray();
        int i;
        String strLine = new String("");

        for (i = 0; i < cLine.length; i++) {
            strLine = Integer.toHexString((int) cLine[i]);
            if (strLine.length() == 1) {
                strbLine.append("0");
                strbLine.append(strLine);
            } else {
                strbLine.append(strLine);
            }
        }

        String tmp = strbLine.toString();
        while (tmp.length() < final_lenght) {
            tmp = tmp + "00";
        }
        return tmp;
    }

    /**
     * To pad zero in front of a string until the string length equal to returnStrLength
     * 
     * @param returnStrLength
     *            The length of the processed string
     * @param numberStr
     *            The input string
     * @return String
     */
    public static String AddZeroToNumberStr(int returnStrLength, String numberStr) {
        int numberStrLengrh = numberStr.length();
        for (int i = numberStrLengrh; i < returnStrLength; i++) {
            numberStr = '0' + numberStr;
        }
        return numberStr;
    }

    /**
     * Dump the byte array into a HEX string
     * 
     * @param bs
     *            The byte array to dumped
     * @return String The HEX string of the input byte array
     */
    public static String DumpBytes(byte[] bs) {
        StringBuffer ret = new StringBuffer(bs.length);
        for (int i = 0; i < bs.length; i++) {
            String hex = Integer.toHexString(0x0100 + (bs[i] & 0x00FF)).substring(1);
            ret.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return ret.toString().toUpperCase();
    }

    /**
     * Convert a byte arrray to base64 string
     * 
     * @param bs
     *            The byte array to be converted
     * @return String The converted Base64 string
     */
    public static String Byte2Base64String(byte[] bs) {
        return new String(Base64Coder.encode(bs));
    }

    /**
     * Convert the base64 string to a byte array
     * 
     * @param str
     *            The string to be converted
     * @return byte[]
     * 
     */
    public static byte[] Base64String2Byte(String str) {
        return Base64Coder.decode(str);
    }

    /**
     * Generate a 32-length uuid based on UUID.
     */
    public static String uuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "");
    }

    // public static void main(String[] args) throws Exception{
    // try {
    // System.out.println(Utilities.Byte2Base64String("პაროლი არასწორია".getBytes("UTF-16BE")));
    // System.out.println(Utilities.Byte2Base64String("პაროლი არასწორია".getBytes("UTF-16LE")));
    // System.out.println(Utilities.DumpBytes("პაროლი არასწორია".getBytes("UTF-16LE")));
    // System.out.println(Utilities.DumpBytes(Utilities.Base64String2Byte("4hDgENAQ3BDWENAQ5RDqENgQ0BA=")));
    // } catch (Exception ex) {
    // System.out.println(ex.fillInStackTrace().toString());
    // }
    // }
}
