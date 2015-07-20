package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InstantPrizeCalculatorUnitTest {
    private int totalPrizeLevel = 10;

    @Test
    public void testCalculatePrizeLevel_1() {
        String serialNo = "456123789012";
        String xor = "363386044168419078795268";
        String mac = "333CF2725BDC6C54D37CE39580197800";
        int prizeLevelIndex = 7;

        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(serialNo, mac, xor, prizeLevelIndex,
                totalPrizeLevel);
        assertEquals(prizeLevel, 3);
    }

    @Test
    public void testCalculatePrizeLevel_2() {
        String serialNo = "598198195103";
        String xor = "907062589474802131240018";
        String mac = "DDC65B1798283D04A1F1038F0FE976C9";
        int prizeLevelIndex = 0;

        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(serialNo, mac, xor, prizeLevelIndex,
                totalPrizeLevel);
        assertEquals(prizeLevel, 1);
    }

    @Test
    public void testCalculatePrizeLevel_3() {
        String serialNo = "198415681983";
        String xor = "581038306634533037330218";
        String mac = "B17835883C94BE331F4ECA6E89AC797F";
        int prizeLevelIndex = 15;

        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(serialNo, mac, xor, prizeLevelIndex,
                totalPrizeLevel);
        assertEquals(prizeLevel, 2);
    }

    @Test
    public void testCalculatePrizeLevel_4() {
        String serialNo = "984161896312";
        String xor = "314556304314173430586373";
        String mac = "69A02DE5B12EF084A2CF287EF368A90E";
        int prizeLevelIndex = 0;

        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(serialNo, mac, xor, prizeLevelIndex,
                totalPrizeLevel);
        assertEquals(prizeLevel, 0);
    }

    @Test
    public void testCalculatePrizeLevel_5() {
        String serialNo = "157823119021";
        String xor = "186754702379549779712579";
        String mac = "A059A8CD6395DBCD5C85AF88F0D53795";
        int prizeLevelIndex = 10;

        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(serialNo, mac, xor, prizeLevelIndex,
                totalPrizeLevel);
        assertEquals(prizeLevel, 1);
    }

    @Test
    public void testCalculatePrizeLevel_6() {
        String serialNo = "954977971257";
        String xor = "376502212530453769805098";
        String mac = "A4574F63DE45B1197B910CDCFD704D32";
        int prizeLevelIndex = 9;

        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(serialNo, mac, xor, prizeLevelIndex,
                totalPrizeLevel);
        assertEquals(prizeLevel, 2);
    }

}
