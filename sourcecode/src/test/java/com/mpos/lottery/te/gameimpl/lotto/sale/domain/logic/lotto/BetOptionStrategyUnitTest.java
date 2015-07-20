package com.mpos.lottery.te.gameimpl.lotto.sale.domain.logic.lotto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetOptionStrategyUnitTest {

    @Test
    public void testRegex() throws Exception {
        assertEquals(false, matchSingle("1,2,"));
        assertEquals(false, matchSingle("11,2,"));
        assertEquals(false, matchSingle(",1,2"));
        assertEquals(false, matchSingle(",1,a"));
        assertEquals(false, matchSingle("1,2,a"));
        assertEquals(false, matchSingle("1,2a,"));
        assertEquals(false, matchSingle("1,2.3"));

        assertEquals(true, matchSingle("1,2"));
        assertEquals(true, matchSingle("1,2,3,4,5,6,11"));

        assertEquals(false, matchBanker("1,2,34,5,l"));
        assertEquals(false, matchBanker("1,2a,34,5,l"));
        assertEquals(false, matchBanker("1-2,34,5,l"));
        assertEquals(true, matchBanker("1,2,3-4,5,1"));
        assertEquals(true, matchBanker("1-4,5,1"));

        int numbers[] = { 1, 5, 3, 6 };
        Arrays.sort(numbers);
        for (int i : numbers) {
            System.out.println(i);
        }
    }

    private boolean matchSingle(String input) {
        String pattern = "^\\d+([,]\\d+)*$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.matches();
    }

    private boolean matchBanker(String input) {
        String pattern = "^\\d+([,]\\d+)*[-]\\d+([,]\\d+)*$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.matches();
    }
}
