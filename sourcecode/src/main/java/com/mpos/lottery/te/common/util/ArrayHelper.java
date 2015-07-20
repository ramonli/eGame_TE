package com.mpos.lottery.te.common.util;

import java.lang.reflect.Array;

public class ArrayHelper {
    /**
     * Concatenate 2 arrays into 1.
     */
    public static <T> T[] concatenate(T[] a, T... b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, C, 0, aLen);
        System.arraycopy(b, 0, C, aLen, bLen);

        return C;
    }

}
