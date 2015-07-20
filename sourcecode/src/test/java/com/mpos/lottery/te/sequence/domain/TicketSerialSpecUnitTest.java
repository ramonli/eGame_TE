package com.mpos.lottery.te.sequence.domain;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.util.SimpleToolkit;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Date;

public class TicketSerialSpecUnitTest {

    @Test
    public void testToSerialNo() {
        Date date = SimpleToolkit.parseDate("20131220183927", "yyyyMMddHHmmss");
        TicketSerialSpec spec = new TicketSerialSpec(TicketSerialSpec.ONLINE_MODE, date, 7, new BigInteger("781273"));
        System.out.println(spec);
        String serialNo = spec.toSerialNo();
        assertEquals(20, serialNo.length());
        String fix = serialNo.substring(0, 18);
        // OYDHGHDYGMDMXXXXXXZZ
        assertEquals("013108537349781273", fix);
    }

    public void TestFromSerialNo() throws Exception {
        // [saleMode=0, year=13, dayOfYear=354, gameType=07, hour=18, minute=39,
        // sequence=781273, randomNum=36]
        TicketSerialSpec spec = new TicketSerialSpec("01310853734978127336");
        assertEquals("0", spec.getMode());
        assertEquals("13", spec.getYear());
        assertEquals("354", spec.getDayOfYear());
        assertEquals("07", spec.getGameTypeOrSecond());
        assertEquals(7, Integer.parseInt(spec.getGameTypeOrSecond()));
        assertEquals("18", spec.getHour());
        assertEquals("39", spec.getMinute());
        assertEquals("781273", spec.getSequence());
        assertEquals("36", spec.getRandomNum());
    }

    public static void main(String args[]) {
        TicketSerialSpec spec = new TicketSerialSpec(TicketSerialSpec.ONLINE_MODE, new Date(), 7, new BigInteger(
                "781273"));
        System.out.println(spec.toSerialNo());
    }
}
