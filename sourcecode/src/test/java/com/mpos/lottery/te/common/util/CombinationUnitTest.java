package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CombinationUnitTest {
    private static String[] input;
    private static int r = 2;
    private static int n = 10; // if n=49, this test will run a long time.
    private static int total = 45;

    private Combination comb;

    @BeforeClass
    public static void beforeClass() {
        input = new String[r];
        for (int i = 1; i < (r + 1); i++) {
            input[i - 1] = i + "";
        }
    }

    @Before
    public void setUp() {
        comb = new Combination(n, r);
    }

    @After
    public void tearDown() {
        comb = null;
    }

    @Test
    public void testGetTotal() {
        assertEquals(total, comb.getTotal().longValue());
    }

    @Test
    public void testGetNext() {
        int[] combItem = comb.getNext();
        assertEquals("0,1", printCombination(combItem));
        for (int i = 0; i < (total - 1); i++) {
            assertEquals(true, comb.hasMore());
            assertEquals((total - i - 1), comb.getNumLeft().longValue());
            comb.getNext();
        }
        assertEquals(false, comb.hasMore());
        try {
            comb.getNext();
            fail("Beyond the total.");
        } catch (ArrayIndexOutOfBoundsException e) {
            // silently ignored
        }

        // reset
        comb.reset();
        comb.getNext();
        assertEquals(true, comb.hasMore());
        assertEquals(total - 1, comb.getNumLeft().longValue());
    }

    private static String printCombination(int n[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n.length; i++) {
            if (i != 0)
                sb.append(",");
            sb.append(n[i]);
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        System.out.println(new Combination(6, 3).getTotal());
        System.out.println(new Combination(90, 3).getTotal());
        Combination c = new Combination(6, 3);
        for (int i = 0; i < c.getTotal().intValue(); i++) {
            int[] n = c.getNext();
            System.out.println(printCombination(n));
        }
    }
}
