package com.mpos.lottery.te.valueaddservice.voucher;

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

/**
 * Represents all sold vouchers.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "VAS_VOUCHER_SELLING_RECORDS")
public class VoucherSale extends SettlementEntity {
    private static final long serialVersionUID = 6561635219972027015L;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_CANCEL = 2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TE_TRANSACTION_ID", nullable = false)
    private Transaction transaction;
    @Column(name = "VOUCHER_ID")
    private String voucherId;
    @Column(name = "VOUCHER_SERIAL")
    private String voucherSerialNo;
    @Column(name = "FACE_VALUE")
    private BigDecimal voucherFaceAmount = new BigDecimal("0");
    @ManyToOne
    @JoinColumn(name = "GAME_ID")
    private Game game;
    @Column(name = "STATUS")
    private int status;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherSerialNo() {
        return voucherSerialNo;
    }

    public void setVoucherSerialNo(String voucherSerialNo) {
        this.voucherSerialNo = voucherSerialNo;
    }

    public BigDecimal getVoucherFaceAmount() {
        return voucherFaceAmount;
    }

    public void setVoucherFaceAmount(BigDecimal voucherFaceAmount) {
        this.voucherFaceAmount = voucherFaceAmount;
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

}
