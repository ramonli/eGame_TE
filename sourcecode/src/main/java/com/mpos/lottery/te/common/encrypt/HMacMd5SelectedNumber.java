package com.mpos.lottery.te.common.encrypt;

import com.mpos.lottery.te.config.exception.SystemException;

public class HMacMd5SelectedNumber {
    private static final String SELECTED_NUMBER_KEY = "KpWkPzlFpROUr7JGf3x8/hHXuTNC4e7zDRks3R7fGN8qM9Dg6AL2i9PSB486nAlkDTwUBaDYKqlpPOATQhcHSg==";

    public static String doDigestBySelectedNumbers(String selectedNumbers) {
        try {
            return HMacMd5Cipher.doDigest(selectedNumbers, SELECTED_NUMBER_KEY);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(doDigestBySelectedNumbers("1,2,3,4,5"));
    }
}
