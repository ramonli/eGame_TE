package com.mpos.lottery.te.gameimpl.lfn.game;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "LFN_GAME_INSTANCE")
public class LfnGameInstance extends BaseGameInstance {

    private static final long serialVersionUID = -8644223823742264109L;

    @Column(name = "PAYOUT_START_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date payoutStartTime;

    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String prizeLogicId;

    @Transient
    private LfnGameResult gameResult;

    /**
     * check if current time is after last claim day.
     */
    @Override
    public void isPastLastClaimDay() throws ApplicationException {
        Calendar c = Calendar.getInstance();
        c.setTime(this.getPayoutStartTime());
        // c.set(Calendar.HOUR_OF_DAY, 0);
        // c.set(Calendar.MINUTE, 0);
        // c.set(Calendar.SECOND, 0);
        c.add(Calendar.DAY_OF_MONTH, getMaxClaimDays());
        if (new Date().after(c.getTime())) {
            throw new ApplicationException(SystemException.CODE_EXCEED_LAST_CLAIMTIME,
                    "Current time has passed last payout time(" + c.getTime() + "), can NOT payout.");
        }
    }

    public Date getPayoutStartTime() {
        return payoutStartTime;
    }

    public void setPayoutStartTime(Date payoutStartTime) {
        this.payoutStartTime = payoutStartTime;
    }

    @Override
    public LfnGameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(LfnGameResult gameResult) {
        this.gameResult = gameResult;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
