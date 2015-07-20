package com.mpos.lottery.te.gamespec.game;

import com.google.protobuf.Message;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessage;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessageSerializer;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * A base game instance for all game types.
 * 
 * @author Ramon Li
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class BaseGameInstance implements java.io.Serializable, TeTransactionMessageSerializer {
    public static final int STATE_NEW = 1; // New
    public static final int STATE_ACTIVE = 2; // Active
    public static final int STATE_INACTIVE = 3; // Inactive
    public static final int STATE_STOP = 4; // Stop Sale
    public static final int STATE_RESULT_ENTERED = 5; // Result Entered
    public static final int STATE_RESULT_AUDITED = 6; // Result Audited
    public static final int STATE_PAYOUT_STARTED = 7; // Payout Started
    // public static final int STATE_PAYOUT_BLOCKED = 8; // Payout Blocked
    public static final int STATE_WINANALYSIS_STARTED = 9;

    public static final int RISKCONTROL_METHOD_MAX_LOSS = 1;
    public static final int RISKCONTROL_METHOD_DYNAMIC = 2;

    @Id
    @Column(name = "ID")
    // // create seqence TE_SEQ start with 1 increment by 1;
    // @SequenceGenerator(name="TE_SEQ", sequenceName="TE_SEQ")
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="TE_SEQ")
    private String id;

    @Column(name = "VERSION")
    private long version;

    @Column(name = "DRAW_DATE")
    private Date drawDate;

    @Column(name = "DRAW_NO")
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_ID", nullable = false)
    // explicitly require underlying hibernate to generate SQL with 'join'.
    // @Fetch(FetchMode.JOIN)
    private Game game;

    @Column(name = "START_SELLING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date beginTime;

    @Column(name = "GAME_FREEZING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date freezeTime;

    @Column(name = "STOP_SELLING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "GAME_INSTANCE_NAME")
    private String name;

    @Column(name = "MAX_CLAIM_PERIOD")
    private int maxClaimDays;

    @Column(name = "BD_CHANNEL_SETTING_ID")
    private String gameChannelSettingId;

    @Column(name = "STATUS")
    private int state;

    @Column(name = "IS_SUSPEND_PAYOUT")
    private boolean payoutBlocked;

    @Column(name = "IS_SUSPEND_MANUAL_CANCEL")
    private boolean suspendManualCancel;

    @Column(name = "IS_SUSPEND_SALE")
    private boolean saleSuspended;

    @Column(name = "CONTORL_METHOD")
    private int riskControlMethod;

    @Column(name = "SALES_AMOUNT_PERCENT")
    private BigDecimal percentageOfTurnover = new BigDecimal("0");

    @Column(name = "LOSS_AMOUNT")
    private BigDecimal maxLossAmount = new BigDecimal("0");

    // Only for DTO usage
    @Transient
    private String gameId;
    @Transient
    private Integer gameType;

    /**
     * Convert a game instance into DTO.
     */
    public GameInstanceDto toDto() {
        GameInstanceDto dto = new GameInstanceDto();
        dto.setGameId(this.game == null ? this.getGameId() : this.getGame().getId());
        dto.setNumber(this.number);
        this.customizeToDto(dto);
        return dto;
    }

    protected void customizeToDto(GameInstanceDto dto) {
        // template method for subclass to customize the process.
    }

    public BaseGameResult getGameResult() {
        // template method for subclass to implement
        return null;
    }

    /**
     * check if current time is after last claim day.
     */
    public void isPastLastClaimDay() throws ApplicationException {
        Calendar c = Calendar.getInstance();
        c.setTime(this.getEndTime());
        // c.set(Calendar.HOUR_OF_DAY, 0);
        // c.set(Calendar.MINUTE, 0);
        // c.set(Calendar.SECOND, 0);
        c.add(Calendar.DAY_OF_MONTH, this.getMaxClaimDays());
        if (new Date().after(c.getTime())) {
            throw new ApplicationException(SystemException.CODE_EXCEED_LAST_CLAIMTIME,
                    "Current time has passed last payout time(" + c.getTime() + "), can NOT payout.");
        }
    }

    /**
     * Whether a sale of given game instance can be cancelled. Return true is cancellation allowed, otherwise
     * false(cancel decline).
     */
    public boolean canCancelNormally() throws ApplicationException {
        boolean isCancelDeclined = false;
        // check the status of current game draw
        Date now = new Date();
        if (now.before(this.getFreezeTime())) {
            if (this.getState() == LottoGameInstance.STATE_INACTIVE) {
                throw new ApplicationException(SystemException.CODE_NOT_ACTIVE_DRAW, "Game instance(number="
                        + this.getNumber() + ",gameId=" + this.getGameId() + ") is not active.");
            }
            if (this.getState() >= LottoGameInstance.STATE_PAYOUT_STARTED) {
                // TODO: reject canceling request
            }
        } else {
            isCancelDeclined = true;
        }
        return !isCancelDeclined;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxClaimDays() {
        return maxClaimDays;
    }

    public void setMaxClaimDays(int maxClaimDays) {
        this.maxClaimDays = maxClaimDays;
    }

    public Date getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(Date freezeTime) {
        this.freezeTime = freezeTime;
    }

    public String getKey() {
        if (this.number == null || this.game == null) {
            throw new IllegalStateException("Either this.number or this.gameId is null.");
        }
        return new StringBuffer(this.number).append("-").append(this.game.getId()).toString();
    }

    public Date getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(Date drawDate) {
        this.drawDate = drawDate;
    }

    public boolean isPayoutBlocked() {
        return payoutBlocked;
    }

    public void setPayoutBlocked(boolean payoutBlocked) {
        this.payoutBlocked = payoutBlocked;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getRiskControlMethod() {
        return riskControlMethod;
    }

    public void setRiskControlMethod(int riskControlMethod) {
        this.riskControlMethod = riskControlMethod;
    }

    public BigDecimal getPercentageOfTurnover() {
        return percentageOfTurnover;
    }

    public void setPercentageOfTurnover(BigDecimal percentageOfTurnover) {
        this.percentageOfTurnover = percentageOfTurnover;
    }

    public BigDecimal getMaxLossAmount() {
        return maxLossAmount;
    }

    public void setMaxLossAmount(BigDecimal maxLossAmount) {
        this.maxLossAmount = maxLossAmount;
    }

    public String getGameChannelSettingId() {
        return gameChannelSettingId;
    }

    public void setGameChannelSettingId(String gameChannelSettingId) {
        this.gameChannelSettingId = gameChannelSettingId;
    }

    public boolean isSuspendManualCancel() {
        return suspendManualCancel;
    }

    public void setSuspendManualCancel(boolean suspendManualCancel) {
        this.suspendManualCancel = suspendManualCancel;
    }

    public boolean isSaleSuspended() {
        return saleSuspended;
    }

    public void setSaleSuspended(boolean saleSuspended) {
        this.saleSuspended = saleSuspended;
    }

    @Override
    public Message toProtoMessage(Context respCtx) {
        return TeTransactionMessage.GameInstance.newBuilder().setId(this.getId()).build();
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    /**
     * Generate a instance of BaseGameInstance to easy castor mapping, for example map the response of prize enquiry.
     */
    public BaseGameInstance genCastorBaseGameInstance() {
        BaseGameInstance gameInstance = new BaseGameInstance();
        gameInstance.setGameId(this.getGame().getId());
        gameInstance.setGameType(this.getGame().getType());
        gameInstance.setNumber(this.getNumber());
        return gameInstance;
    }
}
