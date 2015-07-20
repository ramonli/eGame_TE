package com.mpos.lottery.te.merchant.domain;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "MERCHANT")
public class Merchant implements java.io.Serializable {
    private static final long serialVersionUID = 6139641558287268977L;
    // The identifier of top(super) merchant.
    public static final long SUPER_MERCHANT_ID = 1;
    public static final String MERCHANT_DELIMITER = ",";
    // Completely no idea why there are 4 kinds of credit type: credit and
    // prepaid?? anyway in Nigeria project only 1 and 4 will be used.
    public static int CREDIT_TYPE_DEFINITIVEVALUE = 1;
    public static int CREDIT_TYPE_PREPAID = 2;
    public static int CREDIT_TYPE_PRESALE = 3;
    public static int CREDIT_TYPE_USE_PARENT = 4;

    @Id
    @Column(name = "MERCHANT_ID")
    private long id;

    @Column(name = "MERCHANT_NAME")
    private String name;

    @Column(name = "MERCHANT_CODE")
    private String code;

    @Column(name = "SALE_BALANCE")
    private BigDecimal saleCreditLevel = new BigDecimal("0");

    @Column(name = "PAYOUT_BALANCE")
    private BigDecimal payoutCreditLevel = new BigDecimal("0");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID", nullable = true)
    private Merchant parentMerchant;

    @Column(name = "IS_DISTRIBUTE")
    private boolean distributor;

    /**
     * If ticket is sold by distributor A, the payout must be performed under distributor A.
     */
    @Column(name = "DISTRIBUTOR_LIMITATION")
    private boolean salePayoutUnderSameDistributor;

    @Column(name = "STATUS")
    private int status; // refer to Operator.status

    /*
     * Suppose there are A->B->C->D merchants relationship(D's parent is C, C's parent is B, and so forth), and current
     * merchant'id is 'D', then parentMerchants will be 'A,B,C'. The left first parent(A) is a virtual parent, do NOT do
     * any manipulation on it.
     */
    @Column(name = "MERCHANTS")
    private String parentMerchants;

    // How many numbers can be chose when is multiple bet option. 0 means no
    // limit.
    @Column(name = "MAX_MULTIPLE")
    private int maxMultipleBets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BD_PRIZE_GROUP_ID", nullable = true)
    private PrizeGroup prizeGroup;

    @Column(name = "MULTI_DRAW")
    private int allowedMultiDraw;

    // refer to CREDIT_TYPE_XXX
    @Column(name = "LIMIT_TYPE")
    private int creditType;

    @Column(name = "RETAILER_TARGET_BETS")
    private int incentiveTarget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BD_CASH_GROUP_ID", nullable = true)
    private PrizeGroup cashoutGroup; // payout group

    @Column(name = "CASH_OUT_DAY_LEVEL")
    private BigDecimal dailyCashoutLevel = new BigDecimal("0");

    /**
     * Whether need to deduct sale credit when player buy ticket with credit card.
     */
    @Column(name = "CREDIT_CARD_NEED")
    private boolean deductSaleByCreditCard;

    @Column(name = "TAX_ID")
    private String taxNo;

    @Column(name = "MAX_OFFLINE_TICKETS")
    private long maxOfflineTickets;

    @Column(name = "CASHOUT_BALANCE")
    private BigDecimal cashoutBalance = new BigDecimal("0");

    @Column(name = "COMMISION_BALANCE")
    private BigDecimal commisionBalance = new BigDecimal("0");

    @Column(name = "TOPUP_RATE")
    private BigDecimal topupReat = new BigDecimal("0");

    @Column(name = "CASHOUT_RATE")
    private BigDecimal cashoutRate = new BigDecimal("0");

    @Column(name = "UPDATE_TIME", nullable = true)
    private Timestamp updateTime = new Timestamp(System.currentTimeMillis());

    @Transient
    private OperatorCommission operatorCommission;
    @Transient
    private MerchantCommission merchantCommission;

    /**
     * Lookup distributor of a retailer. Theoretically each retailer must under a distributor.
     * 
     * @return the distributor, or null if no found.
     */
    public Merchant lookupDistributor() {
        if (this.getId() == SUPER_MERCHANT_ID) {
            return null;
        }
        if (this.isDistributor()) {
            return this;
        } else {
            return this.getParentMerchant().lookupDistributor();
        }
    }

