package com.mpos.lottery.te.gameimpl.lotto.sale.domain;

import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTamperProofTicket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "TE_TICKET")
public class LottoTicket extends BaseTamperProofTicket {
    private static final long serialVersionUID = -781425489865251044L;
    private static Log logger = LogFactory.getLog(LottoTicket.class);

    public static final int SOURCE_POS = 1;
    public static final int SOURCE_IBETTING = 2;
    public static final int SOURCE_SMS_VOUCHER = 3;
    public static final int SOURCE_SMS = 4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_INSTANCE_ID", nullable = false)
    private LottoGameInstance gameInstance;

    // @Column(name = "FREE_AMOUNT")
    @Transient
    private BigDecimal freeAmount = new BigDecimal("0");

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (LottoGameInstance) gameInstance;
    }

    public BigDecimal getFreeAmount() {
        return freeAmount;
    }

    public void setFreeAmount(BigDecimal freeAmount) {
        this.freeAmount = freeAmount;
    }

}
