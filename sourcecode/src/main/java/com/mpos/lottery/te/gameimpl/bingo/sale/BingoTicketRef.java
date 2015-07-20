package com.mpos.lottery.te.gameimpl.bingo.sale;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * The in advance generated bingo ticket for online sale.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "BG_TICKET_REF")
public class BingoTicketRef extends BaseEntity {
    private static final long serialVersionUID = 7329498629340379998L;
    public static final int STATUS_NEW = 1;
    public static final int STATUS_SOLD = 2;
    public static final int STATUS_CANCEL = 3;

    @Column(name = "BG_GAME_INSTANCE_ID")
    private String gameInstanceId;

    @Column(name = "SERIAL_NO")
    private String importTicketSerialNo;

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount = new BigDecimal("0");

    /**
     * Refer to STATUS_XXX.
     */
    @Column(name = "STATUS")
    private int status;

    @Column(name = "PIN")
    private String pin;

    @Column(name = "SERIAL")
    private String luckySerial;

    @Column(name = "BOOK")
    private String bookNo;

    @Column(name = "SEQUENCE_NUMBER")
    private long sequence;

    @Transient
    List<BingoEntryRef> entryRefs = new LinkedList<BingoEntryRef>();

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public String getImportTicketSerialNo() {
        return importTicketSerialNo;
    }

    public void setImportTicketSerialNo(String rawTicketSerialNo) {
        this.importTicketSerialNo = rawTicketSerialNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getLuckySerial() {
        return luckySerial;
    }

    public void setLuckySerial(String luckySerial) {
        this.luckySerial = luckySerial;
    }

    public String getBookNo() {
        return bookNo;
    }

    public void setBookNo(String bookNo) {
        this.bookNo = bookNo;
    }

    public List<BingoEntryRef> getEntryRefs() {
        return entryRefs;
    }

    public void setEntryRefs(List<BingoEntryRef> entryRefs) {
        this.entryRefs = entryRefs;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

}
