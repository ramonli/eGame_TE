package com.mpos.lottery.te.config;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SYS_CONFIGURATION")
public class SysConfiguration {
    public static final int MANUAL_SETTLEMENT_MODE_LOCK = 0;
    public static final int MANUAL_SETTLEMENT_MODE_PASS = 1;
    public static final int SERVER_TYPE_MASTER = 1;
    public static final int SERVER_TYPE_SLAVE = 2;

    @Id
    @Column(name = "ID")
    private String id;

    // define the mode for handling manual settled: 0 freeze pos until
    // mantainment service,
    // 1 allow pos to start new batch.
    @Column(name = "MANUAL_SETTLEMENT_HANDLING")
    private int manualSettlementHandlingMode;

    // If customer need input a PIN when buy a ticket.
    @Column(name = "PIN_INPUT_MODE")
    private boolean isNeedInputPin;

    @Column(name = "LICENCE_FILE_PATH")
    private String licenseFilePath;

    @Column(name = "PRIZE_AMOUNT_NECESSARY")
    private boolean needInputAmount;

    @Column(name = "IS_ENCRPT_SERIALNO")
    private boolean encryptSerialNo;

    @Column(name = "SERIALNO_PUBLIC_KEY_PATH")
    private String serialNoPublicKeyPath;

    // refer to SERVER_TYPE_XXX
    @Column(name = "SERVER_TYPE")
    private int serverType;

    @Column(name = "PAYOUT_INCREASE_CREDIT")
    private boolean restoreCreditLevelWhenPayout;

    @Column(name = "RETAILER_TARGET_BETS")
    private int incentiveTarget;

    @Column(name = "INCENTIVE_IS_ENABLE")
    private boolean incentiveEnabled;

    @Column(name = "MAX_DAYS_ACTIVE_RPT_ENQ")
    private long maxAllowedDaysOfActivityReport;

    @Column(name = "IS_DISPLAY_VALIDATION_CODE")
    private boolean genValidationCode;

    @Column(name = "SUPPORT_CIMMISSION_CALCULATION")
    private boolean supportCommissionCalculation;

    @Column(name = "APP_KEY")
    private String appKey;

    @Column(name = "MAXIUM_TIMES_OF_CASHOUT_PASS")
    private int maxiumTimesOfCashoutPass;

    @Column(name = "MAX_EXPIRE_TIME_CASHOUT_PASS")
    private int maxExpireTimeCashoutPass;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getManualSettlementHandlingMode() {
        return manualSettlementHandlingMode;
    }

    public void setManualSettlementHandlingMode(int manualSettlementHandlingMode) {
        this.manualSettlementHandlingMode = manualSettlementHandlingMode;
    }

    public boolean isNeedInputPin() {
        return isNeedInputPin;
    }

    public void setNeedInputPin(boolean isNeedInputPin) {
        this.isNeedInputPin = isNeedInputPin;
    }

    public String getLicenseFilePath() {
        return licenseFilePath;
    }

    public void setLicenseFilePath(String licenseFilePath) {
        this.licenseFilePath = licenseFilePath;
    }

    public boolean isNeedInputAmount() {
        return needInputAmount;
    }

    public void setNeedInputAmount(boolean needInputAmount) {
        this.needInputAmount = needInputAmount;
    }

    public boolean isEncryptSerialNo() {
        return encryptSerialNo;
    }

    public void setEncryptSerialNo(boolean encryptSerialNo) {
        this.encryptSerialNo = encryptSerialNo;
    }

    public String getSerialNoPublicKeyPath() {
        return serialNoPublicKeyPath;
    }

    public void setSerialNoPublicKeyPath(String serialNoPublicKeyPath) {
        this.serialNoPublicKeyPath = serialNoPublicKeyPath;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

    public boolean isRestoreCreditLevelWhenPayout() {
        return restoreCreditLevelWhenPayout;
    }

    public void setRestoreCreditLevelWhenPayout(boolean restoreCreditLevelWhenPayout) {
        this.restoreCreditLevelWhenPayout = restoreCreditLevelWhenPayout;
    }

    public int getIncentiveTarget() {
        return incentiveTarget;
    }

    public void setIncentiveTarget(int incentiveTarget) {
        this.incentiveTarget = incentiveTarget;
    }

    public boolean isIncentiveEnabled() {
        return incentiveEnabled;
    }

    public void setIncentiveEnabled(boolean incentiveEnabled) {
        this.incentiveEnabled = incentiveEnabled;
    }

    public long getMaxAllowedDaysOfActivityReport() {
        return maxAllowedDaysOfActivityReport;
    }

    public void setMaxAllowedDaysOfActivityReport(long maxAllowedDaysOfActivityReport) {
        this.maxAllowedDaysOfActivityReport = maxAllowedDaysOfActivityReport;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public boolean isGenValidationCode() {
        return genValidationCode;
    }

    public void setGenValidationCode(boolean genValidationCode) {
        this.genValidationCode = genValidationCode;
    }

    public int getMaxiumTimesOfCashoutPass() {
        return maxiumTimesOfCashoutPass;
    }

    public void setMaxiumTimesOfCashoutPass(int maxiumTimesOfCashoutPass) {
        this.maxiumTimesOfCashoutPass = maxiumTimesOfCashoutPass;
    }

    public int getMaxExpireTimeCashoutPass() {
        return maxExpireTimeCashoutPass;
    }

    public void setMaxExpireTimeCashoutPass(int maxExpireTimeCashoutPass) {
        this.maxExpireTimeCashoutPass = maxExpireTimeCashoutPass;
    }

    public boolean isSupportCommissionCalculation() {
        return supportCommissionCalculation;
    }

    public void setSupportCommissionCalculation(boolean supportCommissionCalculation) {
        this.supportCommissionCalculation = supportCommissionCalculation;
    }

}
