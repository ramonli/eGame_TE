package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantVIRNPrize;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

public class PrizeLevelDto implements Serializable {
    private static final long serialVersionUID = -5759308599431676228L;
    public static final int PRIZE_TYPE_CASH = 1;
    public static final int PRIZE_TYPE_OBJECT = 2;
    public static final int PRIZE_TYPE_BOTH = 3;

    public static final int STATUS_CODE_DUP_MATCHED = 2;
    public static final int STATUS_CODE_DUP_UNMATCHED = 1;
    public static final int STATUS_CODE_UNMATCHED = 3;

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String prizeLogicId;
    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;
    // This value will be set once the prize level has been calculated
    @Column(name = "PRIZE_LEVEL_TYPE")
    private int prizeType = PRIZE_TYPE_CASH;
    // How many players have won t1his prize level??
    @Column(name = "PRIZE_WIN_COUNT")
    private Integer numberOfWinner = 0;
    @OneToMany(mappedBy = "prizeLevel", fetch = FetchType.LAZY)
    private List<PrizeLevelItemDto> levelItems = new ArrayList<PrizeLevelItemDto>();

    // ------------------------------------------------------
    // TRANSIENT FIELDS
    // ------------------------------------------------------

    // total prize amount of all associated items, operator has to input prize
    // amount if needed
    @Transient
    private BigDecimal clientPrizeAmount;
    @Transient
    private BigDecimal prizeAmount = new BigDecimal("0");
    // total tax amount of all associated items.
    @Transient
    private BigDecimal taxAmount = new BigDecimal("0");
    // amount after tax, total actual amount of all associated items.
    @Transient
    private BigDecimal actualAmount = new BigDecimal("0");
    @Transient
    private InstantTicket ticket;
    @Transient
    private int inputChannel; // refer to Payout.INPUT_CHANNEL_XXX
    // Just be compitable with VIRN algorithm(TODO: remove it).
    @Transient
    private InstantVIRNPrize virnPrize;
    // only for batch validation
    @Transient
    private int errorCode = SystemException.CODE_OK;
    // when generate payout records, only cash amount will be counted.
    @Transient
    private BigDecimal cashPrizeAmount, cashTaxAmount, cashActualAmount = new BigDecimal("0");
    // when upload offline validation, this status code must be set at response
    // to identity the handling result at the backend.
    @Transient
    private int statusCode;
    // when has this ticket been validated? when upload offline validation, this
    // field must be set.
    @Transient
    private Date validateTime;

    public PrizeLevelDto() {
    }

    public PrizeLevelDto(com.mpos.lottery.te.gamespec.prize.PrizeLevel prizeLevelDef) {
        super();
        this.id = prizeLevelDef.getId();
        this.updateTime = prizeLevelDef.getUpdateTime();
        this.prizeLogicId = prizeLevelDef.getPrizeLogicId();
        this.prizeLevel = prizeLevelDef.getPrizeLevel();
        this.prizeType = prizeLevelDef.getPrizeType();
        this.numberOfWinner = prizeLevelDef.getNumberOfWinner();
        for (com.mpos.lottery.te.gamespec.prize.PrizeLevelItem levelItem : prizeLevelDef.getLevelItems()) {
            this.getLevelItems().add(new PrizeLevelItemDto(levelItem));
        }
    }

