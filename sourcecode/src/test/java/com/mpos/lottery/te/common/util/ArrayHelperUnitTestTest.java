package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArrayHelperUnitTestTest {

    @Test
    public void testConcatenateArrays() {
        String[] a = new String[] { "1", "2" };
        String[] b = new String[] { "2", "3" };
        String[] c = ArrayHelper.concatenate(a, b);
        assertEquals(4, c.length);
        assertEquals("1", c[0]);
        assertEquals("2", c[1]);
        assertEquals("2", c[2]);
        assertEquals("3", c[3]);

        Integer[] ia = new Integer[] { 1, 2 };
        Integer[] ib = new Integer[] { 2, 3 };
        Integer[] ic = ArrayHelper.concatenate(ia, ib);
        assertEquals(4, ic.length);
        assertEquals(1, ic[0].intValue());
        assertEquals(2, ic[1].intValue());
        assertEquals(2, ic[2].intValue());
        assertEquals(3, ic[3].intValue());
    }

}
