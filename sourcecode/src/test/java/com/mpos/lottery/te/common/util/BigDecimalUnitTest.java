package com.mpos.lottery.te.common.util;

import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalUnitTest {

    @Test
    public void testBigDecimal() {
        double d = 10 / 3d;
        System.out.println(d);
        BigDecimal b = new BigDecimal("10.245");
        b = b.setScale(2, BigDecimal.ROUND_HALF_UP);

        // BigDecimal a = new BigDecimal("10.245", new MathContext(19, );
        System.out.println(b);
    }
}