    public void verifyActiveStatusRecursively() throws ApplicationException {
        if (this.getId() == SUPER_MERCHANT_ID) {
            return;
        }
        if (Operator.STATUS_ACTIVE != this.getStatus()) {
            {
                throw new ApplicationException(SystemException.CODE_MERCHANT_INACTIVE, "merchant(id=" + this.getId()
                        + ") is not active.");
            }
        } else {
            this.getParentMerchant().verifyActiveStatusRecursively();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getParentMerchants() {
        return parentMerchants;
    }

    public void setParentMerchants(String parentMerchants) {
        this.parentMerchants = parentMerchants;
    }

    public Merchant getParentMerchant() {
        return parentMerchant;
    }

    public void setParentMerchant(Merchant parentMerchant) {
        this.parentMerchant = parentMerchant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMaxMultipleBets() {
        return maxMultipleBets;
    }

    public void setMaxMultipleBets(int maxMultipleBets) {
        this.maxMultipleBets = maxMultipleBets;
    }

    public PrizeGroup getPrizeGroup() {
        return prizeGroup;
    }

    public void setPrizeGroup(PrizeGroup prizeGroup) {
        this.prizeGroup = prizeGroup;
    }

    public int getAllowedMultiDraw() {
        return allowedMultiDraw;
    }

    public void setAllowedMultiDraw(int allowedMultiDraw) {
        this.allowedMultiDraw = allowedMultiDraw;
    }

    public int getIncentiveTarget() {
        return incentiveTarget;
    }

    public void setIncentiveTarget(int incentiveTarget) {
        this.incentiveTarget = incentiveTarget;
    }

    public BigDecimal getSaleCreditLevel() {
        return saleCreditLevel == null ? new BigDecimal("0") : saleCreditLevel;
    }

    public void setSaleCreditLevel(BigDecimal saleCreditLevel) {
        this.saleCreditLevel = saleCreditLevel;
    }

    public BigDecimal getPayoutCreditLevel() {
        return payoutCreditLevel == null ? new BigDecimal("0") : payoutCreditLevel;
    }

    public void setPayoutCreditLevel(BigDecimal payoutCreditLevel) {
        this.payoutCreditLevel = payoutCreditLevel;
    }

    public PrizeGroup getCashoutGroup() {
        return cashoutGroup;
    }

    public void setCashoutGroup(PrizeGroup cashoutGroup) {
        this.cashoutGroup = cashoutGroup;
    }

    public BigDecimal getDailyCashoutLevel() {
        return this.dailyCashoutLevel == null ? new BigDecimal("0") : this.dailyCashoutLevel;
    }

    public void setDailyCashoutLevel(BigDecimal dailyCashoutLevel) {
        this.dailyCashoutLevel = dailyCashoutLevel;
    }

    public int getCreditType() {
        return creditType;
    }

    public void setCreditType(int creditType) {
        this.creditType = creditType;
    }

    public OperatorCommission getOperatorCommission() {
        return operatorCommission;
    }

    public void setOperatorCommission(OperatorCommission operatorCommission) {
        this.operatorCommission = operatorCommission;
    }

    public MerchantCommission getMerchantCommission() {
        return merchantCommission;
    }

    public void setMerchantCommission(MerchantCommission merchantCommission) {
        this.merchantCommission = merchantCommission;
    }

    public boolean isDeductSaleByCreditCard() {
        return deductSaleByCreditCard;
    }

    public void setDeductSaleByCreditCard(boolean deductSaleByCreditCard) {
        this.deductSaleByCreditCard = deductSaleByCreditCard;
    }

    public boolean isDistributor() {
        return distributor;
    }

    public void setDistributor(boolean distributor) {
        this.distributor = distributor;
    }

    public String getTaxNo() {
        return taxNo;
    }

    public void setTaxNo(String taxNo) {
        this.taxNo = taxNo;
    }

    public boolean isSalePayoutUnderSameDistributor() {
        return salePayoutUnderSameDistributor;
    }

    public void setSalePayoutUnderSameDistributor(boolean salePayoutUnderSameDistributor) {
        this.salePayoutUnderSameDistributor = salePayoutUnderSameDistributor;
    }

    /**
     * The merchant with given identifier is the parent of current merchant? If current merchant has the same identifier
     * with the given <code>merchantId</code>, true will be returned. This method can only be ran in JPA context.
     */
    public boolean isParent(long merchantId) {
        if (merchantId == SUPER_MERCHANT_ID) {
            return true;
        }

        Merchant parent = this;
        while (true) {
            if (parent.getId() == merchantId) {
                return true;
            } else {
                parent = parent.getParentMerchant();
                if (parent == null || parent.getId() == SUPER_MERCHANT_ID) {
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Merchant [id=" + id + ", name=" + name + ", saleCreditLevel=" + saleCreditLevel
                + ", payoutCreditLevel=" + payoutCreditLevel + ", creditType=" + creditType + ", cashoutBalance="
                + cashoutBalance + ", commisionBalance=" + commisionBalance + "]";
    }

    public long getMaxOfflineTickets() {
        return maxOfflineTickets;
    }

    public void setMaxOfflineTickets(long maxOfflineTickets) {
        this.maxOfflineTickets = maxOfflineTickets;
    }

    public BigDecimal getCashoutBalance() {
        return cashoutBalance;
    }

    public void setCashoutBalance(BigDecimal cashoutBalance) {
        this.cashoutBalance = cashoutBalance;
    }

    public BigDecimal getCommisionBalance() {
        return commisionBalance;
    }

    public void setCommisionBalance(BigDecimal commisionBalance) {
        this.commisionBalance = commisionBalance;
    }

    public BigDecimal getTopupReat() {
        return topupReat;
    }

    public void setTopupReat(BigDecimal topupReat) {
        this.topupReat = topupReat;
    }

    public BigDecimal getCashoutRate() {
        return cashoutRate;
    }

    public void setCashoutRate(BigDecimal cashoutRate) {
        this.cashoutRate = cashoutRate;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }
}
