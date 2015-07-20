package com.mpos.lottery.te.merchant.service.impl;

import com.google.gson.Gson;

import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.DateUtils;
import com.mpos.lottery.te.common.util.SecurityMeasurements;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.config.dao.SysConfigurationDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeGroupItemDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.CashoutPassDao;
import com.mpos.lottery.te.merchant.dao.MerchantCommissionDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.CashoutPass;
import com.mpos.lottery.te.merchant.domain.GsonCashOutOperator;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.MerchantCommission;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;
import com.mpos.lottery.te.merchant.domain.PrizeGroup;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.web.CashOutByManualDto;
import com.mpos.lottery.te.merchant.web.CashOutByOperatorPassDto;
import com.mpos.lottery.te.merchant.web.CashOutPassDto;
import com.mpos.lottery.te.merchant.web.PayoutLevelAllowRequest;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.dao.TransactionMessageDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionMessage;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MerchantServiceImpl implements MerchantService {
    private Log logger = LogFactory.getLog(MerchantServiceImpl.class);
    private MerchantDao merchantDao;
    private MerchantCommissionDao merchantCommissionDao;
    private PrizeGroupItemDao prizeGroupItemDao;
    private UUIDService uuidManager;
    private OperatorDao operatorDao;
    private OperatorMerchantDao operatorMerchantDao;
    private CashoutPassDao cashoutPassDao;
    private SysConfigurationDao sysConfigurationDao;
    private BalanceTransactionsDao balanceTransactionsDao;
    private TransactionMessageDao transMessageDao;

    @Override
    public void allowCashout(Context respCtx, BigDecimal actualCashout) throws ApplicationException {
        this.allowCashoutByLimit(respCtx, actualCashout);
        this.allowDailyCashout(respCtx, actualCashout);
    }

    @Override
    public void allowSale(Transaction context, BaseTicket ticket) throws ApplicationException {
        MerchantCommission comm = this.getMerchantCommissionDao().getByMerchantAndGame(context.getMerchantId(),
                ticket.getGameInstance().getGame().getId());
        if (comm == null || !comm.isAllowSale()) {
            throw new SystemException(SystemException.CODE_OPERATOR_SELL_NOPRIVILEDGE, "operator(id="
                    + context.getOperatorId() + ") has no priviledge to sell ticket of game '"
                    + ticket.getGameInstance().getGame().getId() + "', allocate the game to its merchant first.");
        }
    }

    @Override
    public void allowPayout(Context respCtx, Game game, PayoutLevelAllowRequest[] levelAllowRequests,
            BigDecimal actualPayout) throws ApplicationException {
        // verify whether the game has been allocated
        MerchantCommission comm = this.getMerchantCommissionDao().getByMerchantAndGame(
                respCtx.getTransaction().getMerchantId(), game.getId());
        if (comm == null || !comm.isAllowPayout()) {
            throw new SystemException(SystemException.CODE_OPERATOR_PAYOUT_NOPRIVILEDGE, "operator(id="
                    + respCtx.getOperatorId() + ") has no priviledge to payout ticket of game '" + game.getId()
                    + "', allocate the game to its merchant(id=" + respCtx.getTransaction().getMerchantId()
                    + ") first.");
        }
        GameType gameType = GameType.fromType(game.getType());
        // only need to check the prize group constraints of operator
        PrizeGroup prizeGroup = respCtx.getOperator().getPrizeGroup();
        if (!prizeGroup.isPayoutAllowed()) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_PAY, "Operator("
                    + respCtx.getOperator() + ") doesn't allow payout.");
        }
        // ** both conditions must be satisfied
        // by payout limit
        if (new BigDecimal("0").compareTo(actualPayout) != 0) {
            this.verifyPayoutByLimit(respCtx, actualPayout, respCtx.getOperator());
        }
        // for odds and fix-prize game, no need to check prize level.
        if (!gameType.isFixedPrize()) {
            // by prize level
            if (levelAllowRequests != null) {
                for (PayoutLevelAllowRequest levelAllowRequest : levelAllowRequests) {
                    this.verifyPayoutByLevel(respCtx, respCtx.getOperator(),
                            levelAllowRequest.getRequestedPrizeLevels(), levelAllowRequest.getGameType(),
                            levelAllowRequest.getPrizeGroupType());
                }
            }
        }
    }

    @Override
    public Merchant getMerchant(long merchantId) throws ApplicationException {
        return this.getMerchantDao().findById(Merchant.class, merchantId);
    }

    public void update(Merchant merchant) throws ApplicationException {
        this.getMerchantDao().update(merchant);
    }

    @Override
    public Merchant getMerchantByOperator(String operatorId, boolean forUpdate) throws ApplicationException {
        OperatorMerchant operatorMerchant = this.getOperatorMerchantDao().findByOperator(operatorId);
        if (operatorMerchant == null) {
            throw new ApplicationException(SystemException.CODE_OPERATOR_NO_MERCHANT, "operator(id=" + operatorId
                    + ") doesn't belong to any merchant, allocate it first.");
        }

        Merchant merchant = null;
        if (!forUpdate) {
            merchant = this.getMerchantDao().findById(Merchant.class, operatorMerchant.getMerchantID());
        } else {
            merchant = this.getMerchantDao().findByIdForUpdate(operatorMerchant.getMerchantID());
        }
        if (merchant == null) {
            throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "merchant(id="
                    + operatorMerchant.getMerchantID() + ") doesn't exist.");
        }
        return merchant;
    }

    @Override
    public List<GameType> supportedGameType(long merchantId) throws ApplicationException {
        List<MerchantCommission> mcList = this.getMerchantCommissionDao().getByMerchant(merchantId);
        List<GameType> gameTypes = new LinkedList<GameType>();
        for (MerchantCommission mc : mcList) {
            Game game = mc.getGame();
            // remove duplicated item.
            if (game != null) {
                GameType gameType = GameType.fromType(game.getType());
                if (!gameTypes.contains(gameType)) {
                    gameTypes.add(gameType);
                }
            }
        }
        return gameTypes;
    }

    // ----------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------

    /**
     * When cash out, a merchant can only cashout a given max amount.
     * 
     * @param respCtx
     *            The context of cashout transaction.
     * @param actualCashout
     *            The actual amount of cashout.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    protected void allowDailyCashout(Context respCtx, BigDecimal actualCashout) throws ApplicationException {
        PrizeGroup cashoutGroup = respCtx.getOperator().getCashoutGroup();
        if (cashoutGroup == null) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_CASHOUT, "Operator("
                    + respCtx.getOperator() + ") can't perform cashout, no any cashout " + "group definition found.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Before cash out[dailyCashoutLevel: " + respCtx.getOperator().getDailyCashoutLevel()
                    + ", dailyCashoutLimit: " + cashoutGroup.getDailyCashoutLimit() + ", cashoutAmount: "
                    + actualCashout + "] of Operator( " + respCtx.getOperator() + ").");
        }
        if (actualCashout.add(respCtx.getOperator().getDailyCashoutLevel()).compareTo(
                cashoutGroup.getDailyCashoutLimit()) > 0) {
            throw new ApplicationException(SystemException.CODE_EXCEED_ALLOWED_MERCHANT_DAILY_CASHOUT_LIMIT,
                    "Cash out amount(" + actualCashout + ") + current cashout level("
                            + respCtx.getOperator().getDailyCashoutLevel() + ") has exceeded allowed limit("
                            + cashoutGroup.getDailyCashoutLimit() + ") of Operator(" + respCtx.getOperator() + ").");
        }
    }

    /**
     * When cash out, system check the cashout-limit of the merchant.
     * 
     * @param respCtx
     *            The context of cashout transaction.
     * @param actualCashout
     *            The actual amount of cashout.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    protected void allowCashoutByLimit(Context respCtx, BigDecimal actualCashout) throws ApplicationException {
        PrizeGroup cashoutGroup = respCtx.getOperator().getCashoutGroup();
        if (cashoutGroup == null) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_CASHOUT, "Operator("
                    + respCtx.getOperator() + ") can't perform cashout, no any cashout group definition found.");
        }
        if (cashoutGroup.getMaxPayoutAmount() != null
                && cashoutGroup.getMaxPayoutAmount().compareTo(new BigDecimal("0")) >= 0) {
            if (actualCashout.compareTo(cashoutGroup.getMaxPayoutAmount()) > 0) {
                throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_CASHOUT_SCOPE,
                        "The prize actual amount(" + actualCashout + ") exceed the max " + "allowed cashout amount("
                                + cashoutGroup.getMaxPayoutAmount() + ") of the Operator(" + respCtx.getOperator()
                                + ").");
            }
        }
        if (cashoutGroup.getMinPayoutAmount() != null
                && cashoutGroup.getMinPayoutAmount().compareTo(new BigDecimal("0")) >= 0) {
            if (actualCashout.compareTo(cashoutGroup.getMinPayoutAmount()) < 0) {
                throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_CASHOUT_SCOPE,
                        "The prize actual amount(" + actualCashout + ") is less than min cashout amount("
                                + cashoutGroup.getMinPayoutAmount() + ") of Operator(" + respCtx.getOperator() + ").");
            }
        }
    }

    /**
     * When do payout/validation, system check the payout-limit of the merchant. A merchant maybe follow its parent's
     * payout-limit setting, system should handle this case.
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param actualPayout
     *            The actual amount of payout.
     * @param operator
     *            The operator who do payout.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    private void verifyPayoutByLimit(Context respCtx, BigDecimal actualPayout, Operator operator)
            throws ApplicationException {
        PrizeGroup prizeGroup = this.determinePrizeGroup(respCtx, operator);
        if (PrizeGroup.ALLOWTYPE_UNLIMIT == prizeGroup.getAllowType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("the prize group definition(" + prizeGroup.getId() + ") is no limited");
            }
            return;
        }
        if (prizeGroup.getMaxPayoutAmount() != null
                && prizeGroup.getMaxPayoutAmount().compareTo(new BigDecimal("0")) >= 0) {
            if (actualPayout.compareTo(prizeGroup.getMaxPayoutAmount()) > 0) {
                throw new ApplicationException(SystemException.CODE_EXCEED_MAX_PAYOUT, "The prize actual amount("
                        + actualPayout + ") exceed the max " + "allowed payout amount("
                        + prizeGroup.getMaxPayoutAmount() + ").");
            }
        }
        if (prizeGroup.getMinPayoutAmount() != null
                && prizeGroup.getMinPayoutAmount().compareTo(new BigDecimal("0")) >= 0) {
            if (actualPayout.compareTo(prizeGroup.getMinPayoutAmount()) < 0) {
                throw new ApplicationException(SystemException.CODE_EXCEED_MAX_PAYOUT, "The prize actual amount("
                        + actualPayout + ") is less than min " + "payout amount(" + prizeGroup.getMinPayoutAmount()
                        + ").");
            }
        }
    }

    /**
     * This operation will check whether a merchant has privilege to payout a set of specific prize levels.
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param operator
     *            The operator who try to perform payout.
     * @param requestedPrizeLevels
     *            The requested prize levels which the merchant try to payout.
     * @param gameType
     *            The game type of prize.
     * @param groupType
     *            Refer to PrizeGroupItem.GROUP_TYPE_XXX
     * @throws ApplicationException
     *             if any of the requested prize level is rejected.
     */
    protected void verifyPayoutByLevel(Context respCtx, Operator operator, Set<Integer> requestedPrizeLevels,
            int gameType, int groupType) throws ApplicationException {
        List<PrizeGroupItem> prizeGroupItems = this.getPrizeGroupItemDao().findByGroupAndGameTypeAndGroupType(
                operator.getPrizeGroup().getId(), gameType, groupType);
        // if no any prize group items found for given groupType, merchant and
        // gameType, simply let the merchant perform payout.
        if (prizeGroupItems == null || prizeGroupItems.size() == 0) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_PAY,
                    "No any prize group definition found by (operatorId=" + operator.getId() + ",prizeGroupId="
                            + operator.getPrizeGroup().getId() + ",gameType=" + gameType + ",prizeGroupType="
                            + groupType + "), System will simply reject this payout request.");
        }
        for (int prizeLevel : requestedPrizeLevels) {
            if (!PrizeGroupItem.allow(prizeLevel, prizeGroupItems)) {
                throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_PAY, "Operator(" + operator
                        + ") has no priviledge to pay prize (level:" + prizeLevel + ",groupType=" + groupType
                        + ",gameType=" + gameType + ")");
            }
        }
    }

    /**
     * If the payout-limit of current merchant is 'follow parent', TE should query its parent merchant till find a
     * parent merchant whose payout-limit isn't 'follow parent'.
     * 
     * @return the PrizeGroup which will be applied at final.
     */
    private PrizeGroup determinePrizeGroup(Context respCtx, Operator operator) throws ApplicationException {
        PrizeGroup prizeGroup = operator.getPrizeGroup();
        if (prizeGroup == null) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_PAY,
                    "No any prize group definition found of Operator(" + operator
                            + "), System will simply reject this payout request.");
        }
        if (PrizeGroup.ALLOWTYPE_USEPARENT == prizeGroup.getAllowType()) {
            // lookup prize group definition of operator's parent merchant
            Merchant leafMerchant = this.getMerchantDao().findById(Merchant.class,
                    respCtx.getTransaction().getMerchantId());
            if (leafMerchant == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find merhcant by id="
                        + respCtx.getTransaction().getMerchantId());
            }
            prizeGroup = this.determinePrizeGroup(leafMerchant);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Use the payout-limit setting(max=" + prizeGroup.getMaxPayoutAmount() + ",min="
                        + prizeGroup.getMinPayoutAmount() + ") of Operator(" + operator + ") to verify payout limit.");
            }
        }
        return prizeGroup;
    }

    /**
     * Found the valid prize group definition recursively.
     */
    private PrizeGroup determinePrizeGroup(Merchant merchant) throws ApplicationException {
        if (Merchant.SUPER_MERCHANT_ID == merchant.getId()) {
            return null;
        }
        PrizeGroup prizeGroup = merchant.getPrizeGroup();
        if (prizeGroup == null) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_PAY,
                    "No any prize group definition found of Merchant(" + merchant
                            + "), System will simply reject this payout request.");
        }
        if (PrizeGroup.ALLOWTYPE_USEPARENT == prizeGroup.getAllowType()) {
            prizeGroup = this.determinePrizeGroup(merchant.getParentMerchant());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Use the payout-limit setting(max=" + prizeGroup.getMaxPayoutAmount() + ",min="
                        + prizeGroup.getMinPayoutAmount() + ") of merhcant(" + merchant + ") to verify payout limit.");
            }

            if (!prizeGroup.isPayoutAllowed()) {
                throw new ApplicationException(SystemException.CODE_MERCHANT_UNALLOWED_PAY, "Merchant(" + merchant
                        + ") doesn't allow payout.");
            }
        }

        return prizeGroup;
    }

    @Override
    public CashOutPassDto getCashoutPass(Context respCtx, CashOutPassDto cashoutDto) throws ApplicationException {
        String cashoutOperatorid = respCtx.getOperatorId();
        Operator operator = this.operatorDao.findById(Operator.class, cashoutOperatorid);
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "operator(id=" + cashoutOperatorid
                    + ") doesn't exist.");
        }

        // cashout amount must greater than 0
        if (cashoutDto.getAmount().doubleValue() <= 0) {
            throw new ApplicationException(SystemException.CODE_CASHOUT_AMOUNT_LESSTHAN_ZERO,
                    "GetCashoutPass,OPERATOR(id=" + operator.getId() + ") can't accept cashout amount less or equal 0");
        }

        if (operator.getCreditType() == Merchant.CREDIT_TYPE_DEFINITIVEVALUE) {
            // check operator balance
            BigDecimal totalBalance = new BigDecimal("0");
            totalBalance = totalBalance.add(operator.getCommisionBalance());
            totalBalance = totalBalance.add(operator.getCashoutBalance());
            totalBalance = totalBalance.add(operator.getPayoutCreditLevel());

            if (logger.isDebugEnabled()) {
                logger.debug("current operator :operator_id:" + operator.getId() + ",totalbalance="
                        + totalBalance.toPlainString());
            }

            // judge the balance whether or not sufficient
            if (totalBalance.compareTo(cashoutDto.getAmount()) < 0) {
                throw new ApplicationException(SystemException.CODE_INSUFFICIENT_BALANCE, "OPERATOR(id="
                        + operator.getId()
                        + ") insufficient balance[commission balance,cashout balance,payout balance].");
            }

        } else if (operator.getCreditType() == Merchant.CREDIT_TYPE_USE_PARENT) {
            // lookup the merchant
            Merchant originmerchant = getMerchantByOperator(cashoutOperatorid, false);
            Merchant finalMerchant = this.merchantDao.findDistributeMerchantByMerchantId(originmerchant.getId());
            if (finalMerchant == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "operator(id=" + cashoutOperatorid
                        + ") doesn't exist parent merchant.");
            }
            // check merchant balance
            BigDecimal totalBalance = new BigDecimal("0");
            totalBalance = totalBalance.add(finalMerchant.getCommisionBalance());
            totalBalance = totalBalance.add(finalMerchant.getCashoutBalance());
            totalBalance = totalBalance.add(finalMerchant.getPayoutCreditLevel());

            if (logger.isDebugEnabled()) {
                logger.debug("current merchant :final_merchant_id:" + finalMerchant.getId() + ",totalbalance="
                        + totalBalance.toPlainString());
            }

            // judge the balance whether or not sufficient
            if (totalBalance.compareTo(cashoutDto.getAmount()) < 0) {
                throw new ApplicationException(SystemException.CODE_INSUFFICIENT_BALANCE, "MERCHANT(id="
                        + finalMerchant.getId()
                        + ") insufficient balance[commission balance,cashout balance,payout balance].");
            }
        }

        // Get sys_configuration
        SysConfiguration sysConf = this.getSysConfigurationDao().getSysConfiguration();

        // successful record the data to 'CASHOUT_PASS'
        // MAX_EXPIRE_TIME_CASHOUT_PASS (unit: minute)
        String teTransactionID = respCtx.getTransaction().getId();

        CashoutPass cashoutpass = new CashoutPass();
        String generalid = this.getUuidManager().getGeneralID();
        cashoutpass.setId(generalid);
        cashoutpass.setOperatorId(cashoutOperatorid);
        cashoutpass.setCashoutAmount(cashoutDto.getAmount());
        cashoutpass.setCashoutPassword(cashoutDto.getPassword());
        cashoutpass.setExpireTime(DateUtils.addMinute(new Date(), sysConf.getMaxExpireTimeCashoutPass()));
        cashoutpass.setTriedTimes(0);
        cashoutpass.setTeTransactionId(teTransactionID);
        cashoutpass.setCashoutBarCode(new Barcoder(0, generalid).getBarcode()); // barcode
        cashoutpass.setCreateBy(respCtx.getOperatorId());
        cashoutpass.setCreateTime(net.mpos.fk.util.DateUtils.getCurrentDate());

        this.cashoutPassDao.insert(cashoutpass);

        // assemble the response bean
        CashOutPassDto respCashoutDto = new CashOutPassDto();
        respCashoutDto.setAmount(cashoutDto.getAmount());
        respCashoutDto.setExpireTime(net.mpos.fk.util.DateUtils.convertTimestampToString(cashoutpass.getExpireTime()));
        respCashoutDto.setBarcode(cashoutpass.getCashoutBarCode());

        return respCashoutDto;
    }

    @Override
    public CashOutByOperatorPassDto cashoutOperatorByPass(Context respCtx, Context responseCtx,
            CashOutByOperatorPassDto cashoutDto) throws ApplicationException {
        // tried times add 1
        this.cashoutPassDao.increaseTriedTimes(cashoutDto.getBarcode());

        // lookup the 'CASHOUT_PASS' according to field 'BARCODE' & 'password'
        CashoutPass cashoutpass = this.cashoutPassDao.findByBarcode(cashoutDto.getBarcode());
        if (cashoutpass == null || cashoutpass.getId() == null || "".equals(cashoutpass.getId())) {
            // throw new ApplicationException(SystemException.CODE_CASHOUTPASS_NO_EXIST_BARCODE,
            // "[CashoutByPassword](barcode="
            // + cashoutDto.getBarcode() + ") not exist the barcode].");
            responseCtx.setResponseCode(SystemException.CODE_CASHOUTPASS_NO_EXIST_BARCODE);
            logger.error("======================================");
            logger.error("[CashoutByPassword](barcode=" + cashoutDto.getBarcode() + ") not exist the barcode].");
            logger.error("======================================");
            return null;
        }

        // check whether or not used
        if (cashoutpass.getCashoutTeTransactionId() != null && !"".equals(cashoutpass.getCashoutTeTransactionId())) {
            // throw new ApplicationException(SystemException.CODE_CASHOUTPASS_ALREADY_USED,
            // "[CashoutByPassword](barcode="
            // + cashoutDto.getBarcode() + ") is already used].");
            responseCtx.setResponseCode(SystemException.CODE_CASHOUTPASS_ALREADY_USED);
            logger.error("======================================");
            logger.error("[CashoutByPassword](barcode=" + cashoutDto.getBarcode() + ") is already used].");
            logger.error("======================================");
            return null;
        }

        // check expired time
        if (new Date().compareTo(cashoutpass.getExpireTime()) > 0) {
            // throw new ApplicationException(SystemException.CODE_CASHOUTPASS_EXPIRETIME,
            // "[CashoutByPassword] The cashout password is expiry !");
            responseCtx.setResponseCode(SystemException.CODE_CASHOUTPASS_EXPIRETIME);
            logger.error("======================================");
            logger.error("[CashoutByPassword] The cashout password is expiry !");
            logger.error("======================================");
            return null;
        }

        // check max tried times
        SysConfiguration sysConf = this.getSysConfigurationDao().getSysConfiguration();
        int maxiumtimes = sysConf.getMaxiumTimesOfCashoutPass();
        if ((cashoutpass.getTriedTimes()) > maxiumtimes) {
            // throw new ApplicationException(SystemException.CODE_CASHOUTPASS_EXCEED_MAXTIMES,
            // "[CashoutByPassword] The cashout by password is exceed max tried times !");
            responseCtx.setResponseCode(SystemException.CODE_CASHOUTPASS_EXCEED_MAXTIMES);
            logger.error("======================================");
            logger.error("[CashoutByPassword] The cashout by password is exceed max tried times !");
            logger.error("======================================");
            return null;
        }

        // check password whether or not correct
        if (!cashoutpass.getCashoutPassword().equals(cashoutDto.getPassword())) {
            // throw new ApplicationException(SystemException.CODE_CASHOUTPASS_INCORRECT,
            // "[CashoutByPassword](barcode="
            // + cashoutDto.getBarcode() + ") password is incorrect].");
            responseCtx.setResponseCode(SystemException.CODE_CASHOUTPASS_INCORRECT);
            logger.error("======================================");
            logger.error("[CashoutByPassword](barcode=" + cashoutDto.getBarcode() + ") password is incorrect].");
            logger.error("======================================");
            return null;
        }

        // main logic for cash out
        cashoutLogic(respCtx, cashoutpass, cashoutpass.getOperatorId(), cashoutpass.getCashoutAmount(),
                TransactionType.CASH_OUT_OPERATOR_PASS.getRequestType());

        // ==========================

        // assemble response bean
        CashOutByOperatorPassDto respCashoutPassDto = new CashOutByOperatorPassDto();
        respCashoutPassDto.setAmount(cashoutpass.getCashoutAmount());
        respCashoutPassDto.setOperatorId(cashoutpass.getOperatorId());

        return respCashoutPassDto;
    }

    @Override
    public CashOutByManualDto cashoutOperatorByManual(Context respCtx, CashOutByManualDto cashoutDto)
            throws ApplicationException {

        // cashout amount must greater than 0
        if (cashoutDto.getAmount().doubleValue() <= 0) {
            throw new ApplicationException(SystemException.CODE_CASHOUT_AMOUNT_LESSTHAN_ZERO, "("
                    + "cashoutOperatorByManual) can't accept cashout amount less or equal 0");
        }

        // check password
        String strMD5 = "";
        try {
            strMD5 = SecurityMeasurements.MD5PlainTextToHexString(cashoutDto.getPassword());
        } catch (Exception e) {
            throw new ApplicationException(SystemException.CODE_INTERNAL_SERVER_ERROR, "[CashoutByManual]("
                    + "MD5 password Exception.)");
        }

        // Find by Operator [login name] cashoutDto.getOperatorId is operator login name
        Operator operator = this.operatorDao.findByLoginName(cashoutDto.getOperatorId());

        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "[cashout operator manual]operator(id="
                    + cashoutDto.getOperatorId() + ") doesn't exist.");
        }

        if (operator.getPassword().trim().equalsIgnoreCase(strMD5) == false) {
            throw new ApplicationException(SystemException.CODE_CASHOUTMANUAL_INCORRECT_PASSWORD, "[CashoutByManual]("
                    + "MD5 password is incorrent.)");
        }

        // main logic for cash out
        cashoutLogic(respCtx, null, operator.getId(), cashoutDto.getAmount(),
                TransactionType.CASH_OUT_OPERATOR_MANUAL.getRequestType());

        // ==========================

        // assemble response bean
        CashOutByManualDto respCashoutManualDto = new CashOutByManualDto();
        respCashoutManualDto.setAmount(cashoutDto.getAmount());
        respCashoutManualDto.setOperatorId(cashoutDto.getOperatorId());

        return respCashoutManualDto;
    }

    // -----------------------------------------------------
    // HELP METHOD
    // -----------------------------------------------------
    private void cashoutLogic(Context respCtx, CashoutPass cashoutpass, String cashoutOperatorid,
            BigDecimal cashoutAmount, int transType) throws ApplicationException {
        GsonCashOutOperator gcoo = new GsonCashOutOperator();
        // check account whether or not sufficient
        Operator operator = this.operatorDao.findById(Operator.class, cashoutOperatorid);
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "[Cashout] operator(id="
                    + cashoutOperatorid + ") doesn't exist.");
        }

        // check cash out operator id whether equals respCtx operator id , should not be the same
        if (cashoutOperatorid.equals(respCtx.getOperatorId())) {
            throw new ApplicationException(SystemException.CODE_CASHOUT_OPERATOR_SHOULD_NOT_SAME,
                    "[Cashout] operator(id=" + cashoutOperatorid + ") should not the same with Operator.");
        }

        if (operator.getCreditType() == Merchant.CREDIT_TYPE_DEFINITIVEVALUE) {
            // check operator balance
            BigDecimal totalBalance = new BigDecimal("0");
            totalBalance = totalBalance.add(operator.getCommisionBalance());
            totalBalance = totalBalance.add(operator.getPayoutCreditLevel());
            totalBalance = totalBalance.add(operator.getCashoutBalance());

            if (logger.isDebugEnabled()) {
                logger.debug("[Cashout] current operator :operator_id:" + operator.getId() + ",totalbalance="
                        + totalBalance.toPlainString());
            }

            // judge the balance whether or not sufficient
            if (totalBalance.compareTo(cashoutAmount) < 0) {
                throw new ApplicationException(SystemException.CODE_INSUFFICIENT_BALANCE, "[Cashout] OPERATOR(id="
                        + operator.getId()
                        + ") insufficient balance[commission balance,cashout balance,payout balance].");
            }

            // ==============
            // deduct the money from balance order by [1)Commission balance
            // 2)Payout balance 3)Cash-out balance]
            BigDecimal commissionDeducted = new BigDecimal("0");
            BigDecimal payoutDeducted = new BigDecimal("0");
            BigDecimal cashoutDeducted = new BigDecimal("0");

            BigDecimal cashoutAmountTemp = operator.getCommisionBalance().subtract(cashoutAmount);
            if (cashoutAmountTemp.doubleValue() >= 0) {
                commissionDeducted = cashoutAmount;
            } else {
                // negative value
                if (operator.getCommisionBalance().doubleValue() < 0
                        && operator.getPayoutCreditLevel().doubleValue() < 0) {
                    cashoutDeducted = cashoutAmount;
                } else if (operator.getCommisionBalance().doubleValue() < 0) {
                    // deducted account only (payout balance)
                    if (operator.getPayoutCreditLevel().subtract(cashoutAmount).doubleValue() > 0) {
                        payoutDeducted = cashoutAmount;
                    } else {
                        payoutDeducted = operator.getPayoutCreditLevel();
                        cashoutDeducted = (cashoutAmount.subtract(payoutDeducted));
                    }
                } else {
                    // deducted account (commission balance + payout balance)
                    if ((operator.getCommisionBalance().add(operator.getPayoutCreditLevel())).subtract(cashoutAmount)
                            .doubleValue() >= 0) {
                        commissionDeducted = operator.getCommisionBalance();
                        payoutDeducted = cashoutAmount.subtract(commissionDeducted);
                    } else {
                        if ((operator.getCommisionBalance().add(operator.getPayoutCreditLevel()).add(operator
                                .getCashoutBalance())).subtract(cashoutAmount).doubleValue() >= 0) {
                            commissionDeducted = operator.getCommisionBalance();
                            payoutDeducted = operator.getPayoutCreditLevel();
                            cashoutDeducted = (cashoutAmount.subtract(commissionDeducted)).subtract(payoutDeducted);
                        }
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[Cashout] current operator :operator_id:" + cashoutOperatorid
                        + ",deducted money balance:commissionDeducted=" + commissionDeducted.toPlainString()
                        + ";payoutdeducted:" + payoutDeducted + ";cashoutdeducted:" + cashoutDeducted);
            }

            // set 2 decimals place
            commissionDeducted.setScale(
                    MLotteryContext.getInstance().getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION),
                    BigDecimal.ROUND_HALF_UP);
            payoutDeducted.setScale(2, BigDecimal.ROUND_HALF_UP);
            cashoutDeducted.setScale(2, BigDecimal.ROUND_HALF_UP);
            this.operatorDao.deductBalanceByOperator(commissionDeducted, payoutDeducted, cashoutDeducted,
                    cashoutOperatorid);
            // ADD BALANCE_TRANSACTION RECORD
            addBalanceTransactionRecord(respCtx, new BigDecimal(String.valueOf(cashoutAmount.doubleValue())),
                    transType, cashoutOperatorid, BalanceTransactions.OWNER_TYPE_OPERATOR,
                    BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, new BigDecimal("0"), new BigDecimal("0"));

            // assemble the Json obj
            gcoo.setOperatorMerchantType(BalanceTransactions.OWNER_TYPE_OPERATOR);
            gcoo.setOperatorMerchantid(cashoutOperatorid);
            gcoo.setOperatorid(cashoutOperatorid);
            gcoo.setTotalAmount(cashoutAmount);
            gcoo.setCommission(commissionDeducted);
            gcoo.setPayout(payoutDeducted);
            gcoo.setCashout(cashoutDeducted);

        } else if (operator.getCreditType() == Merchant.CREDIT_TYPE_USE_PARENT) {
            // lookup the merchant
            Merchant originmerchant = getMerchantByOperator(cashoutOperatorid, false);
            Merchant finalMerchant = this.merchantDao.findDistributeMerchantByMerchantId(originmerchant.getId());
            if (finalMerchant == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "operator(id=" + cashoutOperatorid
                        + ") doesn't exist parent merchant.");
            }
            // check merchant balance
            BigDecimal totalBalance = new BigDecimal("0");
            totalBalance = totalBalance.add(finalMerchant.getCommisionBalance());
            totalBalance = totalBalance.add(finalMerchant.getCashoutBalance());
            totalBalance = totalBalance.add(finalMerchant.getPayoutCreditLevel());

            if (logger.isDebugEnabled()) {
                logger.debug("current merchant :final_merchant_id:" + finalMerchant.getId() + ",totalbalance="
                        + totalBalance.toPlainString());
            }

            // judge the balance whether or not sufficient
            if (totalBalance.compareTo(cashoutAmount) < 0) {
                throw new ApplicationException(SystemException.CODE_INSUFFICIENT_BALANCE, "MERCHANT(id="
                        + finalMerchant.getId()
                        + ") insufficient balance[commission balance,cashout balance,payout balance].");
            }

            // ==============
            // deduct the money from balance order by [1)Commission balance
            // 2)Payout balance 3)Cash-out balance]
            BigDecimal commissionDeducted = new BigDecimal("0");
            BigDecimal payoutDeducted = new BigDecimal("0");
            BigDecimal cashoutDeducted = new BigDecimal("0");

            BigDecimal cashoutAmountTemp = finalMerchant.getCommisionBalance().subtract(cashoutAmount);
            if (cashoutAmountTemp.doubleValue() >= 0) {
                commissionDeducted = cashoutAmount;
            } else {
                // negative value
                if (finalMerchant.getCommisionBalance().doubleValue() < 0
                        && finalMerchant.getPayoutCreditLevel().doubleValue() < 0) {
                    cashoutDeducted = cashoutAmount;
                } else if (finalMerchant.getCommisionBalance().doubleValue() < 0) {
                    // deducted account only (payout balance)
                    if (finalMerchant.getPayoutCreditLevel().subtract(cashoutAmount).doubleValue() > 0) {
                        payoutDeducted = cashoutAmount;
                    } else {
                        payoutDeducted = finalMerchant.getPayoutCreditLevel();
                        cashoutDeducted = (cashoutAmount.subtract(payoutDeducted));
                    }
                } else {
                    // deducted account (commission balance + payout balance)
                    if ((finalMerchant.getCommisionBalance().add(finalMerchant.getPayoutCreditLevel())).subtract(
                            cashoutAmount).doubleValue() >= 0) {
                        commissionDeducted = finalMerchant.getCommisionBalance();
                        payoutDeducted = cashoutAmount.subtract(commissionDeducted);
                    } else {
                        if ((finalMerchant.getCommisionBalance().add(finalMerchant.getPayoutCreditLevel())
                                .add(finalMerchant.getCashoutBalance())).subtract(cashoutAmount).doubleValue() >= 0) {
                            commissionDeducted = finalMerchant.getCommisionBalance();
                            payoutDeducted = finalMerchant.getPayoutCreditLevel();
                            cashoutDeducted = (cashoutAmount.subtract(commissionDeducted)).subtract(payoutDeducted);
                        }
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[Cashout] current merchant :merchant_id:" + finalMerchant.getId()
                        + ",deducted money balance:commissionDeducted=" + commissionDeducted.toPlainString()
                        + ";payoutdeducted:" + payoutDeducted + ";cashoutdeducted:" + cashoutDeducted);
            }

            commissionDeducted.setScale(
                    MLotteryContext.getInstance().getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION),
                    BigDecimal.ROUND_HALF_UP);
            payoutDeducted.setScale(2, BigDecimal.ROUND_HALF_UP);
            cashoutDeducted.setScale(2, BigDecimal.ROUND_HALF_UP);
            this.merchantDao.deductBalanceByMerchant(commissionDeducted, payoutDeducted, cashoutDeducted,
                    finalMerchant.getId());

            // ADD BALANCE_TRANSACTION RECORD
            addBalanceTransactionRecord(respCtx, new BigDecimal(String.valueOf(cashoutAmount.doubleValue())),
                    transType, String.valueOf(finalMerchant.getId()), BalanceTransactions.OWNER_TYPE_MERCHANT,
                    BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, new BigDecimal("0"), new BigDecimal("0"));

            // assemble the Json obj
            gcoo.setOperatorMerchantType(BalanceTransactions.OWNER_TYPE_MERCHANT);
            gcoo.setOperatorMerchantid(String.valueOf(finalMerchant.getId()));
            gcoo.setOperatorid(cashoutOperatorid);
            gcoo.setTotalAmount(cashoutAmount);
            gcoo.setCommission(commissionDeducted);
            gcoo.setPayout(payoutDeducted);
            gcoo.setCashout(cashoutDeducted);

        }

        // update 'CASHOUT_PASS' the field 'cashout_te_transaction_id'
        // increase the tried times add 1
        if (cashoutpass != null) {
            cashoutpass.setId(cashoutpass.getId());
            cashoutpass.setTriedTimes(cashoutpass.getTriedTimes());
            cashoutpass.setCashoutTeTransactionId(respCtx.getTransaction().getId());
            cashoutpass.setUpdateBy(respCtx.getOperatorId());
            cashoutpass.setUpdateTime(net.mpos.fk.util.DateUtils.getCurrentDate());
            this.cashoutPassDao.update(cashoutpass);
        }
        // ==========================
        // //add cash out balance & commission to self operator
        String commissionOperatorId = respCtx.getOperatorId();
        Operator commisionOperator = this.operatorDao.findById(Operator.class, commissionOperatorId);
        if (commisionOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR,
                    "[CashoutByPassword] commisionOperator(id=" + commissionOperatorId + ") doesn't exist.");
        }

        // credit
        if (commisionOperator.getCreditType() == Merchant.CREDIT_TYPE_DEFINITIVEVALUE) {
            BigDecimal cashoutrate = commisionOperator.getCashoutRate();
            BigDecimal cashoutamount = cashoutAmount;
            // BigDecimal commissionAmount = cashoutamount.multiply(cashoutrate);
            BigDecimal commissionAmount = SimpleToolkit.mathMultiple(cashoutamount, cashoutrate, MLotteryContext
                    .getInstance().getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION));

            cashoutamount.setScale(2, BigDecimal.ROUND_HALF_UP);
            this.operatorDao.addCashoutAndCommissionToOperator(cashoutamount, commissionAmount, commissionOperatorId);

            // insert record into 'BALANCE_TRANSACTIONS'
            // ADD BALANCE_TRANSACTION RECORD
            addBalanceTransactionRecord(respCtx, cashoutAmount, transType, commissionOperatorId,
                    BalanceTransactions.OWNER_TYPE_OPERATOR, BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY,
                    commissionAmount, cashoutrate);
            // assemble the Json obj
            gcoo.setPlusOperatorMerchantType(BalanceTransactions.OWNER_TYPE_OPERATOR);
            gcoo.setPlusOperatorid(commissionOperatorId);
            gcoo.setPlusOperatorCashoutBalance(cashoutamount);
            gcoo.setPlusOperatorCommissionBalance(commissionAmount);
            gcoo.setPlusOperatorCommissionRate(cashoutrate);
        } else if (commisionOperator.getCreditType() == Merchant.CREDIT_TYPE_USE_PARENT) {
            // lookup the merchant
            Merchant originmerchant = getMerchantByOperator(commissionOperatorId, false);
            Merchant finalMerchant = this.merchantDao.findDistributeMerchantByMerchantId(originmerchant.getId());
            if (finalMerchant == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "commission operator(id="
                        + commissionOperatorId + ") doesn't exist parent merchant.");
            }

            // use operator cash out rate
            BigDecimal cashoutrate = commisionOperator.getCashoutRate();
            BigDecimal cashoutamount = cashoutAmount;
            // BigDecimal commissionOperatorAmount = cashoutamount.multiply(cashoutrate);
            BigDecimal commissionOperatorAmount = SimpleToolkit.mathMultiple(cashoutamount, cashoutrate,
                    MLotteryContext.getInstance().getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION));

            // merchant cash out rate
            // BigDecimal finalcashoutrate = finalMerchant.getCashoutRate();
            // BigDecimal finalcashoutamount = cashoutAmount;
            // BigDecimal commissionMerchantAmount = finalcashoutamount.multiply(finalcashoutrate);

            cashoutamount.setScale(2, BigDecimal.ROUND_HALF_UP);
            this.merchantDao.addCashoutAndCommissionToMerchant(cashoutamount, null, finalMerchant.getId());
            // ADD BALANCE_TRANSACTION RECORD
            addBalanceTransactionRecord(respCtx, cashoutAmount, transType, commissionOperatorId,
                    BalanceTransactions.OWNER_TYPE_OPERATOR, BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY,
                    commissionOperatorAmount, cashoutrate);

            addBalanceTransactionRecord(respCtx, cashoutAmount, transType, String.valueOf(finalMerchant.getId()),
                    BalanceTransactions.OWNER_TYPE_MERCHANT, BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY,
                    new BigDecimal("0"), new BigDecimal("0"));

            // assemble the Json obj
            gcoo.setPlusOperatorMerchantType(BalanceTransactions.OWNER_TYPE_OPERATOR);
            gcoo.setPlusOperatorid(commissionOperatorId);
            gcoo.setPlusOperatorCashoutBalance(cashoutamount);
            gcoo.setPlusOperatorCommissionBalance(commissionOperatorAmount);
            gcoo.setPlusOperatorCommissionRate(cashoutrate);

            gcoo.setPlusOperatorMerchantType(BalanceTransactions.OWNER_TYPE_MERCHANT);
            gcoo.setPlusMerchantid(String.valueOf(finalMerchant.getId()));
            gcoo.setPlusMerchantCashoutBalance(cashoutamount);
            gcoo.setPlusMerchantCommissionBalance(new BigDecimal("0"));
            gcoo.setPlusMerchantCommissionRate(new BigDecimal("0"));

        }

        // DESTINATION_OPEATOR
        respCtx.getTransaction().setDestinationOpeator(cashoutOperatorid);
        respCtx.getTransaction().setTotalAmount(cashoutAmount);

        // write transaction message for reversal.
        // add record to te_transaction_msg
        // assemble request message use JSON
        String reqmsg = new Gson().toJson(gcoo);
        TransactionMessage msg = new TransactionMessage();
        msg.setTransactionId(respCtx.getTransaction().getId());
        msg.setRequestMsg(reqmsg);
        respCtx.getTransaction().setTransMessage(msg);

    }

    private void addBalanceTransactionRecord(Context respCtx, BigDecimal cashoutAmount, int transType,
            String operatorMerchantid, int ownerType, int paymentType, BigDecimal commissionAmount,
            BigDecimal commissionRate) throws ApplicationException {
        BalanceTransactions bt = new BalanceTransactions();
        bt.setTeTransactionId(respCtx.getTransaction().getId());
        bt.setMerchantId(respCtx.getTransaction().getMerchantId());
        bt.setDeviceId(respCtx.getTransaction().getDeviceId());
        bt.setOperatorId(respCtx.getTransaction().getOperatorId());
        bt.setOwnerId(operatorMerchantid);
        bt.setOwnerType(ownerType);
        bt.setPaymentType(paymentType);
        bt.setTransactionType(transType);
        bt.setOriginalTransType(transType);
        bt.setTransactionAmount(cashoutAmount);
        bt.setCommissionAmount(commissionAmount);
        bt.setCommissionRate(commissionRate);
        bt.setCreateTime(net.mpos.fk.util.DateUtils.getNowTimestamp());
        bt.setStatus(BalanceTransactions.STATUS_VALID);
        this.balanceTransactionsDao.insert(bt);
    }

    // ----------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public MerchantCommissionDao getMerchantCommissionDao() {
        return merchantCommissionDao;
    }

    public void setMerchantCommissionDao(MerchantCommissionDao merchantCommissionDao) {
        this.merchantCommissionDao = merchantCommissionDao;
    }

    public PrizeGroupItemDao getPrizeGroupItemDao() {
        return prizeGroupItemDao;
    }

    public void setPrizeGroupItemDao(PrizeGroupItemDao prizeGroupItemDao) {
        this.prizeGroupItemDao = prizeGroupItemDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public OperatorMerchantDao getOperatorMerchantDao() {
        return operatorMerchantDao;
    }

    public void setOperatorMerchantDao(OperatorMerchantDao operatorMerchantDao) {
        this.operatorMerchantDao = operatorMerchantDao;
    }

    public CashoutPassDao getCashoutPassDao() {
        return cashoutPassDao;
    }

    public void setCashoutPassDao(CashoutPassDao cashoutPassDao) {
        this.cashoutPassDao = cashoutPassDao;
    }

    public SysConfigurationDao getSysConfigurationDao() {
        return sysConfigurationDao;
    }

    public void setSysConfigurationDao(SysConfigurationDao sysConfigurationDao) {
        this.sysConfigurationDao = sysConfigurationDao;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

    public TransactionMessageDao getTransMessageDao() {
        return transMessageDao;
    }

    public void setTransMessageDao(TransactionMessageDao transMessageDao) {
        this.transMessageDao = transMessageDao;
    }

}
