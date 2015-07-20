package com.mpos.lottery.te.gamespec.sale;

import com.google.protobuf.Message;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessage;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessageSerializer;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * A base ticket definition for all game type.
 * 
 * @author Ramon Li
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class BaseEntry extends VersionEntity implements Cloneable, TeTransactionMessageSerializer {
    public static final String DEFAULT_SELECTED_NUMBER = "PLAY";

    public static final int INPUT_CHANNEL_NOTQP_NOTOMR = 0;
    public static final int INPUT_CHANNEL_QP_NOTOMR = 1;
    public static final int INPUT_CHANNEL_NOTQP_OMR = 2;
    public static final int INPUT_CHANNEL_QP_OMR = 3;

    public static final int BETOPTION_SINGLE = 1; // Single
    public static final int BETOPTION_MULTIPLE = 2; // Multiple
    public static final int BETOPTION_BANKER = 3; // Banker
    public static final int BETOPTION_ROLL = 4; // Roll

    public static final int QUICKPICK_YES = 1;
    public static final int QUICKPICK_NO = 0;

    @Column(name = "TICKET_SERIALNO")
    private String ticketSerialNo;

    @Column(name = "SELECTED_NUMBER")
    private String selectNumber;

    @Column(name = "IS_QUIDPICK")
    private int inputChannel;

    @Column(name = "BET_OPTION")
    private int betOption;

    @Column(name = "ENTRY_NO")
    private String entryNo;

    @Column(name = "TOTAL_BETS")
    private long totalBets = 1;

    @Column(name = "ENTRY_AMOUNT")
    private BigDecimal entryAmount;

    /**
     * If want the backend to generate QP numbers, the count of QP numbers must be knew.
     */
    @Transient
    private int countOfQPNumber;
    @Transient
    private SelectedNumber parsedSelectedNumber;

    public static BaseEntry defaultEntry() {
        BaseEntry entry = new BaseEntry();
        entry.setBetOption(BETOPTION_SINGLE);
        entry.setInputChannel(INPUT_CHANNEL_NOTQP_NOTOMR);
        entry.setSelectNumber(DEFAULT_SELECTED_NUMBER);
        entry.setTotalBets(1);
        return entry;
    }

    public SelectedNumber parseSelectedNumber() {
        if (this.selectNumber != null && this.parsedSelectedNumber == null) {
            this.parsedSelectedNumber = new SelectedNumber(this);
        }
        return this.parsedSelectedNumber;
    }

    public boolean isQP() {
        if (INPUT_CHANNEL_QP_NOTOMR == this.inputChannel || INPUT_CHANNEL_QP_OMR == this.inputChannel) {
            return true;
        }
        return false;
    }

    public SelectedNumber getParsedSelectedNumber() {
        return this.parsedSelectedNumber;
    }

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public String getSelectNumber() {
        return selectNumber;
    }

    public void setSelectNumber(String selectNumber) {
        this.selectNumber = selectNumber;
    }

    public int getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(int inputChannel) {
        this.setInputChannel(inputChannel, true);
    }

    public void setInputChannel(int inputChannel, boolean check) {
        if (check) {
            if (inputChannel == INPUT_CHANNEL_NOTQP_NOTOMR || inputChannel == INPUT_CHANNEL_NOTQP_OMR
                    || inputChannel == INPUT_CHANNEL_QP_NOTOMR || inputChannel == INPUT_CHANNEL_QP_OMR) {
                this.inputChannel = inputChannel;
            } else {
                throw new MessageFormatException(SystemException.CODE_UNSUPPORTED_INPUT_CHANNEL,
                        "unsupported input channel of entry:" + inputChannel);
            }
        } else {
            this.inputChannel = inputChannel;
        }
    }

    public int getBetOption() {
        return betOption;
    }

    public void setBetOption(int betOption) {
        this.betOption = betOption;
    }

    public String getEntryNo() {
        return entryNo;
    }

    public void setEntryNo(String entryNo) {
        this.entryNo = entryNo;
    }

    public long getTotalBets() {
        return totalBets;
    }

    public void setTotalBets(long totalBets) {
        this.totalBets = totalBets;
    }

    public BigDecimal getEntryAmount() {
        return entryAmount;
    }

    public void setEntryAmount(BigDecimal entryAmount) {
        this.entryAmount = entryAmount;
    }

    @Override
    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    public int getCountOfQPNumber() {
        return countOfQPNumber;
    }

    public void setCountOfQPNumber(int countOfQPNumber) {
        this.countOfQPNumber = countOfQPNumber;
    }

    @Override
    public Message toProtoMessage(Context respCtx) {
        return TeTransactionMessage.Sale.Entry.newBuilder().setBetOption(this.getBetOption())
                .setSelectedNumber(this.getSelectNumber()).setTotalBets((int) this.getTotalBets())
                .setEntryAmount(this.getEntryAmount().toString()).setInputChannel(this.getInputChannel() + "").build();
    }

    @Override
    public String toString() {
        return "BaseEntry [ticketSerialNo=" + ticketSerialNo + ", selectNumber=" + selectNumber + ", betOption="
                + betOption + ", totalBets=" + totalBets + ", entryAmount=" + entryAmount + "]";
    }

}
