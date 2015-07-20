package com.mpos.lottery.te.valueaddservice.voucher;

import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;

public class VoucherDomainMocker {

    /**
     * Mock a dto.
     */
    public static Voucher mockVoucherTopup() {
        Voucher dto = new Voucher();
        dto.setFaceAmount(new BigDecimal("60.0"));
        Game game = new Game();
        game.setId("VOUCHER-1");
        dto.setGame(game);
        return dto;
    }
}
