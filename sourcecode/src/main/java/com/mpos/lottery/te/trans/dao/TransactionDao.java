package com.mpos.lottery.te.trans.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.springframework.dao.DataAccessException;

import java.util.List;

public interface TransactionDao extends DAO {

    Transaction getByDeviceAndTraceMessage(long deviceId, String traceMessaegId) throws DataAccessException;

    /**
     * find all transaction by operatorId and batchNumber and merchantId.
     */
    public List<Transaction> getAllTransByBatchOperatorMerchant(String operatorId, long merchantId, String batchNumber);

    // void delete(String id) throws DataAccessException;

    /**
     * Avoid fetch all settlement transactions, only the transactions with specified transtype will be fetched.
     */
    List<Transaction> getByBatchNumber(String operatorId, long merchantId, String batchNumber, int transType);

    /**
     * Fetch all transactions of specified transType in a batch.
     */
    List<Transaction> getByBatchAndOperatorAndMerchantAndTransTypeAndResponse(String operatorId, long merchantId,
            String batchNumber, int transType, int responseCode);

    Transaction getByTicketAndType(String serialNo, int transType);

    /**
     * Get all transactions according to serial.
     * */
    List<Transaction> getAllByTicketSerial(String serialNo);
}
