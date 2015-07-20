package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gamespec.game.GameType;

import org.junit.Test;

public class BarcodeUnitTest {

    @Test
    public void testEncodeAndDecode() {
        String ticketSerialNo = "S-123456";
        Barcoder barcoder = new Barcoder(GameType.LFN.getType(), ticketSerialNo);
        String barcode = barcoder.getBarcode();
        System.out.println(barcode);
        barcoder = new Barcoder(barcode);
        String serialNo = barcoder.getSerialNo();
        assertEquals(ticketSerialNo, serialNo);
        assertEquals(GameType.LFN.getType(), barcoder.getGameType());
    }

    public static void main(String args[]) {
        System.out.println(new Barcoder(GameType.RAFFLE.getType(), "20140805001").getBarcode());
    }
}
