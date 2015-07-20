package com.mpos.lottery.te.gamespec.game;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoFunType;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoOperationParameter;
import com.mpos.lottery.te.gameimpl.bingo.prize.BingoWinningItem;
import com.mpos.lottery.te.gameimpl.bingo.prize.BingoWinningStatistics;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntry;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalFunType;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalGameInstance;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalOperationParameter;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalWinningItem;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalWinningStatistics;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalTicket;
import com.mpos.lottery.te.gameimpl.extraball.prize.ExtraBallWinningItem;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallEntry;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gameimpl.lfn.game.LfnFunType;
import com.mpos.lottery.te.gameimpl.lfn.game.LfnGameInstance;
import com.mpos.lottery.te.gameimpl.lfn.game.LfnOperationParameter;
import com.mpos.lottery.te.gameimpl.lfn.prize.LfnWinningItem;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnEntry;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnTicket;
import com.mpos.lottery.te.gameimpl.lotto.draw.LottoOperationParameter;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoFunType;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningItem;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningStatistics;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100OperationParameter;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.game.RaffleGameInstance;
import com.mpos.lottery.te.gameimpl.raffle.game.RaffleOperationParameter;
import com.mpos.lottery.te.gameimpl.raffle.prize.RaffleWinning;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToEntry;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToGameInstance;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToWinningItem;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToWinningStatistics;
import com.mpos.lottery.te.gameimpl.union.game.UnionFunType;
import com.mpos.lottery.te.gameimpl.union.game.UnionGameInstance;
import com.mpos.lottery.te.gameimpl.union.game.UnionOperationParameter;
import com.mpos.lottery.te.gameimpl.union.prize.UnionWinningItem;
import com.mpos.lottery.te.gameimpl.union.prize.UnionWinningStatistics;
import com.mpos.lottery.te.gameimpl.union.sale.UnionEntry;
import com.mpos.lottery.te.gameimpl.union.sale.UnionTicket;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

public enum GameType {
    VAS(1000),
    VAT(-2),
    UNDEF(-1),
    LOTTO(
            1,
            false,
            LottoGameInstance.class,
            LottoFunType.class,
            LottoOperationParameter.class,
            LottoTicket.class,
            LottoEntry.class,
            WinningItem.class,
            WinningStatistics.class),
    SOCCER(2),
    RACING(3),
    IG(4),
    TOTO(
            5,
            false,
            ToToGameInstance.class,
            null,
            null,
            ToToTicket.class,
            ToToEntry.class,
            ToToWinningItem.class,
            ToToWinningStatistics.class),
    BINGO(
            6,
            false,
            BingoGameInstance.class,
            BingoFunType.class,
            BingoOperationParameter.class,
            BingoTicket.class,
            BingoEntry.class,
            BingoWinningItem.class,
            BingoWinningStatistics.class),
    DIGITAL(
            7,
            true,
            DigitalGameInstance.class,
            DigitalFunType.class,
            DigitalOperationParameter.class,
            DigitalTicket.class,
            DigitalEntry.class,
            DigitalWinningItem.class,
            DigitalWinningStatistics.class),
    UNION(
            8,
            true,
            UnionGameInstance.class,
            UnionFunType.class,
            UnionOperationParameter.class,
            UnionTicket.class,
            UnionEntry.class,
            UnionWinningItem.class,
            UnionWinningStatistics.class),
    EIG(9),
    LEGAL_RAFFLE(10),
    VOUCHER(12),
    EXTRABALL(
            13,
            false,
            null,
            null,
            null,
            ExtraBallTicket.class,
            ExtraBallEntry.class,
            ExtraBallWinningItem.class,
            null),
    RAFFLE(
            14,
            false,
            RaffleGameInstance.class,
            null,
            RaffleOperationParameter.class,
            RaffleTicket.class,
            null,
            RaffleWinning.class,
            null),
    LFN(
            15,
            true,
            LfnGameInstance.class,
            LfnFunType.class,
            LfnOperationParameter.class,
            LfnTicket.class,
            LfnEntry.class,
            LfnWinningItem.class,
            null),
    LUCKYNUMBER(
            18,
            true,
            Magic100GameInstance.class,
            null,
            Magic100OperationParameter.class,
            Magic100Ticket.class,
            Magic100Entry.class,
            null,
            null),
    LUCKYDRAW(19),
    AIRTIME(1000),
    TELECO_VOUCHER(1001);

    private int type;
    /**
     * If the prize logic of game type is Fix amount or Odds, it can be regarded as fixed-prize, as the turnover won't
     * affect his prize amount. The prize level definition will be ignored for this kind of game type.
     */
    private boolean fixedPrize;
    // define the meta information of game type.
    private Class<? extends BaseGameInstance> gameInstanceType;
    private Class<? extends BaseFunType> funType;
    private Class<? extends BaseOperationParameter> operationParametersType;
    private Class<? extends BaseTicket> ticketType;
    private Class<? extends BaseEntry> ticketEntryType;
    private Class<? extends BaseWinningItem> winningItemType;
    private Class<? extends BaseWinningStatistics> winningStatisticsType;

    private GameType(int type) {
        this(type, false, null, null, null, null, null, null, null);
    }

    private GameType(int type, boolean fixedPrize, Class<? extends BaseGameInstance> gameInstanceType,
            Class<? extends BaseFunType> funType, Class<? extends BaseOperationParameter> operationParametersType,
            Class<? extends BaseTicket> ticketType, Class<? extends BaseEntry> ticketEntryType,
            Class<? extends BaseWinningItem> winningItemType,
            Class<? extends BaseWinningStatistics> winningStatisticsType) {
        this.type = type;
        this.fixedPrize = fixedPrize;
        this.funType = funType;
        this.gameInstanceType = gameInstanceType;
        this.operationParametersType = operationParametersType;
        this.ticketEntryType = ticketEntryType;
        this.ticketType = ticketType;
        this.winningItemType = winningItemType;
        this.winningStatisticsType = winningStatisticsType;
    }

    /**
     * Build a <code>GameType</code> instance from its primitive value.
     */
    public static GameType fromType(int gameType) {
        GameType[] types = GameType.values();
        for (int i = 0; i < types.length; i++) {
            if (gameType == types[i].getType()) {
                return types[i];
            }
        }
        throw new SystemException(SystemException.CODE_UNSUPPORTED_TRANSTYPE, "Unsupported game type:" + gameType);
    }

    public int getType() {
        return type;
    }

    public boolean isFixedPrize() {
        return this.fixedPrize;
    }

    public Class<? extends BaseGameInstance> getGameInstanceType() {
        return gameInstanceType;
    }

    public Class<? extends BaseFunType> getFunType() {
        return funType;
    }

    public Class<? extends BaseOperationParameter> getOperationParametersType() {
        return operationParametersType;
    }

    public Class<? extends BaseTicket> getTicketType() {
        return ticketType;
    }

    public Class<? extends BaseEntry> getTicketEntryType() {
        return ticketEntryType;
    }

    public Class<? extends BaseWinningItem> getWinningItemType() {
        return winningItemType;
    }

    public Class<? extends BaseWinningStatistics> getWinningStatisticsType() {
        return winningStatisticsType;
    }

}
