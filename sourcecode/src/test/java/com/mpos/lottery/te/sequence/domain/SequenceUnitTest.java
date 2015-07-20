package com.mpos.lottery.te.sequence.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.math.BigInteger;

public class SequenceUnitTest {

    @Test
    public void testGetCurrentValue() {
        Sequence seq = this.mock();
        assertEquals(1, seq.getCurrentValue().intValue());
        assertEquals(2, seq.getCurrentValue().intValue());
        assertEquals(3, seq.getCurrentValue().intValue());
        // assertEquals(4, seq.getCurrentValue().intValue());
        assertNull(seq.getCurrentValue());

        System.out.println(seq.getMaxValue().toString());
        System.out.println(new BigInteger("3"));
    }

    //
    // @Test
    // public void testFillZero(){
    // Sequence seq = this.mock();
    // assertEquals("00001", seq.getZeroFilledCurrentValue(5));
    // assertEquals("00002", seq.getZeroFilledCurrentValue(5));
    // assertEquals("00003", seq.getZeroFilledCurrentValue(5));
    // assertNull(seq.getZeroFilledCurrentValue(5));
    // }

    private Sequence mock() {
        Sequence seq = new Sequence();
        seq.setMinValue(BigInteger.ONE);
        seq.setMaxValue(new BigInteger("999999999999"));
        seq.setInterval(new BigInteger("3"));
        seq.setNextMin(seq.getMinValue());
        seq.setNextMax(seq.getNextMin().add(seq.getInterval()).subtract(BigInteger.ONE));
        seq.setName("test");
        return seq;
    }
}
