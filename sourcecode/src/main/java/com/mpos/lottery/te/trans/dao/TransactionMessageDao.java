package com.mpos.lottery.te.trans.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.springframework.dao.DataAccessException;

public interface TransactionMessageDao extends DAO {

    /**
     * Fetch TransactionMessage by the identifier of transaction. In fact, the identifier of transaction is also the
     * identifier of TransactionMessage.
     * 
     * @param transactionId
     *            The identifier of transaction.
     * @return a TransactionMessage with a specified transactionId.
     * @throws DataAccessException
     *             when encounter any exception.
     */
    TransactionMessage getById(String transactionId) throws DataAccessException;

}
