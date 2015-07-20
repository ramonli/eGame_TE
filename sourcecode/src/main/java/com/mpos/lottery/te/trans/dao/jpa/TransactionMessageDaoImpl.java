package com.mpos.lottery.te.trans.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.trans.dao.TransactionMessageDao;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

public class TransactionMessageDaoImpl extends BaseJpaDao implements TransactionMessageDao {
    private Log logger = LogFactory.getLog(TransactionMessageDaoImpl.class);

    /**
     * @see TransactionMessageDao#getById(String).
     */
    public TransactionMessage getById(final String transactionId) throws DataAccessException {
        return this.findById(TransactionMessage.class, transactionId);
    }

}
