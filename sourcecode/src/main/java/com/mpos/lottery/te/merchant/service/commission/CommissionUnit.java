package com.mpos.lottery.te.merchant.service.commission;

import com.mpos.lottery.te.merchant.domain.BalanceTransactions;

import java.math.BigDecimal;

/**
 * This class will be used to carry the information of commission calculation.
 * 
 * @author Ramon
 */
public class CommissionUnit {
    // for same transaction, such as 'operator cashout', simply keep it as null.
    private String gameId;
    // commission = commissionRate*transAmount
    private BigDecimal commissonRate = new BigDecimal("0");
    private BigDecimal transAmount = new BigDecimal("0");
    // to sale, payment type is deducting, however to payout, payment type is plusing.
    private int paymentType = BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY;

    /**
     * Constructor.
     */
    public CommissionUnit(String gameId, BigDecimal commissonRate, BigDecimal transAmount) {
        super();
        this.gameId = gameId;
        this.commissonRate = commissonRate;
        this.transAmount = transAmount;
    }

    /**
     * Constructor.
     */
    public CommissionUnit(String gameId, BigDecimal commissonRate, BigDecimal transAmount, int paymentType) {
        super();
        this.gameId = gameId;
        this.commissonRate = commissonRate;
        this.transAmount = transAmount;
        this.paymentType = paymentType;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public BigDecimal getCommissonRate() {
        return commissonRate;
    }

    public void setCommissonRate(BigDecimal commissonRate) {
        this.commissonRate = commissonRate;
    }

    public BigDecimal getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(BigDecimal transAmount) {
        this.transAmount = transAmount;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

}
