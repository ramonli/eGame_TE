package com.mpos.lottery.te.merchant.service.impl;

import com.google.gson.Gson;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.CreditTransferLogDao;
import com.mpos.lottery.te.merchant.dao.MerchantCommissionDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorCommissionDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.CreditTransferLog;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.MerchantCommission;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.domain.OperatorCommission;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.web.CreditTransferDto;
import com.mpos.lottery.te.merchant.web.OperatorTopupDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionMessage;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

public class DefaultCreditService extends AbstractReversalOrCancelStrategy implements CreditService {
    private Log logger = LogFactory.getLog(DefaultCreditService.class);

    private MerchantDao merchantDao;

    private MerchantCommissionDao merchantCommissionDao;

    // private PrizeGroupItemDao prizeGroupItemDao;
    private UUIDService uuidManager;

    private OperatorDao operatorDao;

    private OperatorMerchantDao operatorMerchantDao;

    private CreditTransferLogDao creditTransferLogDao;

    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;

    private MerchantService merchantService;

    private BalanceTransactionsDao balanceTransactionsDao;

    private OperatorCommissionDao operatorCommissionDao;

    public Object credit(String operatorId, long merchantId, BigDecimal amount, String gameId, boolean isRestore,
            boolean isSaleStyle, boolean isSoldByCreditCard) throws ApplicationException {
        return this.credit(operatorId, merchantId, amount, gameId, isRestore, isSaleStyle, isSoldByCreditCard, null);
    }

