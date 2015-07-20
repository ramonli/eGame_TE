package com.mpos.lottery.te.valueaddservice.airtime;

import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;

public class AirtimeDomainMocker {

    /**
     * Mock a dto.
     */
    public static AirtimeTopup mockCoobillAirtimeTopup() {
        AirtimeTopup dto = new AirtimeTopup();
        // dto.setMobileNo("008613825207590");
        dto.setMobileNo("111");
        dto.setAmount(new BigDecimal("60.0"));
        Game game = new Game();
        game.setId("AIRTIME-1");
        dto.setGame(game);
        return dto;
    }

    public static AirtimeTopup mockSmartAirtimeTopup() {
        AirtimeTopup dto = new AirtimeTopup();
        // test Data to Smart gateway
        // dto.setMobileNo("70204008");
        dto.setMobileNo("13800138000");
        dto.setAmount(new BigDecimal("0.01"));
        Game game = new Game();
        game.setId("AIRTIME-SMART");
        dto.setGame(game);
        return dto;
    }
}
