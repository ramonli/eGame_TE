package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SimpleToolkitUnitTest {

    @Test
    public void testGeneratePIN() {
        int pin = SimpleToolkit.generatePin(100000, 999999);
        System.out.println("PIN: " + pin);
    }

    @Test
    public void testDayAfter() {
        Date now = new Date();
        System.out.println(SimpleToolkit.dayAfter(now, 365));
    }

    @Test
    public void testMd5() {
        String m = SimpleToolkit.md5("!!!!");
        System.out.println(m);
        m = SimpleToolkit.md5("111111");
        System.out.println(m);
    }

    @Test
    public void testCompareDate() throws Exception {
        Date d1 = new Date();
        Thread.sleep(1 * 1000);
        Date d2 = new Date();
        System.out.println(SimpleToolkit.compare(d1, d2));
    }

    @Test
    public void testSplitLong() {
        String input = "12,1,41";
        List result = SimpleToolkit.splitToLong(input, ",");
        System.out.println(result.get(0) + "," + result.get(1) + "," + result.get(2));

        input = ",12,1,14";
        result = SimpleToolkit.splitToLong(input, ",");
        System.out.println(result.get(0) + "," + result.get(1) + "," + result.get(2));

        input = ",12,1,14,";
        result = SimpleToolkit.splitToLong(input, ",");
        System.out.println(result.get(0) + "," + result.get(1) + "," + result.get(2));
    }

    @Test
    public void testString2IntArray() throws Exception {
        String input = "01,31,09,012,22";
        int[] output = SimpleToolkit.string2IntArray(input, ",", true);
        assertEquals("[1, 9, 12, 22, 31]", Arrays.toString(output));
        output = SimpleToolkit.string2IntArray(input, ",", false);
        assertEquals("[1, 31, 9, 12, 22]", Arrays.toString(output));
    }

    @Test
    public void testFormatNumericString() throws Exception {
        String input = "01,31,09,012,22";
        String output = SimpleToolkit.formatNumericString(input, ",");
        assertEquals("1,31,9,12,22", output);
    }

    @Test
    public void testJoinNumbers() throws Exception {
        int[] input = new int[] { 4, 2, 7, 4, 8, 1 };
        String output = SimpleToolkit.join(input, 0, 6, ",");
        assertEquals("4,2,7,4,8,1", output);

        assertEquals("2,7,4,8", SimpleToolkit.join(input, 1, 4, ","));
        assertEquals("4,2,7,4,8", SimpleToolkit.join(input, 0, 5, ","));
        assertEquals("2,7,4,8,1", SimpleToolkit.join(input, 1, 5, ","));

        Arrays.sort(input);
        System.out.println(input);
    }

    @Test
    public void testJoinList() throws Exception {
        int[] input = new int[] { 4, 2, 7, 4, 8, 1 };
        Arrays.sort(input);
        String output = SimpleToolkit.join(Arrays.asList(1, 2, 4, 4, 7, 8), SelectedNumber.DELEMETER_NUMBER);
        assertEquals("1,2,4,4,7,8", output);
    }
}
