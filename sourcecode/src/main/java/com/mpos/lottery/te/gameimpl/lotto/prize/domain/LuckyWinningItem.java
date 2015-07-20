package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "WINNING_OBJECT")
public class LuckyWinningItem extends VersionEntity {
    private static final long serialVersionUID = -5997504459045787538L;

    public final static int PRIZE_TYPE_CASH = 1;
    public final static int PRIZE_TYPE_OBJECT = 2;
    public final static int WINNING_TYPE_NORMAL = 1;
    public final static int WINNING_TYPE_REDO = 2;

    @Column(name = "TICKET_SERIALNO")
    private String ticketSerialNo;
    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;
    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;
    // how many given prize level does a ticket win??
    @Column(name = "PRIZE_NUMBER")
    private int numberOfLevel;
    @Column(name = "IS_VALID")
    private boolean valid;
    // @Column(name="PRIZE_TYPE")
    // private int winningType;
    @Transient
    private int prizeType = PRIZE_TYPE_OBJECT;
    @Transient
    private PrizeObject prizeObject;
    @Transient
    private LottoGameInstance gameInstance;
    // how many objects are in the given prize level?
    @Transient
    private int numberOfPrize = 1;
    @Transient
    private int prizeGroupType = PrizeGroupItem.GROUP_TYPE_GLOBAL_LUCKYDRAW;

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getNumberOfLevel() {
        return numberOfLevel;
    }

    public void setNumberOfLevel(int numberOfPrize) {
        this.numberOfLevel = numberOfPrize;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public PrizeObject getPrizeObject() {
        return prizeObject;
    }

    public void setPrizeObject(PrizeObject prizeObject) {
        this.prizeObject = prizeObject;
    }

    public int getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(int prizeType) {
        this.prizeType = prizeType;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        if (this.prizeObject == null) {
            this.prizeObject = new PrizeObject();
        }
        this.prizeObject.setPrizeAmount(prizeAmount);
    }

    public BigDecimal getPrizeAmount() {
        if (this.prizeObject != null) {
            return this.prizeObject.getPrizeAmount();
        }
        return new BigDecimal("0");
    }

    public void setObjectName(String objectName) {
        if (this.prizeObject == null) {
            this.prizeObject = new PrizeObject();
        }
        this.prizeObject.setName(objectName);
    }

    public String getObjectName() {
        if (this.prizeObject != null) {
            return this.prizeObject.getName();
        }
        return null;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        if (this.prizeObject == null) {
            this.prizeObject = new PrizeObject();
        }
        this.prizeObject.setTax(taxAmount);
    }

    public BigDecimal getTaxAmount() {
        if (this.prizeObject != null) {
            return this.prizeObject.getTax();
        }
        return new BigDecimal("0");
    }

    // public int getWinningType() {
    // return winningType;
    // }
    //
    // public void setWinningType(int winningType) {
    // this.winningType = winningType;
    // }

    public int getNumberOfPrize() {
        if (this.prizeObject != null) {
            return this.prizeObject.getNumberOfPrize();
        }
        return 1;
    }

    public void setNumberOfPrize(int numberOfObject) {
        if (this.prizeObject == null) {
            this.prizeObject = new PrizeObject();
        }
        this.prizeObject.setNumberOfPrize(numberOfObject);
    }

    public LottoGameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(LottoGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public int getPrizeGroupType() {
        return prizeGroupType;
    }

    public void setPrizeGroupType(int prizeGroupType) {
        this.prizeGroupType = prizeGroupType;
    }

}
