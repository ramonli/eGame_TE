package com.mpos.lottery.te.trans.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.junit.Test;

import javax.annotation.Resource;

public class TransactionMessageDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "transMessageDao")
    private TransactionMessageDao transMessageDao;

    @Test
    public void testInsert() {
        this.printMethod();
        final TransactionMessage transMsg = this.mock();
        this.getTransMessageDao().insert(transMsg);

        // Use ID to query a just inserted entity, the hibernate-implemented JPA
        // runtime won't hit the underlying database.
        TransactionMessage dbMsg = this.getTransMessageDao().getById(transMsg.getTransactionId());
        /**
         * If query database by JDBC directly(bypass JPA runtime), there will be no result set returned. As we insert
         * entity by JPA runtime, and no any query against this entity from inside of JPA runtime, the insert action
         * won't be synchronized to the underlying database.
         * <p/>
         * So after the insert action, if query the entity from inside JPA runtime, the entity will be returned; If
         * query from outside the JPA runtime, no entity will be returned.
         */
        // TransactionMessage dbMsg = (TransactionMessage)
        // this.getJdbcTemplate().queryForObject(
        // "select TRANSACTION_ID from TE_TRANSACTION_MSG where TRANSACTION_ID='"
        // + transMsg.getTransactionId() + "'", new RowMapper() {
        //
        // @Override
        // public Object mapRow(ResultSet rs, int arg1) throws SQLException {
        // TransactionMessage object = new TransactionMessage();
        // object.setTransactionId(rs.getString(1));
        // return object;
        // }
        //
        // });
        assertNotNull(dbMsg);
        this.doAssertion(transMsg, dbMsg);
    }

    private TransactionMessage mock() {
        TransactionMessage msg = new TransactionMessage();
        msg.setTransactionId(uuid());
        msg.setResponseMsg("response msg");
        msg.setRequestMsg("request msg");
        return msg;
    }

    private void doAssertion(TransactionMessage msg, TransactionMessage dbMsg) {
        assertNotNull(dbMsg);
        // assertEquals(msg.getTransaction().getId(),
        // dbMsg.getTransaction().getId());
        assertEquals(msg.getRequestMsg(), dbMsg.getRequestMsg());
        assertEquals(msg.getResponseMsg(), dbMsg.getResponseMsg());
    }

    public TransactionMessageDao getTransMessageDao() {
        return transMessageDao;
    }

    public void setTransMessageDao(TransactionMessageDao transMessageDao) {
        this.transMessageDao = transMessageDao;
    }

}
