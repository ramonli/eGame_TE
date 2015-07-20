package com.mpos.lottery.te.merchant.web;

import java.math.BigDecimal;

public class IncomeBalanceDto {
    private BigDecimal amount;
    private BigDecimal payoutBalance = new BigDecimal("0");
    private BigDecimal saleBalance = new BigDecimal("0");
    private BigDecimal cashoutBalance = new BigDecimal("0");
    private BigDecimal commissionBalance = new BigDecimal("0");

    /** need cancelled trasaction **/
    private Long merchantId;
    private String operatorId;
    private BigDecimal deductPayoutAmount = new BigDecimal("0");
    private BigDecimal deductCashoutAmount = new BigDecimal("0");
    private BigDecimal deductCommissionAmount = new BigDecimal("0");
    private BigDecimal addSaleAmount = new BigDecimal("0");

    /**
     * @return amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @param amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * @return payoutBalance
     */
    public BigDecimal getPayoutBalance() {
        return payoutBalance;
    }

    /**
     * @param payoutBalance
     */
    public void setPayoutBalance(BigDecimal payoutBalance) {
        this.payoutBalance = payoutBalance;
    }

    /**
     * @return saleBalance
     */
    public BigDecimal getSaleBalance() {
        return saleBalance;
    }

    /**
     * @param saleBalance
     */
    public void setSaleBalance(BigDecimal saleBalance) {
        this.saleBalance = saleBalance;
    }

    /**
     * @return cashoutBalance
     */
    public BigDecimal getCashoutBalance() {
        return cashoutBalance;
    }

    /**
     * @param cashoutBalance
     */
    public void setCashoutBalance(BigDecimal cashoutBalance) {
        this.cashoutBalance = cashoutBalance;
    }

    /**
     * @return commissionBalance
     */
    public BigDecimal getCommissionBalance() {
        return commissionBalance;
    }

    /**
     * @param commissionBalance
     */
    public void setCommissionBalance(BigDecimal commissionBalance) {
        this.commissionBalance = commissionBalance;
    }

    /**
     * @return merchantId
     */
    public Long getMerchantId() {
        return merchantId;
    }

    /**
     * @param merchantId
     */
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    /**
     * @return operatorId
     */
    public String getOperatorId() {
        return operatorId;
    }

    /**
     * @param operatorId
     */
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * @return deductPayoutAmount
     */
    public BigDecimal getDeductPayoutAmount() {
        return deductPayoutAmount;
    }

    /**
     * @param deductPayoutAmount
     */
    public void setDeductPayoutAmount(BigDecimal deductPayoutAmount) {
        this.deductPayoutAmount = deductPayoutAmount;
    }

    /**
     * @return deductCashoutAmount
     */
    public BigDecimal getDeductCashoutAmount() {
        return deductCashoutAmount;
    }

    /**
     * @param deductCashoutAmount
     */
    public void setDeductCashoutAmount(BigDecimal deductCashoutAmount) {
        this.deductCashoutAmount = deductCashoutAmount;
    }

    /**
     * @return deductCommissionAmount
     */
    public BigDecimal getDeductCommissionAmount() {
        return deductCommissionAmount;
    }

    /**
     * @param deductCommissionAmount
     */
    public void setDeductCommissionAmount(BigDecimal deductCommissionAmount) {
        this.deductCommissionAmount = deductCommissionAmount;
    }

    /**
     * @return addSaleAmount
     */
    public BigDecimal getAddSaleAmount() {
        return addSaleAmount;
    }

    /**
     * @param addSaleAmount
     */
    public void setAddSaleAmount(BigDecimal addSaleAmount) {
        this.addSaleAmount = addSaleAmount;
    }

}
