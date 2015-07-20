package com.mpos.lottery.te.gameimpl.lotto.sale;

import java.math.BigDecimal;

/**
 * Represents the payment of a transaction.
 * 
 * @author Ramon Li
 */
public class Payment {
    /**
     * When assemble a payment request, client doesn't know how must capital amount and free amount will be deduced, so
     * in such a case, client only need to specify the field 'capitalAmount', which the service will calculate out how
     * much real capital amount and free amound needed, and return the detailed information in returned payment.
     */
    private BigDecimal capitalAmount = new BigDecimal("0");
    private BigDecimal freeAmount = new BigDecimal("0");

    public Payment() {
    }

    public Payment(BigDecimal capitalAmount, BigDecimal freeAmount) {
        if (capitalAmount != null) {
            this.capitalAmount = capitalAmount;
        }
        if (freeAmount != null) {
            this.freeAmount = freeAmount;
        }
    }

    public BigDecimal getCapitalAmount() {
        return capitalAmount;
    }

    public void setCapitalAmount(BigDecimal capitalAmount) {
        this.capitalAmount = capitalAmount;
    }

    public BigDecimal getFreeAmount() {
        return freeAmount;
    }

    public void setFreeAmount(BigDecimal freeAmount) {
        this.freeAmount = freeAmount;
    }

}
