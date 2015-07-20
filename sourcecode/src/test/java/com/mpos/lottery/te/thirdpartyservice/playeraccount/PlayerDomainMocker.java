package com.mpos.lottery.te.thirdpartyservice.playeraccount;

import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutRequest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerTopupDto;

import java.math.BigDecimal;

public class PlayerDomainMocker {

    /**
     * Mock a cashout dto.
     */
    public static CashoutRequest cashoutRequest() {
        CashoutRequest dto = new CashoutRequest();
        dto.setMobile("13800138001");
        dto.setUserPIN("!!!");
        // dto.setMobile("999888");
        // dto.setUserPIN("7");
        dto.setCashoutAmount(new BigDecimal("300.0"));
        return dto;
    }

    /**
     * Mock a topup dto.
     */
    public static PlayerTopupDto mockVoucherTopup() {
        PlayerTopupDto dto = new PlayerTopupDto();
        dto.setAmount(new BigDecimal("60.0"));
        dto.setVoucherSerialNo("PLAYER-VOUCHER");
        dto.setAccountId("13800138000");
        return dto;
    }
}
