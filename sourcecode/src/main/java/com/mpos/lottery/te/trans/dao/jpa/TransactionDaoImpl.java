package com.mpos.lottery.te.trans.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class TransactionDaoImpl extends BaseJpaDao implements TransactionDao {
    private Log logger = LogFactory.getLog(TransactionDaoImpl.class);

    @Override
    public Transaction getByDeviceAndTraceMessage(long deviceId, String traceMessageId) throws DataAccessException {
        Map params = new HashMap(0);
        params.put("deviceId", deviceId);
        params.put("traceMessageId", traceMessageId);
        List trans = this.findByNamedParams("from Transaction t where "
                + "t.deviceId=:deviceId and t.traceMessageId=:traceMessageId", params);
        if (trans.size() > 0) {
            return (Transaction) trans.get(0);
        }
        return null;
    }

    /**
     * @see TransactionDao#getByBatchNumber(String, long, String, int).
     */
    public List<Transaction> getByBatchNumber(String operatorId, long merchantId, String batchNumber, int transType) {
        Map params = new HashMap(0);
        params.put("merchantId", merchantId);
        params.put("operatorId", operatorId);
        params.put("batchNumber", batchNumber);
        params.put("transType", (transType == TransactionType.CANCEL_DECLINED.getRequestType()
                ? TransactionType.CANCEL_BY_TICKET.getRequestType()
                : transType));
        params.put("responseCode", (transType == TransactionType.CANCEL_DECLINED.getRequestType()
                ? SystemException.CODE_FAILTO_CANCEL
                : SystemException.CODE_OK));

        return this.findByNamedParams("from Transaction t where" + this.getWhereClause(), params);
    }

    @Override
    public List<Transaction> getByBatchAndOperatorAndMerchantAndTransTypeAndResponse(String operatorId,
            long merchantId, String batchNumber, int transType, int responseCode) {
        String sql = "from Transaction t where " + "t.merchantId=:merchantId and t.operatorId=:operatorId  and "
                + "t.batchNumber=:batchNumber and t.type=:transType and t.responseCode=:responseCode";
        Map params = new HashMap(0);
        params.put("merchantId", merchantId);
        params.put("operatorId", operatorId);
        params.put("batchNumber", batchNumber);
        params.put("transType", transType);
        params.put("responseCode", responseCode);
        return this.findByNamedParams(sql, params);
    }

    /**
     * find all transaction by operatorId and batchNumber and merchantId.
     */
    public List<Transaction> getAllTransByBatchOperatorMerchant(String operatorId, long merchantId, String batchNo) {
        String sql = "from com.mpos.lottery.te.trans.domain.Transaction t where "
                + "t.merchantId=:merchantId and t.operatorId=:operatorId  and " + "t.batchNumber=:batchNumber";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("operatorId", operatorId);
        params.put("batchNumber", batchNo);
        return this.findByNamedParams(sql, params);
    }

    public static String getSimpleWhereClause() {
        return " t.merchantId=:merchantId and t.operatorId=:operatorId " + "and t.batchNumber=:batchNumber ";
    }

    private static String getWhereClause() {
        return getSimpleWhereClause() + " and t.type=:transType and t.responseCode=:responseCode";
    }

    @Override
    public Transaction getByTicketAndType(String serialNo, int transType) {
        String jpql = "from Transaction t where t.ticketSerialNo=:ticketSerialNo and t.type=:type";
        Query query = this.getEntityManager().createQuery(jpql);
        query.setParameter("ticketSerialNo", serialNo);
        query.setParameter("type", transType);
        return (Transaction) this.single(query.getResultList(), false);
    }

    /**
     * Get all transactions according to serial
     * */
    @Override
    public List<Transaction> getAllByTicketSerial(String serialNo) {
        String jpql = "from Transaction t where t.ticketSerialNo=:ticketSerialNo ";
        Query query = this.getEntityManager().createQuery(jpql);
        query.setParameter("ticketSerialNo", serialNo);

        return query.getResultList();
    }

}
