package com.mpos.lottery.te.merchant.cancel;

import com.google.gson.Gson;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.GsonCashOutOperator;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import net.mpos.fk.util.DateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

public class CashoutOperatorCancellation extends AbstractReversalOrCancelStrategy {
    private Log logger = LogFactory.getLog(CashoutOperatorCancellation.class);

    private UUIDService uuidManager;
    private MerchantDao merchantDao;
    private OperatorDao operatorDao;
    private BalanceTransactionsDao balanceTransactionsDao;

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        if (targetTrans.getTransMessage() == null || targetTrans.getTransMessage().getRequestMsg() == null) {
            logger.warn("No ransaction message found.");
            return false;
        }
        GsonCashOutOperator dto = new Gson().fromJson(targetTrans.getTransMessage().getRequestMsg(),
                GsonCashOutOperator.class);
        Operator operator = this.getOperatorDao().findById(Operator.class, respCtx.getOperatorId());
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "can NOT find operator by id='"
                    + respCtx.getOperatorId() + "'.");
        }

        Merchant merchant = this.getMerchantDao().findById(Merchant.class, respCtx.getMerchant().getId());
        if (merchant == null) {
            throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find merchant by id='"
                    + respCtx.getMerchant().getId() + "'.");
        }

        // {"operatorMerchantType":1,"operatorMerchantid":"OPERATOR-112","totalAmount":100,"commission":20,"payout":30,"cashout":50,
        // "plusOperatorMerchantType":1,"plusOperatorid":"OPERATOR-111","plusOperatorCashoutBalance":100,"plusOperatorCommissionBalance":0,"plusOperatorCommissionRate":0,
        // "plusMerchantCashoutBalance":0,"plusMerchantCommissionBalance":0,"plusMerchantCommissionRate":0}

        // cancellation , reversal the logic balance
        if (dto.getOperatorMerchantType() == BalanceTransactions.OWNER_TYPE_OPERATOR) {
            this.operatorDao.deductBalanceByOperatorCancel(dto.getCommission(), dto.getPayout(), dto.getCashout(),
                    dto.getOperatorMerchantid());
        } else if (dto.getOperatorMerchantType() == BalanceTransactions.OWNER_TYPE_MERCHANT) {
            this.merchantDao.deductBalanceByMerchantCancel(dto.getCommission(), dto.getPayout(), dto.getCashout(),
                    Long.parseLong(dto.getOperatorMerchantid()));
        }

        // service center staff
        if (dto.getPlusOperatorMerchantType() == BalanceTransactions.OWNER_TYPE_OPERATOR) {
            this.operatorDao.addCashoutAndCommissionToOperatorCancel(dto.getPlusOperatorCashoutBalance(), null,
                    dto.getPlusOperatorid());
        } else if (dto.getPlusOperatorMerchantType() == BalanceTransactions.OWNER_TYPE_MERCHANT) {
            this.merchantDao.addCashoutAndCommissionToMerchantCancel(dto.getPlusMerchantCashoutBalance(), null,
                    Long.parseLong(dto.getPlusMerchantid()));
        }

        // update balance_transaction status to invalid
        balanceTransactionsDao.updateBalanceTransactionsStatusByteTransactionId(targetTrans.getTransMessage()
                .getTransactionId());

        BalanceTransactions balanceTransactions1 = this.assembleBalanceTransactions(respCtx, dto,
                dto.getOperatorMerchantid(), dto.getOperatorMerchantType(), dto.getTotalAmount(), new BigDecimal("0"),
                new BigDecimal("0"), targetTrans, BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY);
        balanceTransactionsDao.insert(balanceTransactions1);

        if (dto.getPlusOperatorMerchantType() == BalanceTransactions.OWNER_TYPE_OPERATOR) {
            BalanceTransactions balanceTransactions21 = this.assembleBalanceTransactions(respCtx, dto, dto
                    .getPlusOperatorid(), dto.getPlusOperatorMerchantType(), new BigDecimal(dto
                    .getPlusOperatorCashoutBalance().doubleValue()), BalanceTransactions.ZERO.subtract(dto
                    .getPlusOperatorCommissionBalance()), dto.getPlusOperatorCommissionRate(), targetTrans,
                    BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);
            balanceTransactionsDao.insert(balanceTransactions21);
        } else if (dto.getPlusOperatorMerchantType() == BalanceTransactions.OWNER_TYPE_MERCHANT) {
            BalanceTransactions balanceTransactions21 = this.assembleBalanceTransactions(respCtx, dto, dto
                    .getPlusOperatorid(), dto.getPlusOperatorMerchantType(), new BigDecimal(dto
                    .getPlusOperatorCashoutBalance().doubleValue()), BalanceTransactions.ZERO.subtract(dto
                    .getPlusOperatorCommissionBalance()), dto.getPlusOperatorCommissionRate(), targetTrans,
                    BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);
            balanceTransactionsDao.insert(balanceTransactions21);

            BalanceTransactions balanceTransactions22 = this.assembleBalanceTransactions(respCtx, dto, dto
                    .getPlusMerchantid(), dto.getPlusOperatorMerchantType(), new BigDecimal(dto
                    .getPlusMerchantCashoutBalance().doubleValue()), new BigDecimal("0"), new BigDecimal("0"),
                    targetTrans, BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);
            balanceTransactionsDao.insert(balanceTransactions22);
        }

        // SET TRANSACTION
        respCtx.getTransaction().setTotalAmount(targetTrans.getTotalAmount());
        respCtx.getTransaction().setDestinationOpeator(dto.getOperatorid());
        return false;
    }

    private BalanceTransactions assembleBalanceTransactions(Context respCtx, GsonCashOutOperator dto, String ownerid,
            int ownerType, BigDecimal balanceAmount, BigDecimal commissionAmount, BigDecimal commissionRate,
            Transaction targetTrans, int paymentType) throws ApplicationException {
        BalanceTransactions balanceTransactions = new BalanceTransactions();
        if (respCtx != null) {
            balanceTransactions.setTeTransactionId(respCtx.getTransactionID());
            balanceTransactions.setMerchantId(respCtx.getMerchant().getId());
            balanceTransactions.setOperatorId(respCtx.getOperatorId());
            balanceTransactions.setDeviceId(respCtx.getTerminalId());
            balanceTransactions.setTransactionType(TransactionType.CANCEL_BY_TRANSACTION.getRequestType());
            balanceTransactions.setOriginalTransType(targetTrans.getType());
            balanceTransactions.setStatus(BalanceTransactions.STATUS_VALID);
        }

        balanceTransactions.setOwnerId(ownerid);
        balanceTransactions.setOwnerType(ownerType);
        balanceTransactions.setPaymentType(paymentType);

        balanceTransactions.setTransactionAmount(balanceAmount);
        balanceTransactions.setCommissionAmount(commissionAmount);
        balanceTransactions.setCommissionRate(commissionRate);

        balanceTransactions.setCreateTime(DateUtils.getNowTimestamp());

        return balanceTransactions;

    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

}
