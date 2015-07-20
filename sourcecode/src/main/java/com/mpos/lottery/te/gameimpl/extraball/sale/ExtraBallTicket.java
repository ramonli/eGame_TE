package com.mpos.lottery.te.gameimpl.extraball.sale;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "EB_TE_TICKET")
public class ExtraBallTicket extends BaseTicket {
    private static final long serialVersionUID = 153835329193049643L;
    @Column(name = "BATCH_NO")
    private String batchNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EB_GAME_INSTANCE_ID", nullable = false)
    private ExtraBallGameInstance gameInstance;

    public ExtraBallTicket() {
    }

    public ExtraBallTicket(String id, String gameInstanceId, Transaction trans, String serialNo,
            BigDecimal totalAmount, int multipleDraws) {
        super();
        this.setId(id);
        ExtraBallGameInstance gameInstance = new ExtraBallGameInstance();
        gameInstance.setId(gameInstanceId);
        this.setGameInstance(gameInstance);
        this.setTransaction(trans);
        this.setSerialNo(serialNo);
        this.setTotalAmount(totalAmount);
        this.setBatchNo(trans.getBatchNumber());
        this.setDevId(trans.getDeviceId());
        this.setMerchantId(trans.getMerchantId());
        this.setOperatorId(trans.getOperatorId());
        this.setMultipleDraws(multipleDraws);
        this.setCreateTime(new Date());
        this.setUpdateTime(this.getCreateTime());
    }

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (ExtraBallGameInstance) gameInstance;
    }

    /**
     * Validate the format of selected number.
     */
    public void validate(ExtraBallFunType funType, ExtraBallOperationParameter operationParam)
            throws ApplicationException {
        // 1st: validate format
        for (Object e : this.getEntries()) {
            ExtraBallEntry entry = (ExtraBallEntry) e;
            if (ExtraBallEntry.BET_OPTION_NUMBER == entry.getBetOption()) {
                try {
                    int number = Integer.parseInt(entry.getSelectNumber());
                    if (number < funType.getK() || number > funType.getN()) {
                        throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                                "the selected number must be in range from " + funType.getK() + " to " + funType.getN()
                                        + ", client's selected number:" + entry.getSelectNumber());
                    }
                } catch (NumberFormatException ee) {
                    throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                            "the selected number must be a number range from " + funType.getK() + " to "
                                    + funType.getN() + ", client's selected number:" + entry.getSelectNumber());
                }
            }
            // TODO validate the format when bet option is color and range.
        }

        // 2nd: validate amount
        BigDecimal expectedSum = new BigDecimal("0");
        for (Object entry : this.getEntries()) {
            ExtraBallEntry e = (ExtraBallEntry) entry;
            // client amount must be evenly divided by base amount
            if (e.getEntryAmount().intValue() % operationParam.getBaseAmount().intValue() != 0) {
                throw new ApplicationException(SystemException.CODE_EVENLY_DIVIDED_BASE_AMOUNT,
                        "Amount of single draw(" + e.getEntryAmount() + ") can't be evenly divided by base amount("
                                + operationParam.getBaseAmount() + ").");
            }
            if (e.getEntryAmount().compareTo(operationParam.getMinAmount()) < 0
                    || e.getEntryAmount().compareTo(operationParam.getMaxAmount()) > 0) {
                throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "Amount of single darw("
                        + e.getEntryAmount() + ") must be between " + operationParam.getMinAmount() + " and "
                        + operationParam.getMaxAmount());
            }
            expectedSum = expectedSum.add(e.getEntryAmount());
        }
        // verify total amount
        expectedSum = expectedSum.multiply(new BigDecimal(this.getMultipleDraws()));
        if (expectedSum.compareTo(this.getTotalAmount()) != 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The exptected total amount("
                    + expectedSum + ") calculated based on entries is unmatched with " + this.getTotalAmount());
        }

        // 3rd: validate multiple draws
        if (this.getMultipleDraws() < operationParam.getMinAllowedMultiDraw()
                || this.getMultipleDraws() > operationParam.getMaxAllowedMultiDraw()) {
            throw new ApplicationException(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, "The request multil draw("
                    + this.getMultipleDraws() + ") must be in range from " + operationParam.getMinAllowedMultiDraw()
                    + " to " + operationParam.getMaxAllowedMultiDraw());
        }
    }

    /**
     * Calculate the amount of a single draw ticket.
     */
    public BigDecimal singleDrawAmount() {
        return this.getTotalAmount().divide(new BigDecimal(this.getMultipleDraws()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * In which batch this ticket is sold/cancel?
     * <ul>
     * <li>LottoTicket is cancelled, including cancel declined, this field represents the batch number of cancellation
     * transaction.</li>
     * <li>LottoTicket is accepted/paid, it represents the batch number of sale transaction.</li>
     * </ul>
     * In general, sale and cancellation transction will belong to same batch.
     * 
     * @return the batch number of sale/cancel transaction.
     */
    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    @Override
    protected void verifyExtendTxt(List<? extends BaseEntry> actualEntries) throws ApplicationException {
        // do nothing
    }

}
