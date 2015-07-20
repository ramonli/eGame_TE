package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "IG_GAME_INSTANCE")
public class InstantGameDraw {
    public static final int VALIDATION_TYPE_VIRN = 0;
    public static final int VALIDATION_TYPE_EGAME = 1;

    @Id
    @Column(name = "IG_GAME_INSTANCE_ID")
    private String id;

    @Column(name = "GAME_INSTANCE_NAME")
    private String name;

    @Column(name = "START_ACTIVATION_TIME")
    private Date startActivationTime;

    @Column(name = "STOP_ACTIVATION_TIME")
    private Date stopActivationTime;

    @Column(name = "STOP_PAYOUT_TIME")
    private Date stopPayoutTime;

    @Column(name = "STATUS")
    private int status; // refer to GameDraw.status

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_ID", nullable = false)
    private Game game;

    @Column(name = "IS_SUSPEND_PAYOUT")
    private int isSuspendPayoutBlocked;

    @Column(name = "IS_SUSPEND_ACTIVATION", nullable = true)
    private int isSuspendActiveBlocked;

    @Column(name = "VALIDATION_TYPE")
    private int validationType;

    @Column(name = "FACE_VALUE")
    private BigDecimal ticketFaceValue;

    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String prizeLogicId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartActivationTime() {
        return startActivationTime;
    }

    public void setStartActivationTime(Date startActivationTime) {
        this.startActivationTime = startActivationTime;
    }

    public Date getStopActivationTime() {
        return stopActivationTime;
    }

    public void setStopActivationTime(Date stopActivationTime) {
        this.stopActivationTime = stopActivationTime;
    }

    public Date getStopPayoutTime() {
        return stopPayoutTime;
    }

    public void setStopPayoutTime(Date stopPayoutTime) {
        this.stopPayoutTime = stopPayoutTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getValidationType() {
        return validationType;
    }

    public void setValidationType(int validationType) {
        this.validationType = validationType;
    }

    public BigDecimal getTicketFaceValue() {
        return ticketFaceValue;
    }

    public void setTicketFaceValue(BigDecimal ticketFaceValue) {
        this.ticketFaceValue = ticketFaceValue;
    }

    public int getIsSuspendActiveBlocked() {
        return isSuspendActiveBlocked;
    }

    public void setIsSuspendActiveBlocked(int isSuspendActiveBlocked) {
        this.isSuspendActiveBlocked = isSuspendActiveBlocked;
    }

    public int getIsSuspendPayoutBlocked() {
        return isSuspendPayoutBlocked;
    }

    public void setIsSuspendPayoutBlocked(int isSuspendPayoutBlocked) {
        this.isSuspendPayoutBlocked = isSuspendPayoutBlocked;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
