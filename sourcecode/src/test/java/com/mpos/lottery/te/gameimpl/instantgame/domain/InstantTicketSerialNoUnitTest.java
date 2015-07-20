package com.mpos.lottery.te.gameimpl.instantgame.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class InstantTicketSerialNoUnitTest {

    @Test
    public void testGetXXX() {
        try {
            String serialNo = "123000001199";
            InstantTicketSerialNo no = new InstantTicketSerialNo(serialNo);
            assertEquals("123", no.getGGG());
            assertEquals("000001", no.getBBB());
            assertEquals("199", no.getIndex());

            System.out.println(Integer.parseInt("000001"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