    /**
     * Calculate prizeAmount/taxAmount/taxAmount based on <code>List&lt;PrizeLevelItemDto&gt;</code>.
     * <p>
     * This method should be called after <code>setLevelItems</code>. Due to <code>PrizeLevel</code> and
     * <code>PrizeLevelItemDto</code> are managed by JPA runtime, when initialize <code>PrizeLevel</code>, the runtime
     * will set a List<PrizeLevelItemDto> of 0 elements to it, that is why we can't calculate XXX_amount in
     * <code>setLevelItems()</code>
     * <p>
     * Besides call this method will force runtime to retrieve the entity <code>PrizeLevelItemDto</code> from underlying
     * database, not just a proxy.
     */
    public void calculateAmount() {
        /**
         * prizeAmount/taxAmount/actualAmount count both cash and object amount.
         * cashPrizeAmount/cashTaxAmount/cashActualAmount count only cash amount, and when generate 'PAYOUT' record,
         * cashXXXAmount will be set as totalAmount.
         * <p>
         * But when generating response to client, the prizeAmount/taxAmount/actualAmount will be returned.
         */
        // reset all XXX_Amount first
        this.prizeAmount = new BigDecimal("0");
        this.taxAmount = new BigDecimal("0");
        this.actualAmount = new BigDecimal("0");

        this.cashActualAmount = new BigDecimal("0");
        this.cashTaxAmount = new BigDecimal("0");
        this.cashPrizeAmount = new BigDecimal("0");

        // update all XXX_AMOUNT
        for (PrizeLevelItemDto item : this.levelItems) {
            this.prizeAmount = this.prizeAmount.add(item.getPrizeAmount().multiply(
                    new BigDecimal(item.getNumberOfObject())));
            this.taxAmount = this.taxAmount.add(item.getTaxAmount().multiply(new BigDecimal(item.getNumberOfObject())));
            this.actualAmount = this.actualAmount.add(item.getActualAmount().multiply(
                    new BigDecimal(item.getNumberOfObject())));
            if (PrizeLevelDto.PRIZE_TYPE_CASH == item.getPrizeType()) {
                this.cashPrizeAmount = this.cashPrizeAmount.add(item.getPrizeAmount().multiply(
                        new BigDecimal(item.getNumberOfObject())));
                this.cashTaxAmount = this.cashTaxAmount.add(item.getTaxAmount().multiply(
                        new BigDecimal(item.getNumberOfObject())));
            }
        }
        this.cashActualAmount = this.cashPrizeAmount.subtract(this.cashTaxAmount);
    }

    public int getNumberOfObject() {
        int total = 0;
        for (PrizeLevelItemDto item : this.levelItems) {
            if (PrizeLevelDto.PRIZE_TYPE_CASH != item.getPrizeType()) {
                total += item.getNumberOfObject();
            }
        }
        return total;
    }

    public List<PrizeLevelItemDto> getItemByPrizeType(int prizeType) {
        List<PrizeLevelItemDto> prizeLevelItems = new ArrayList<PrizeLevelItemDto>();
        for (PrizeLevelItemDto item : this.levelItems) {
            if (prizeType == item.getPrizeType()) {
                prizeLevelItems.add(item);
            }
        }
        return prizeLevelItems;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    /**
     * Except test methods, no other clients should call this method.
     */
    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    /**
     * Except test methods, no other clients should call this method.
     */
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public InstantTicket getTicket() {
        return ticket;
    }

    public void setTicket(InstantTicket ticket) {
        this.ticket = ticket;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    /**
     * Except test methods, no other clients should call this method.
     */
    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public int getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(int inputChannel) {
        this.inputChannel = inputChannel;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(int prizeType) {
        this.prizeType = prizeType;
    }

    public InstantVIRNPrize getVirnPrize() {
        return virnPrize;
    }

    public void setVirnPrize(InstantVIRNPrize virnPrize) {
        this.virnPrize = virnPrize;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setLevelItems(List<PrizeLevelItemDto> prizeLevelItems) {
        this.levelItems = prizeLevelItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

    public Integer getNumberOfWinner() {
        return numberOfWinner;
    }

    public void setNumberOfWinner(Integer numberOfWinner) {
        this.numberOfWinner = numberOfWinner;
    }

    public List<PrizeLevelItemDto> getLevelItems() {
        return levelItems;
    }

    public BigDecimal getClientPrizeAmount() {
        return clientPrizeAmount;
    }

    public void setClientPrizeAmount(BigDecimal clientPrizeAmount) {
        this.clientPrizeAmount = clientPrizeAmount;
    }

    public BigDecimal getCashPrizeAmount() {
        return cashPrizeAmount;
    }

    public void setCashPrizeAmount(BigDecimal cashPrizeAmount) {
        this.cashPrizeAmount = cashPrizeAmount;
    }

    public BigDecimal getCashTaxAmount() {
        return cashTaxAmount;
    }

    public void setCashTaxAmount(BigDecimal cashTaxAmount) {
        this.cashTaxAmount = cashTaxAmount;
    }

    public BigDecimal getCashActualAmount() {
        return cashActualAmount;
    }

    public void setCashActualAmount(BigDecimal cashActualAmount) {
        this.cashActualAmount = cashActualAmount;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Date getValidateTime() {
        return validateTime;
    }

    public void setValidateTime(Date validateTime) {
        this.validateTime = validateTime;
    }
}