    /**
     * If data consistency(maybe multiple clients update credit level concurrently) is prior to performance, you can
     * synchronize this method.
     * 
     * @see CreditService#credit(String, long, BigDecimal, String, boolean, boolean, boolean)
     */
    public Object credit(String operatorId, long merchantId, BigDecimal amount, String gameId, boolean isRestore,
            boolean isSaleStyle, boolean isSoldByCreditCard, Transaction transaction) throws ApplicationException {
        Operator operator = this.getOperatorDao().findById(Operator.class, operatorId);
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "can NOT find operator by id='"
                    + operatorId + "'.");
        }
        if (operator.isIgnoreCredit()) {
            logger.info("Ignore the credit setting of operator(" + operator + ")");
            return null;
        }

        Merchant merchant = this.getMerchantDao().findById(Merchant.class, merchantId);
        if (merchant == null) {
            throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find merchant by id='"
                    + merchantId + "'.");
        }
        return this.doCredit(operator, merchant, amount, gameId, isRestore, isSaleStyle, isSoldByCreditCard,
                transaction);
    }

    @Override
    public Object credit(String operatorId, long merchantId, BigDecimal amount, boolean isRestore, boolean isSaleStyle)
            throws ApplicationException {

        return this.credit(operatorId, merchantId, amount, null, isRestore, isSaleStyle, false);
    }

    public void update(Merchant merchant) throws ApplicationException {
        this.getMerchantDao().update(merchant);
    }

    @Override
    public CreditTransferDto transferCredit(Context reqCtx, Context respCtx, CreditTransferDto dto)
            throws ApplicationException {
        // lookup from operator
        Operator fromOperator = this.getOperatorDao().findByLoginNameForUpdate(dto.getFromOperatorLoginName());
        if (fromOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found by login name("
                    + dto.getFromOperatorLoginName() + ").");
        }
        Merchant fromMerchant = this.getMerchantService().getMerchantByOperator(fromOperator.getId(), true);

        // lookup to operator
        Operator toOperator = this.getOperatorDao().findByLoginNameForUpdate(dto.getToOperatorLoginName());
        if (toOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found by login name("
                    + dto.getToOperatorLoginName() + ").");
        }
        Merchant toMerchant = this.getMerchantService().getMerchantByOperator(toOperator.getId(), true);

        Object fromTarget = null;
        Object toTarget = null;

        // 1161 transfer sale
        // 1162 transfer payout
        // 1163 transfer commission
        // 1164 transfer cashout
        if (CreditTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            // deduct from from-operator
            fromTarget = this.doCredit(reqCtx, respCtx, fromOperator, fromMerchant, dto.getAmount(), null, false, true,
                    false, false, null);
            // topup
            toTarget = this.doCredit(reqCtx, respCtx, toOperator, toMerchant, dto.getAmount(), null, true, true, false,
                    false, null);
        } else if (CreditTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            // deduct from from-operator
            fromTarget = this.doCredit(reqCtx, respCtx, fromOperator, fromMerchant, dto.getAmount(), null, false,
                    false, false, false, null);
            // topup
            toTarget = this.doCredit(reqCtx, respCtx, toOperator, toMerchant, dto.getAmount(), null, true, false,
                    false, false, null);
        } else if (CreditTransferDto.CREDITTYPE_COMMISSION == dto.getCreditType()) {
            // deduct from from-operator
            fromTarget = this.doCredit(reqCtx, respCtx, fromOperator, fromMerchant, dto.getAmount(), null, false,
                    false, false, false, null);
            // topup
            toTarget = this.doCredit(reqCtx, respCtx, toOperator, toMerchant, dto.getAmount(), null, true, false,
                    false, false, null);
        } else if (CreditTransferDto.CREDITTYPE_CASHOUT == dto.getCreditType()) {
            // deduct from from-operator
            fromTarget = this.doCredit(reqCtx, respCtx, fromOperator, fromMerchant, dto.getAmount(), null, false,
                    false, false, false, null);
            // topup
            toTarget = this.doCredit(reqCtx, respCtx, toOperator, toMerchant, dto.getAmount(), null, true, false,
                    false, false, null);
        } else {
            throw new SystemException("Unsupported credit type of [" + CreditTransferDto.class + "]:"
                    + dto.getCreditType());
        }

        // insert into balance transaction start
        // transType
        int transType = 116;
        if (CreditTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_SALE_BALANCE_TRANS_TYPE;
        } else if (CreditTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_PAYOUT_BALANCE_TRANS_TYPE;
        } else if (CreditTransferDto.CREDITTYPE_COMMISSION == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_COMMISSION_BALANCE_TRANS_TYPE;
        } else if (CreditTransferDto.CREDITTYPE_CASHOUT == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_CASHOUT_BALANCE_TRANS_TYPE;
        }

        // from record
        Long fromPrefixFromParentMerchantid = null;
        Long fromPrefixToParentMerchantid = null;

        if (fromTarget instanceof Merchant) {
            fromPrefixFromParentMerchantid = ((Merchant) fromTarget).getId();
        }

        if (toTarget instanceof Merchant) {
            fromPrefixToParentMerchantid = ((Merchant) toTarget).getId();
        }

        balanceTransactionsDao.addBalanceTransferBalanceTransactionRecord(reqCtx, dto.getAmount(), transType,
                transType, fromPrefixFromParentMerchantid, fromPrefixToParentMerchantid, toOperator.getId(),
                fromOperator.getId(), BalanceTransactions.OWNER_TYPE_OPERATOR,
                BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);

        // to record
        Long toPrefixFromParentMerchantid = null;
        Long toPrefixToParentMerchantid = null;

        if (toTarget instanceof Merchant) {
            toPrefixFromParentMerchantid = ((Merchant) toTarget).getId();
        }

        if (fromTarget instanceof Merchant) {
            toPrefixToParentMerchantid = ((Merchant) fromTarget).getId();
        }

        balanceTransactionsDao.addBalanceTransferBalanceTransactionRecord(reqCtx, dto.getAmount(), transType,
                transType, toPrefixFromParentMerchantid, toPrefixToParentMerchantid, fromOperator.getId(),
                toOperator.getId(), BalanceTransactions.OWNER_TYPE_OPERATOR,
                BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY);

        // insert into balance transaction end

        // initialize a transfer log
        CreditTransferLog log = this.assembleCreditTransferLog(respCtx, dto, fromOperator, fromMerchant, toOperator,
                toMerchant, fromTarget, toTarget);
        this.getCreditTransferLogDao().insert(log);

        // assemble response DTO
        this.assembleCreditBalance(dto, fromTarget, toTarget);

        return dto;
    }

    /**
     * Cancel the transaction of 'CreditTransfer'.
     */
    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        if (targetTrans.getTransMessage() == null || targetTrans.getTransMessage().getRequestMsg() == null) {
            logger.warn("NO associated transaction message found.");
            return false;
        }
        CreditTransferDto dto = new Gson().fromJson(targetTrans.getTransMessage().getRequestMsg(),
                CreditTransferDto.class);

        //fix #7574
        boolean isCalCommission = MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation();
        Context reqctx = new Context();
        CreditTransferDto credittransferdto = new CreditTransferDto();
        
        
        // lookup from operator
        Operator fromOperator = this.getOperatorDao().findByLoginNameForUpdate(dto.getFromOperatorLoginName());
        if (fromOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found by login name("
                    + dto.getFromOperatorLoginName() + ").");
        }
        Merchant fromMerchant = this.getMerchantService().getMerchantByOperator(fromOperator.getId(), true);

        // lookup to operator
        Operator toOperator = this.getOperatorDao().findByLoginNameForUpdate(dto.getToOperatorLoginName());
        if (toOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found by login name("
                    + dto.getToOperatorLoginName() + ").");
        }
        Merchant toMerchant = this.getMerchantService().getMerchantByOperator(toOperator.getId(), true);

        if (CreditTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            /**
             * Update from-operator first, guarantee cancellation transaction will update operator in the same order
             * with creditTransfer transaction, otherwise there is possibility to cause deadlock.
             */
            // topup to from-operator
            credittransferdto.setCreditType(CreditTransferDto.CREDITTYPE_SALE);
            reqctx.setModel(credittransferdto);
            this.doCredit(reqctx, null, fromOperator, fromMerchant, dto.getAmount(), null, true, true, false, isCalCommission, null);
            // deduct
            this.doCredit(reqctx, null, toOperator, toMerchant, dto.getAmount(), null, false, true, false, isCalCommission, null);
        } else if (CreditTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            // topup from-operator
            credittransferdto.setCreditType(CreditTransferDto.CREDITTYPE_PAYOUT);
            reqctx.setModel(credittransferdto);
            this.doCredit(reqctx, null, fromOperator, fromMerchant, dto.getAmount(), null, true, false, false, isCalCommission, null);
            // deduct
            this.doCredit(reqctx, null, toOperator, toMerchant, dto.getAmount(), null, false, false, false, isCalCommission,null);
        } else if (CreditTransferDto.CREDITTYPE_COMMISSION == dto.getCreditType()) {
            // topup from-operator
            credittransferdto.setCreditType(CreditTransferDto.CREDITTYPE_COMMISSION);
            reqctx.setModel(credittransferdto);
            this.doCredit(reqctx, null, fromOperator, fromMerchant, dto.getAmount(), null, true, false, false, isCalCommission, null);
            // deduct
            this.doCredit(reqctx, null, toOperator, toMerchant, dto.getAmount(), null, false, false, false, isCalCommission, null);
        } else if (CreditTransferDto.CREDITTYPE_CASHOUT == dto.getCreditType()) {
            // topup from-operator
            credittransferdto.setCreditType(CreditTransferDto.CREDITTYPE_CASHOUT);
            reqctx.setModel(credittransferdto);
            this.doCredit(reqctx, null, fromOperator, fromMerchant, dto.getAmount(), null, true, false, false, isCalCommission, null);
            // deduct
            this.doCredit(reqctx, null, toOperator, toMerchant, dto.getAmount(), null, false, false, false, isCalCommission, null);
        } else {
            throw new SystemException("Unsupported credit type of [" + CreditTransferDto.class + "]:"
                    + dto.getCreditType());
        }

        // cancel balance transactions
        // update balance_transaction status to invalid
        balanceTransactionsDao.updateBalanceTransactionsStatusByteTransactionId(targetTrans.getTransMessage()
                .getTransactionId());

        // insert new 2 cancel record

        // insert into balance transaction start
        // transType
        int transType = 116;
        if (CreditTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_SALE_BALANCE_TRANS_TYPE;
        } else if (CreditTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_PAYOUT_BALANCE_TRANS_TYPE;
        } else if (CreditTransferDto.CREDITTYPE_COMMISSION == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_COMMISSION_BALANCE_TRANS_TYPE;
        } else if (CreditTransferDto.CREDITTYPE_CASHOUT == dto.getCreditType()) {
            transType = BalanceTransactions.TRANSFER_CASHOUT_BALANCE_TRANS_TYPE;
        }

        // from record
        balanceTransactionsDao.addBalanceTransferBalanceTransactionRecord(respCtx, dto.getAmount(),
                TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), transType, null, null, fromOperator.getId(),
                toOperator.getId(), BalanceTransactions.OWNER_TYPE_OPERATOR,
                BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY);

        // to record

        balanceTransactionsDao.addBalanceTransferBalanceTransactionRecord(respCtx, dto.getAmount(),
                TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), transType, null, null, toOperator.getId(),
                fromOperator.getId(), BalanceTransactions.OWNER_TYPE_OPERATOR,
                BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);

        // insert into balance transaction end

        // update creditTransferLog
        CreditTransferLog log = this.getCreditTransferLogDao().findByTransactionId(targetTrans.getId());
        log.setStatus(CreditTransferLog.STATUS_REVERSAL);
        this.getCreditTransferLogDao().update(log);

        return false;
    }

    @Override
    public OperatorTopupDto topupOperator(Context respCtx, OperatorTopupDto topupDto) throws ApplicationException {
        if (logger.isDebugEnabled()) {
            logger.debug("topup balance(" + topupDto + ") to operator " + respCtx.getOperatorId());
        }
        BalanceTransactions balanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(respCtx,
                topupDto.getAmount());
        String operatorId = respCtx.getOperatorId();
        long merchantId = respCtx.getMerchant().getId();
        if (topupDto.getOperatorId() != null && !"".equals(topupDto.getOperatorId())) {
            OperatorMerchant operatorMerchant = this.getOperatorMerchantDao().findByOperator(operatorId);
            if (operatorMerchant == null) {
                throw new ApplicationException(SystemException.CODE_OPERATOR_NO_MERCHANT, "operator(id=" + operatorId
                        + ") doesn't belong to any merchant, allocate it first.");
            }
            operatorId = topupDto.getOperatorId();
            merchantId = operatorMerchant.getMerchantID();
        }
        Object updatedOperator = this.credit(operatorId, merchantId, topupDto.getAmount(), true, true);
        if (updatedOperator == null) {
            throw new SystemException(SystemException.CODE_OPERATOR_TOPUP_IGNORED, "THe topup to operator(id="
                    + respCtx.getOperatorId() + " will be ignored.");
        } else {
            topupDto.setOperatorId(operatorId);
            if (updatedOperator instanceof Operator) {
                Operator operator = (Operator) updatedOperator;
                topupDto.setSaleBalance(operator.getSaleCreditLevel());
            } else if (updatedOperator instanceof Merchant) {
                Merchant merchant = (Merchant) updatedOperator;
                topupDto.setSaleBalance(merchant.getSaleCreditLevel());
                balanceTransactions.setOwnerId(String.valueOf(merchant.getId()));
                balanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_MERCHANT);
            } else {
                throw new IllegalStateException("unsupported topup target type: " + updatedOperator);
            }
        }
        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            balanceTransactionsDao.insert(balanceTransactions);
        }
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(respCtx.getTransaction().getId());
        transMsg.setRequestMsg(new Gson().toJson(balanceTransactions));
        respCtx.getTransaction().setTransMessage(transMsg);
        return topupDto;
    }

    // ----------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------

    protected CreditTransferLog assembleCreditTransferLog(Context respCtx, CreditTransferDto dto,
            Operator fromOperator, Merchant fromMerchant, Operator toOperator, Merchant toMerchant, Object fromTarget,
            Object toTarget) throws ApplicationException {
        BigDecimal saleCreditTransfer = null;
        BigDecimal payoutCreditTransfer = null;
        if (CreditTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            saleCreditTransfer = dto.getAmount();
        } else if (CreditTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            payoutCreditTransfer = dto.getAmount();
        }

        CreditTransferLog log = new CreditTransferLog(this.getUuidManager().getGeneralID(), respCtx.getTransaction()
                .getId(), fromOperator.getId(), fromMerchant.getId(), toOperator.getId(), respCtx.getTerminalId(),
                toMerchant.getId(), toMerchant.getCode(), toMerchant.getName(), saleCreditTransfer,
                payoutCreditTransfer, respCtx.getTransaction().getCreateTime());
        if (toTarget instanceof Operator) {
            Operator to = (Operator) toTarget;
            log.setSaleCreditOfToOperatorAfter(to.getSaleCreditLevel());
            log.setPayoutCreditOfToOperatorAfter(to.getPayoutCreditLevel());
            log.setTargetType(CreditTransferLog.TARGET_TYPE_CARD);
        } else if (toTarget instanceof Merchant) {
            Merchant to = (Merchant) toTarget;
            log.setSaleCreditOfToOperatorAfter(to.getSaleCreditLevel());
            log.setPayoutCreditOfToOperatorAfter(to.getPayoutCreditLevel());
            log.setTargetType(CreditTransferLog.TARGET_TYPE_MERCHANT);
        }
        if (fromTarget instanceof Operator) {
            Operator to = (Operator) fromTarget;
            log.setSaleCreditOfFromOperatorAfter(to.getSaleCreditLevel());
            log.setPayoutCreditOfFromOperatorAfter(to.getPayoutCreditLevel());
        } else if (fromTarget instanceof Merchant) {
            Merchant to = (Merchant) fromTarget;
            log.setSaleCreditOfFromOperatorAfter(to.getSaleCreditLevel());
            log.setPayoutCreditOfFromOperatorAfter(to.getPayoutCreditLevel());
        }
        return log;
    }

    /**
     * Calculate the commission of a merchant. A merchant can gain commission from payout/sale transactions, furthermore
     * different game will be assigned a different commission rate.
     * 
     * @param merchant
     *            Calculate commission for which merchant.
     * @param gameId
     *            The payout/sale is upon which game
     * @param actualAmount
     *            The actual amount of the transaction.
     * @param isSaleStyle
     *            Sale/cancel is sale style, however payout/cacnel is payout style.
     * @return the commission for the merchant according to the game commission rate.
     */
    private BigDecimal calculateCommission(Merchant merchant, String gameId, BigDecimal actualAmount,
            boolean isSaleStyle) throws ApplicationException {

        int commType = isSaleStyle ? MerchantCommission.COMMTYPE_SALE : MerchantCommission.COMMTYPE_PAYOUT;
        MerchantCommission merchantComm = this.getMerchantCommissionDao()
                .getByMerchantAndGame(merchant.getId(), gameId);
        if (merchantComm == null) {
            throw new ApplicationException(SystemException.CODE_MERCHANT_NO_GAME, "No game(id=" + gameId
                    + ") has been allocated to merchant(dd=" + merchant.getId() + ").");
        }
        merchant.setMerchantCommission(merchantComm);
        BigDecimal commRate = this.getActualCommRate(merchant, commType);

        BigDecimal comm = actualAmount.multiply(commRate);
        if (logger.isDebugEnabled()) {
            logger.debug("Calculate commssion for (merchantId=" + merchant.getId() + ",gameId=" + gameId + ",amount="
                    + actualAmount + ",commType=" + commType + ",commRate=" + commRate + "): " + comm);
        }
        return comm;
    }

    /**
     * Calculate the commission of a operator. A operator can gain commission from payout/sale transactions, furthermore
     * different game will be assigned a different commission rate.
     * 
     * @param operator
     *            Calculate commission for which operator.
     * @param merchant
     *            Calculate commission for which merchant.
     * @param gameId
     *            The payout/sale is upon which game
     * @param isSaleStyle
     *            Sale/cancel is sale style, however payout/cacnel is payout style.
     * @return the commission Rate for the operator according to the game commission rate.
     */
    private BigDecimal calculateCommission(Operator operator, Merchant merchant, String gameId, boolean isSaleStyle)
            throws ApplicationException {

        int commType = isSaleStyle ? MerchantCommission.COMMTYPE_SALE : MerchantCommission.COMMTYPE_PAYOUT;
        OperatorCommission operatorCommission = this.getOperatorCommissionDao().getByOperatorAndMerchantAndGame(
                operator.getId(), merchant.getId(), gameId);
        if (operatorCommission == null) {
            throw new ApplicationException(SystemException.CODE_OPERATOR_NO_MERCHANT, "No game(id=" + gameId
                    + ") has been allocated to operator(id=" + operator.getId() + ").");
        };
        BigDecimal commRate = this.getActualCommRate(operatorCommission, commType);

        if (logger.isDebugEnabled()) {
            logger.debug("Calculate commssion for (merchant=" + merchant.getId() + " +operator=" + operator.getId()
                    + ",gameId=" + gameId + ",commType=" + commType + ",commRate=" + commRate + ") ");
        }
        return commRate;
    }

    private BigDecimal getActualCommRate(Merchant merchant, int commType) {
        MerchantCommission merchantComm = merchant.getMerchantCommission();
        BigDecimal commRate = new BigDecimal("0");
        if (commType == MerchantCommission.COMMTYPE_PAYOUT) {
            commRate = merchantComm.getPayoutCommissionRate();
        } else if (commType == MerchantCommission.COMMTYPE_SALE) {

            commRate = merchantComm.getSaleCommissionRate();
        } else {
            throw new SystemException("Unsupported commission type:" + commType);
        }
        if (commRate.compareTo(new BigDecimal("0")) < 0) {
            throw new SystemException("The actual commission rate(merchantId=" + merchant.getId() + ",commType="
                    + commType + ") is " + commRate + ", it can NOT be less than 0.");
        }
        return commRate;
    }

    private BigDecimal getActualCommRate(OperatorCommission operatorCommission, int commType) {
        BigDecimal commRate = new BigDecimal("0");
        if (commType == MerchantCommission.COMMTYPE_PAYOUT) {
            commRate = operatorCommission.getPayoutRate();
        } else if (commType == MerchantCommission.COMMTYPE_SALE) {

            commRate = operatorCommission.getSaleRate();
        } else {
            throw new SystemException("Unsupported commission type:" + commType);
        }
        if (commRate.compareTo(new BigDecimal("0")) < 0) {
            throw new SystemException("The actual commission rate(operator=" + operatorCommission.getOperatorId()
                    + ",commType=" + commType + ") is " + commRate + ", it can NOT be less than 0.");
        }
        return commRate;
    }

    /**
     * Assemble the sale/payout credit balance of from/to operator/merchant.
     * 
     * @param dto
     *            The return DTO.
     * @param fromTarget
     *            Will be the from-operator, or from-merchant if from-operator's credit type is use-parent.
     * @param toTarget
     *            Will be the to-operator, or to-merchant if to-operator's credit type is use-parent.
     */
    protected void assembleCreditBalance(CreditTransferDto dto, Object fromTarget, Object toTarget)
            throws ApplicationException {
        if (CreditTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            if (fromTarget instanceof Operator) {
                dto.setCreditBalanceOfFromOperator(((Operator) fromTarget).getSaleCreditLevel());
            } else {
                dto.setCreditBalanceOfFromOperator(((Merchant) fromTarget).getSaleCreditLevel());
            }

            if (toTarget instanceof Operator) {
                dto.setCreditBalanceOfToOperator(((Operator) toTarget).getSaleCreditLevel());
            } else {
                dto.setCreditBalanceOfToOperator(((Merchant) toTarget).getSaleCreditLevel());
            }
        } else if (CreditTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            if (fromTarget instanceof Operator) {
                dto.setCreditBalanceOfFromOperator(((Operator) fromTarget).getPayoutCreditLevel());
            } else {
                dto.setCreditBalanceOfFromOperator(((Merchant) fromTarget).getPayoutCreditLevel());
            }

            if (toTarget instanceof Operator) {
                dto.setCreditBalanceOfToOperator(((Operator) toTarget).getPayoutCreditLevel());
            } else {
                dto.setCreditBalanceOfToOperator(((Merchant) toTarget).getPayoutCreditLevel());
            }
        } else if (CreditTransferDto.CREDITTYPE_COMMISSION == dto.getCreditType()) {
            if (fromTarget instanceof Operator) {
                dto.setCreditBalanceOfFromOperator(((Operator) fromTarget).getCommisionBalance());
            } else {
                dto.setCreditBalanceOfFromOperator(((Merchant) fromTarget).getCommisionBalance());
            }

            if (toTarget instanceof Operator) {
                dto.setCreditBalanceOfToOperator(((Operator) toTarget).getCommisionBalance());
            } else {
                dto.setCreditBalanceOfToOperator(((Merchant) toTarget).getCommisionBalance());
            }
        } else if (CreditTransferDto.CREDITTYPE_CASHOUT == dto.getCreditType()) {
            if (fromTarget instanceof Operator) {
                dto.setCreditBalanceOfFromOperator(((Operator) fromTarget).getCashoutBalance());
            } else {
                dto.setCreditBalanceOfFromOperator(((Merchant) fromTarget).getCashoutBalance());
            }

            if (toTarget instanceof Operator) {
                dto.setCreditBalanceOfToOperator(((Operator) toTarget).getCashoutBalance());
            } else {
                dto.setCreditBalanceOfToOperator(((Merchant) toTarget).getCashoutBalance());
            }
        }

        // insufficient balance
        if (dto.getCreditBalanceOfFromOperator().doubleValue() < 0) {
            throw new ApplicationException(SystemException.CODE_INSUFFICIENT_BALANCE, "Credit transfer login name("
                    + dto.getFromOperatorLoginName() + " insufficient balance)."+ dto.getCreditBalanceOfFromOperator().doubleValue());
        }
    }

    protected Object doCredit(Operator operator, Merchant merchant, BigDecimal amount, String gameId,
            boolean isRestore, boolean isSaleStyle, boolean isSoldByCrecitCard, Transaction transaction)
            throws ApplicationException {
        boolean isCalCommission = MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation();
        return this.doCredit(null, null, operator, merchant, amount, gameId, isRestore, isSaleStyle,
                isSoldByCrecitCard, isCalCommission, transaction);

    }

    /**
     * Calculate sale/payout credit level.
     * 
     * @return A Operator or Merchant from which the credit level is deducted/toptup.
     */
    protected Object doCredit(Context reqCtx, Context respCtx, Operator operator, Merchant merchant, BigDecimal amount,
            String gameId, boolean isRestore, boolean isSaleStyle, boolean isSoldByCrecitCard, boolean isCalCommission,
            Transaction transaction) throws ApplicationException {
        // 不管是define value or use parent都需要计算operator 的commissionBalance到流水记录。
        BalanceTransactions balanceTransactions = new BalanceTransactions();
        TransactionMessage transMsg = new TransactionMessage();
        if (transaction != null && isCalCommission) {
            if (transaction.getType() == TransactionType.SELL_TICKET.getRequestType()
                    || transaction.getType() == TransactionType.PAYOUT.getRequestType()
                    || transaction.getType() == TransactionType.VALIDATE_INSTANT_TICKET.getRequestType()) {

                BigDecimal commRate = this.calculateCommission(operator, merchant, gameId, isSaleStyle);
                BigDecimal commisionAmount = SimpleToolkit.mathMultiple(amount, commRate, MLotteryContext.getInstance()
                        .getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION));
                // amount.multiply(commRate).setScale(6, BigDecimal.ROUND_HALF_UP);
                // update cancelled transaction msg;
                balanceTransactions.setCommissionRate(commRate);
                balanceTransactions.setTransactionAmount(amount);
                balanceTransactions.setCommissionAmount(commisionAmount);
                transMsg.setTransactionId(transaction.getId());
                transMsg.setRequestMsg(new Gson().toJson(balanceTransactions));
            }
        }

        // Credit Transfer
        CreditTransferDto creditTransferDto = null;
        int creditType = 0;
        if (reqCtx != null && reqCtx.getModel() != null) {
            if (reqCtx.getModel() instanceof CreditTransferDto) {
                creditTransferDto = (CreditTransferDto) reqCtx.getModel();
                creditType = creditTransferDto.getCreditType();
            }
        }

        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == operator.getCreditType()) {

            /**
             * If try to refresh a entity before flush state changes into underlying database, the changes of entity
             * will be lost.
             */
            this.getEntityManager().flush();
            /**
             * Refresh entity to latest state of underlying database and lock it.
             */
            this.getEntityManager().refresh(operator, LockModeType.PESSIMISTIC_READ);
            if (logger.isDebugEnabled()) {
                logger.debug("The current credit level(before transaction) of " + operator + " is "
                        + "saleCreditLevel:" + operator.getSaleCreditLevel() + ",payoutCreditLevel:"
                        + operator.getPayoutCreditLevel());
            }
            if (isSaleStyle) {
                if (isSoldByCrecitCard && !merchant.isDeductSaleByCreditCard()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("The sale is paid by credit card, and no need to decut/topup sale credit of merchant("
                                + merchant + ").");
                    }
                    return operator;
                }
                if (!isRestore) { // sale
                    BigDecimal tmpCreditLevel = operator.getSaleCreditLevel().subtract(amount);
                    if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0) {
                        throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT,
                                "The balance of sale credit level(" + operator.getSaleCreditLevel() + ") of "
                                        + operator + " isn't enough for sale(amount=" + amount + ").");
                    }
                    operator.setSaleCreditLevel(tmpCreditLevel);
                } else { // sale cancellation
                    operator.setSaleCreditLevel(operator.getSaleCreditLevel().add(amount));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("The new sale credit level of " + operator + " is " + operator.getSaleCreditLevel());
                }
            } else {
                if (creditType == CreditTransferDto.CREDITTYPE_PAYOUT || creditType == 0) {
                    if (isRestore) { // payout
                        operator.setPayoutCreditLevel(operator.getPayoutCreditLevel().add(amount));
                        if (isCalCommission) {
                            operator.setCommisionBalance(operator.getCommisionBalance().add(
                                    balanceTransactions.getCommissionAmount()));
                        }
                        if (transaction != null && isCalCommission) {
                            transaction.setTransMessage(transMsg);
                        }
                    } else { // payout cancellation.
                        BigDecimal tmpCreditLevel = operator.getPayoutCreditLevel().subtract(amount);
                        operator.setPayoutCreditLevel(tmpCreditLevel);
                        if (isCalCommission) {
                            if (transaction != null) {
                                balanceTransactions = new Gson().fromJson(
                                        transaction.getTransMessage().getRequestMsg(), BalanceTransactions.class);
                                BigDecimal commisionAmount = operator.getCommisionBalance().subtract(
                                        balanceTransactions.getCommissionAmount());
                                operator.setCommisionBalance(commisionAmount);
                            }
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The new payout credit level of " + operator + " is "
                                + operator.getPayoutCreditLevel());
                    }
                } else if (creditType == CreditTransferDto.CREDITTYPE_COMMISSION) {
                    if (isRestore) { // commission
                        operator.setCommisionBalance(operator.getCommisionBalance().add(amount));
                        if (transaction != null && isCalCommission) {
                            transaction.setTransMessage(transMsg);
                        }
                    } else { // commission cancellation.
                        BigDecimal tmpCreditLevel = operator.getCommisionBalance().subtract(amount);
                        operator.setCommisionBalance(tmpCreditLevel);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The new commission credit level of " + operator + " is "
                                + operator.getCommisionBalance());
                    }
                } else if (creditType == CreditTransferDto.CREDITTYPE_CASHOUT) {
                    if (isRestore) { // CASH OUT
                        operator.setCashoutBalance(operator.getCashoutBalance().add(amount));
                        if (transaction != null) {
                            transaction.setTransMessage(transMsg);
                        }
                    } else { // CASH OUT cancellation.
                        BigDecimal tmpCreditLevel = operator.getCashoutBalance().subtract(amount);
                        operator.setCashoutBalance(tmpCreditLevel);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The new cashout credit level of " + operator + " is "
                                + operator.getCashoutBalance());
                    }
                }

            }
            this.getOperatorDao().update(operator);
            return operator;
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == operator.getCreditType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore credit level calculation, as the credit type of " + operator
                        + " is USE PARENT... check its parent merchant");
            }
            if (transaction != null && isCalCommission) {
                transaction.setTransMessage(transMsg);
            }
            return this.creditMerchant(reqCtx, merchant, amount, gameId, isRestore, isSaleStyle, isSoldByCrecitCard);
        } else {
            logger.info("Ignore calculation of credit level, as the credit type of " + operator + " is "
                    + operator.getCreditType());
            return null;
        }
    }

    /**
     * Check and calculate the sale/payout credit level of merchant.
     */
    private Merchant creditMerchant(Context reqCtx, Merchant merchant, BigDecimal amount, String gameId,
            boolean isRestore, boolean isSaleStyle, boolean isSoldByCreditCard) throws ApplicationException {

        // Credit Transfer
        CreditTransferDto creditTransferDto = null;
        int creditType = 0;
        if (reqCtx != null && reqCtx.getModel() != null) {
            if (reqCtx.getModel() instanceof CreditTransferDto) {
                creditTransferDto = (CreditTransferDto) reqCtx.getModel();
                creditType = creditTransferDto.getCreditType();
            }
        }

        if (merchant.getId() == Merchant.SUPER_MERCHANT_ID) {
            throw new SystemException("can't calculate credit level, as has reached the top merchant(id="
                    + Merchant.SUPER_MERCHANT_ID + ")");
        }
        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == merchant.getCreditType()) {
            /**
             * If try to refresh a entity before flush state changes into underlying database, the changes of entity
             * will be lost.
             */
            this.getEntityManager().flush();
            /**
             * Refresh entity to latest state of underlying database and lock it.
             */
            this.getEntityManager().refresh(merchant, LockModeType.PESSIMISTIC_READ);
            if (logger.isDebugEnabled()) {
                logger.debug("The current credit level(before transaction) of " + merchant + " is saleCreditLevel:"
                        + merchant.getSaleCreditLevel() + ",payoutCreditLevel:" + merchant.getPayoutCreditLevel());
            }
            BigDecimal commission = new BigDecimal("0");
            // if (gameId != null)
            // commission = this.calculateCommission(merchant, gameId, amount,
            // isSaleStyle);

            if (isSaleStyle) {
                if (isSoldByCreditCard && !merchant.isDeductSaleByCreditCard()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("The sale is paid by credit card, and no need to decut/topup sale credit "
                                + "of merchant(" + merchant + ").");
                    }
                    return merchant;
                }
                if (!isRestore) { // sale
                    BigDecimal tmpCreditLevel = merchant.getSaleCreditLevel().subtract(amount).add(commission);
                    if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0) {
                        throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT,
                                "The balance of sale credit level(" + merchant.getSaleCreditLevel() + ") of "
                                        + merchant + " isn't enough for sale(amount=" + amount + ").");
                    }
                    merchant.setSaleCreditLevel(tmpCreditLevel);
                } else { // sale cancellation
                    merchant.setSaleCreditLevel(merchant.getSaleCreditLevel().add(amount).subtract(commission));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("The new sale credit level of merchant" + merchant + " is "
                            + merchant.getSaleCreditLevel());
                }
            } else {
                if (creditType == CreditTransferDto.CREDITTYPE_PAYOUT || creditType == 0) {
                    if (isRestore) { // payout
                        merchant.setPayoutCreditLevel(merchant.getPayoutCreditLevel().add(amount).add(commission));
                    } else { // payout cancellation

                        BigDecimal tmpCreditLevel = merchant.getPayoutCreditLevel().subtract(amount)
                                .subtract(commission);
                        // allow negative payout balance
                        // if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0)
                        // throw new
                        // ApplicationException(SystemException.
                        // CODE_EXCEED_CREDITLIMIT,
                        // "The balance of payout credit level(" +
                        // merchant.getPayoutCreditLevel()
                        // + ") of " + merchant + " isn't enough for payout(amount="
                        // + amount
                        // + ").");
                        merchant.setPayoutCreditLevel(tmpCreditLevel);

                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The new payout credit level of merchant" + merchant + " is "
                                + merchant.getPayoutCreditLevel());
                    }
                } else if (creditType == CreditTransferDto.CREDITTYPE_COMMISSION) {
                    if (isRestore) { // commission
                        merchant.setCommisionBalance(merchant.getCommisionBalance().add(amount));
                    } else { // commission cancellation
                        BigDecimal tmpCreditLevel = merchant.getCommisionBalance().subtract(amount);
                        merchant.setCommisionBalance(tmpCreditLevel);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The new commission credit level of merchant" + merchant + " is "
                                + merchant.getCommisionBalance());
                    }
                } else if (creditType == CreditTransferDto.CREDITTYPE_CASHOUT) {
                    if (isRestore) { // commission
                        merchant.setCashoutBalance(merchant.getCashoutBalance().add(amount));
                    } else { // commission cancellation
                        BigDecimal tmpCreditLevel = merchant.getCashoutBalance().subtract(amount);
                        merchant.setCashoutBalance(tmpCreditLevel);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("The new cash out credit level of merchant" + merchant + " is "
                                + merchant.getCashoutBalance());
                    }
                }
            }
            this.getMerchantDao().update(merchant);
            return merchant;
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == merchant.getCreditType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore credit level calculation, as the credit type of" + merchant
                        + " is USE PARENT... check its parent merchant.");
            }
            // invoke method recursively
            if (merchant.getParentMerchant() == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find parent of merchant(id='"
                        + merchant.getId() + "').");
            }
            Merchant parent = this.getMerchantDao().findByIdForUpdate(merchant.getParentMerchant().getId());
            return this.creditMerchant(reqCtx, parent, amount, gameId, isRestore, isSaleStyle, isSoldByCreditCard);
        } else {
            logger.info("Ignore calculation of credit level, as the credit type of " + merchant + " is "
                    + merchant.getCreditType());
            return null;
        }
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

    public CreditTransferLogDao getCreditTransferLogDao() {
        return creditTransferLogDao;
    }

    public void setCreditTransferLogDao(CreditTransferLogDao creditTransferLogDao) {
        this.creditTransferLogDao = creditTransferLogDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * @return balanceTransactionsDao
     */
    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    /**
     * @param balanceTransactionsDao
     */
    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

    /**
     * @return operatorCommissionDao
     */
    public OperatorCommissionDao getOperatorCommissionDao() {
        return operatorCommissionDao;
    }

    /**
     * @param operatorCommissionDao
     */
    public void setOperatorCommissionDao(OperatorCommissionDao operatorCommissionDao) {
        this.operatorCommissionDao = operatorCommissionDao;
    }

}
