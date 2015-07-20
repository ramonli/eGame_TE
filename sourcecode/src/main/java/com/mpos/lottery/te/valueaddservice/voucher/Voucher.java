package com.mpos.lottery.te.valueaddservice.voucher;

import com.mpos.lottery.te.common.dao.BaseEntity;
import com.mpos.lottery.te.common.encrypt.DesCipher;
import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents all imported vouchers.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "VAS_VOUCHERS")
public class Voucher extends BaseEntity {
    private static final String VOUCHER_PIN_DES_KEY = "KmvjzpgmPlI=";
    private static final long serialVersionUID = 6561635219972027015L;
    public static final int STATUS_IMPORTED = 1;
    public static final int STATUS_SOLD = 2;
    public static final int STATUS_INVALID = 3;

    @Column(name = "SERIAL_NUMBER")
    private String serialNo;
    @Column(name = "VOUCHER_NUMBER")
    private String pin;
    @Column(name = "EXPIRED_DATE")
    private Date expireDate;
    @Column(name = "FACE_VALUE")
    private BigDecimal faceAmount = new BigDecimal("0");
    @Column(name = "STATUS")
    private int status;
    @Column(name = "BATCH_NUMBER")
    private String batchNo;
    @Column(name = "CURRENCY_TYPE")
    private String currencyType;
    @ManyToOne
    @JoinColumn(name = "GAME_ID")
    private Game game;
    // By default the PIN is encrypted by DES.
    @Transient
    private String plainPin;

    /**
     * Decrypt PIN and set it into plain PIN.
     */
    public void decryptPin() throws ApplicationException {
        try {
            byte[] plain = DesCipher
                    .decrypt(Base64Coder.decode(this.getPin()), Base64Coder.decode(VOUCHER_PIN_DES_KEY));
            this.setPlainPin(new String(plain));
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public BigDecimal getFaceAmount() {
        return faceAmount;
    }

    public void setFaceAmount(BigDecimal faceAmount) {
        this.faceAmount = faceAmount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getPlainPin() {
        return plainPin;
    }

    public void setPlainPin(String plainPin) {
        this.plainPin = plainPin;
    }

}
