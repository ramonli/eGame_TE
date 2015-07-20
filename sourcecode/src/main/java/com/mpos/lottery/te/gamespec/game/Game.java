package com.mpos.lottery.te.gamespec.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "GAME")
public class Game<T> implements java.io.Serializable {
    private static final long serialVersionUID = -5049425494540471730L;
    public static final int TYPE_UNDEF = GameType.UNDEF.getType();
    public static final int TYPE_VAT = GameType.VAT.getType();
    public static final int TYPE_LOTT = GameType.LOTTO.getType();
    public static final int TYPE_SOCCER = GameType.SOCCER.getType();
    public static final int TYPE_RACING = GameType.RACING.getType();
    public static final int TYPE_INSTANT = GameType.IG.getType();
    public static final int TYPE_TOTO = GameType.TOTO.getType();
    public static final int TYPE_UNION = GameType.UNION.getType();
    public static final int TYPE_BINGO = GameType.BINGO.getType();
    public static final int TYPE_DIGITAL = GameType.DIGITAL.getType();
    public static final int TYPE_VOUCHER = GameType.VOUCHER.getType();
    public static final int TYPE_EXTRABALL = GameType.EXTRABALL.getType();
    public static final int TYPE_RAFFLE = GameType.RAFFLE.getType();
    public static final int TYPE_LFN = GameType.LFN.getType();
    public static final int TYPE_LUCKYNUMBER = GameType.LUCKYNUMBER.getType();

    // public static final int TYPE_NUMBER = 5; // Number game, such as 3-D, 4-D
    public static final int TAXMETHOD_ANALYSIS = 1;
    public static final int TAXMETHOD_PAYOUT = 2;
    public static final int TAXMETHOD_BASE_BET = 1;
    public static final int TAXMETHOD_BASE_TICKET = 2;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;

    @Id
    @Column(name = "GAME_ID")
    private String id;

    @Column(name = "GAME_TYPE_ID")
    private int type;

    @Column(name = "FUNDAMENTAL_TYPE_ID")
    private String funTypeId;

    @Column(name = "OPERATION_PARAMETERS_ID")
    private String operatorParameterId;

    @Column(name = "GAME_NAME")
    private String name;

    @Column(name = "WINNER_TAX_POLICY_ID")
    private String taxPolicyId;

    @Column(name = "TAX_CALCULATION_METHOD")
    private Integer taxMethod;

    /**
     * refer to {@code Game#TAXMETHOD_BASE_BET and Game#TAXMETHOD_BASE_TICKET}
     */
    @Column(name = "TAX_CALCULATION_BASED")
    private int taxMethodBase;

    // refer to STATUS_XXX
    @Column(name = "STATUS")
    private int state;

    @Column(name = "LEGAL_NUM")
    private String legalNum;

    @Column(name = "TICKET_LOGO")
    private String ticketLogo;

    @Column(name = "NEED_PAYOUT")
    private boolean needAutoPayout;

    // @Column(name = "EB_BALLS_INFO_ID")
    @Transient
    private String ballInfoId;

    @Transient
    private T funType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFunTypeId() {
        return funTypeId;
    }

    public void setFunTypeId(String fundamentalId) {
        this.funTypeId = fundamentalId;
    }

    public String getOperatorParameterId() {
        return operatorParameterId;
    }

    public void setOperatorParameterId(String operatorParameterId) {
        this.operatorParameterId = operatorParameterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getFunType() {
        return funType;
    }

    public void setFunType(T funType) {
        this.funType = funType;
    }

    public String getTaxPolicyId() {
        return taxPolicyId;
    }

    public void setTaxPolicyId(String taxPolicyId) {
        this.taxPolicyId = taxPolicyId;
    }

    public Integer getTaxMethod() {
        return taxMethod;
    }

    public void setTaxMethod(Integer taxMethod) {
        this.taxMethod = taxMethod;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Integer getTaxMethodBase() {
        return taxMethodBase;
    }

    public void setTaxMethodBase(Integer taxMethodBase) {
        this.taxMethodBase = taxMethodBase;
    }

    /**
     * @return return legalNum
     */
    public String getLegalNum() {
        return legalNum;
    }

    public void setLegalNum(String legalNum) {
        this.legalNum = legalNum;
    }

    /**
     * @return return ticketLogo
     */
    public String getTicketLogo() {
        return ticketLogo;
    }

    public void setTicketLogo(String ticketLogo) {
        this.ticketLogo = ticketLogo;
    }

    public String getBallInfoId() {
        return ballInfoId;
    }

    public void setBallInfoId(String ballInfoId) {
        this.ballInfoId = ballInfoId;
    }

    public boolean isNeedAutoPayout() {
        return needAutoPayout;
    }

    public void setNeedAutoPayout(boolean needAutoPayout) {
        this.needAutoPayout = needAutoPayout;
    }

}
