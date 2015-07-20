package com.mpos.lottery.te.gameimpl.lotto;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoFunType;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LottoDomainMocker {

    public static LottoTicket mockTicket() {
        LottoTicket ticket = new LottoTicket();
        ticket.setPIN("123456");
        ticket.setTotalAmount(new BigDecimal("800"));
        ticket.setMultipleDraws(1);
        ticket.setWinning(false);
        // set game instance
        LottoGameInstance gameDraw = new LottoGameInstance();
        gameDraw.setNumber("20090408");
        gameDraw.setGameId("GAME-111");
        // gameDraw.setNumber("20090724");
        // gameDraw.setGameId("GAME-113");
        ticket.setGameInstance(gameDraw);

        // set lotto entries
        List<BaseEntry> entries = new ArrayList<BaseEntry>(0);
        LottoEntry entry1 = new LottoEntry();
        entry1.setBetOption(LottoEntry.BETOPTION_SINGLE);
        entry1.setInputChannel(1);
        entry1.setSelectNumber("1,2,3,6,7,13");
        entry1.setBoostAmount(new BigDecimal("100"));
        entries.add(entry1);
        LottoEntry entry2 = new LottoEntry();
        entry2.setBetOption(LottoEntry.BETOPTION_MULTIPLE);
        entry2.setInputChannel(1);
        entry2.setSelectNumber("3,11,14,16,22,25,36");
        entries.add(entry2);
        ticket.setEntries(entries);

        return ticket;
    }

    public static LottoGameInstance mockGameDraw() {
        // set game instance
        LottoGameInstance gameDraw = new LottoGameInstance();
        gameDraw.setNumber("20090408");
        gameDraw.setGameId("GAME-111");
        return gameDraw;
    }

    public static Transaction mockTransaction() {
        Transaction trans = new Transaction();
        trans.setCreateTime(new Date());
        trans.setDeviceId(111);
        trans.setGpeId("GPE-111");
        trans.setId(SimpleToolkit.simpleUUID());
        trans.setMerchantId(111);
        trans.setOperatorId("O-111");
        trans.setResponseCode(200);
        trans.setTicketSerialNo("123456");
        trans.setTraceMessageId("TM-111");
        trans.setTransTimestamp(new Date());
        trans.setType(201);
        trans.setVersion(100);

        trans.setTicket(LottoDomainMocker.mockTicket());

        return trans;
    }

    public static LottoFunType mockLottoFunType() {
        LottoFunType type = new LottoFunType();
        type.setK(6);
        type.setN(49);

        return type;
    }

    public static Payout mockPayout() {
        Payout payout = new Payout();
        payout.setCreateTime(new Date());
        payout.setUpdateTime(new Date());
        payout.setTicketSerialNo("123456");
        payout.setValid(true);
        payout.setTotalAmount(new BigDecimal("1245.01"));
        payout.setId(SimpleToolkit.simpleUUID());
        LottoGameInstance draw = new LottoGameInstance();
        draw.setId("GII-111");
        payout.setGameInstance(draw);
        Transaction tran = new Transaction();
        tran.setId("TRANS-111");
        payout.setTransaction(tran);
        payout.setType(Payout.TYPE_WINNING);

        return payout;
    }

    public static InstantTicket mockInstantTicket() {
        InstantTicket ticket = new InstantTicket();
        ticket.setSerialNo("984161896312");
        return ticket;
    }
}
