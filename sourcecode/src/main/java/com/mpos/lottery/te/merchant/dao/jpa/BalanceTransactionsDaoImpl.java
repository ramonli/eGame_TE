package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.TransactionType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class BalanceTransactionsDaoImpl extends BaseJpaDao implements BalanceTransactionsDao {
    private UUIDService uuidService;

    @Override
    public List<BalanceTransactions> findBalanceTransactions(String teTransactionId) {
        String sql = "from BalanceTransactions bt where bt.teTransactionId=:teTransactionId";
        Map params = new HashMap();
        params.put("teTransactionId", teTransactionId);
        return this.findByNamedParams(sql, params);
    }

    @Override
    public void updateBalanceTransactionsStatusByteTransactionId(String teTransactionId) {
        String sql = "update BalanceTransactions bt set bt.status=:status where bt.teTransactionId=:teTransactionId";
        Map params = new HashMap();
        params.put("status", BalanceTransactions.STATUS_INVALID);
        params.put("teTransactionId", teTransactionId);
        Query query = this.getEntityManager().createQuery(sql);
        Iterator<String> keyIt = params.keySet().iterator();
        while (keyIt.hasNext()) {
            String paramName = keyIt.next();
            query.setParameter(paramName, params.get(paramName));
        }
        query.executeUpdate();

    }

    public BalanceTransactions assembleBalanceTransactions(Context respCtx, BigDecimal amount)
            throws ApplicationException {
        BalanceTransactions balanceTransactions = new BalanceTransactions();
        // balanceTransactions.setId(this.getUuidService().getGeneralID());
        if (respCtx != null) {
            balanceTransactions.setOwnerId(respCtx.getOperatorId());
            balanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_OPERATOR);
            balanceTransactions.setTeTransactionId(respCtx.getTransactionID());
            balanceTransactions.setMerchantId(respCtx.getMerchant().getId());
            balanceTransactions.setOperatorId(respCtx.getOperatorId());
            balanceTransactions.setDeviceId(respCtx.getTerminalId());
            balanceTransactions.setTransactionType(respCtx.getTransaction().getType());
            balanceTransactions.setStatus(BalanceTransactions.STATUS_VALID);
            balanceTransactions.setPaymentType(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY);
            balanceTransactions.setOriginalTransType(respCtx.getTransaction().getType());
            if (respCtx.getTransType() == TransactionType.CANCEL_BY_TRANSACTION.getRequestType()) {
                balanceTransactions.setPaymentType(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);
            }
        }
        balanceTransactions.setTransactionAmount(amount);

        return balanceTransactions;

    }

    @Override
    public void addBalanceTransactionRecord(Context respCtx, BigDecimal cashoutAmount, int transType,
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
        bt.setTransactionAmount(cashoutAmount);
        bt.setCommissionAmount(commissionAmount);
        bt.setCommissionRate(commissionRate);
        bt.setCreateTime(net.mpos.fk.util.DateUtils.getNowTimestamp());
        bt.setStatus(BalanceTransactions.STATUS_VALID);
        this.insert(bt);
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    @Override
    public List<BalanceTransactions> findByOwnerAndTransaction(String transactionId, String ownerId) {
        String jpql = "from BalanceTransactions b where b.ownerId=:ownerId and b.teTransactionId=:transactionId";
        Query query = this.getEntityManager().createQuery(jpql);
        query.setParameter("ownerId", ownerId);
        query.setParameter("transactionId", transactionId);
        return (List<BalanceTransactions>) query.getResultList();
    }

    @Override
    public BalanceTransactions findByTransactionAndOwnerAndGameAndOrigTransType(String transactionId, String ownerId,
            String gameId, int origTransType) {
        String jpql = "from BalanceTransactions l where l.teTransactionId=:transactionId and "
                + "l.ownerId=:commissionOwnerId and l.originalTransType=:transType";
        if (gameId != null) {
            jpql += " and l.gameId=:gameId";
        }
        Query query = this.getEntityManager().createQuery(jpql);
        query.setParameter("transactionId", transactionId);
        query.setParameter("commissionOwnerId", ownerId + "");
        query.setParameter("transType", origTransType);
        if (gameId != null) {
            query.setParameter("gameId", gameId);
        }
        return (BalanceTransactions) this.single(query.getResultList(), false);
    }

    @Override
    public void addBalanceTransferBalanceTransactionRecord(Context reqCtx, BigDecimal balanceAmount, int transType,
            int orgTransType, Long fromParentMerchantid, Long toParentMerchantid, String operatorid, String ownerid,
            int ownerType, int paymentType) throws ApplicationException {
        BalanceTransactions bt = new BalanceTransactions();
        bt.setTeTransactionId(reqCtx.getTransaction().getId());
        bt.setMerchantId(reqCtx.getTransaction().getMerchantId());
        bt.setDeviceId(reqCtx.getTransaction().getDeviceId());
        bt.setOperatorId(operatorid);
        bt.setOwnerId(ownerid);
        bt.setOwnerType(BalanceTransactions.OWNER_TYPE_OPERATOR);
        bt.setPaymentType(paymentType);
        bt.setTransactionType(transType);
        bt.setOriginalTransType(orgTransType);
        bt.setTransactionAmount(balanceAmount);
        bt.setCommissionAmount(new BigDecimal("0"));
        bt.setCommissionRate(new BigDecimal("0"));
        bt.setFromParentMerchantId(fromParentMerchantid);
        bt.setToParentMerchantId(toParentMerchantid);
        bt.setCreateTime(net.mpos.fk.util.DateUtils.getNowTimestamp());
        bt.setStatus(BalanceTransactions.STATUS_VALID);
        this.insert(bt);
    }

}
