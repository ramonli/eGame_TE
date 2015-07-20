package com.mpos.lottery.te.valueaddservice.airtime;

import com.mpos.lottery.te.common.dao.SettlementEntity;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "AIRTIME_SELLING_RECORDS")
public class AirtimeTopup extends SettlementEntity {
    public static int STATUS_FAIL = 0;
    public static int STATUS_SUCCESS = 1;
    public static int STATUS_PENDING = 2;
    private static final long serialVersionUID = 1172469940694133012L;
    /**
     * The serialNo will be used as refNo of request of 3rd party service.
     */
    @Column(name = "SERIAL")
    private String serialNo;
    @Column(name = "TOPUP_AMOUNT")
    private BigDecimal amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_ID", nullable = false)
    private Game game;
    /**
     * Refer to constant STATUS_XXX.
     */
    @Column(name = "STATUS")
    private int status;
    /**
     * Refer to BaseTicket.TICKET_FROM_XXX
     */
    @Column(name = "SELLING_FROM")
    private int gpeSourceType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TE_TRANSACTION_ID", nullable = false)
    private Transaction transaction;
    @Column(name = "TELCO_REF_ID")
    private String telcCommTransId;
    @Column(name = "PHONE_NUMBER")
    private String mobileNo;
    @Transient
    private String respMessageOfRemoteService;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getGpeSourceType() {
        return gpeSourceType;
    }

    public void setGpeSourceType(int gpeSourceType) {
        this.gpeSourceType = gpeSourceType;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getTelcCommTransId() {
        return telcCommTransId;
    }

    public void setTelcCommTransId(String telcCommTransId) {
        this.telcCommTransId = telcCommTransId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getRespMessageOfRemoteService() {
        return respMessageOfRemoteService;
    }

    public void setRespMessageOfRemoteService(String respMessageOfRemoteService) {
        this.respMessageOfRemoteService = respMessageOfRemoteService;
    }

}
