package com.mpos.lottery.te.trans.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.junit.Test;

import java.util.Date;

import javax.annotation.Resource;

public class TransactionDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;

    @Test
    public void testInsertAndGetById() {
        Transaction trans = this.mock();
        this.getTransactionDao().insert(trans);

        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, trans.getId());
        this.doAssertion(trans, dbTrans);
    }

    @Test
    public void testUpdate() {
        Transaction trans = this.mock();
        this.getTransactionDao().insert(trans);

        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, trans.getId());
        System.out.println("*********" + dbTrans.getClass().getDeclaredClasses());
        dbTrans.setDeviceId(111);
        dbTrans.setMerchantId(333);
        this.getTransactionDao().update(dbTrans);
        Transaction dbTrans1 = this.getTransactionDao().findById(Transaction.class, trans.getId());
        this.doAssertion(dbTrans, dbTrans1);
    }

    private Transaction mock() {
        Transaction trans = new Transaction();
        trans.setCreateTime(new Date());
        trans.setDeviceId(111);
        trans.setGpeId("GPE-111");
        trans.setId(uuid());
        trans.setMerchantId(111);
        trans.setOperatorId("O-111");
        trans.setResponseCode(200);
        trans.setTicketSerialNo("123456");
        trans.setTraceMessageId("TM-111");
        trans.setTransTimestamp(trans.getCreateTime());
        trans.setParentMerchants("001|002|003|004");
        trans.setBatchNumber("2009010101");
        trans.setType(201);
        trans.setVersion(100);

        return trans;
    }

    private void doAssertion(Transaction trans, Transaction dbTrans) {
        assertNotNull(dbTrans);
        // assertNull(dbTrans.getUpdateTime());
        assertEquals(trans.getDeviceId(), dbTrans.getDeviceId());
        assertEquals(trans.getGpeId(), dbTrans.getGpeId());
        assertEquals(trans.getId(), dbTrans.getId());
        assertEquals(trans.getMerchantId(), dbTrans.getMerchantId());
        assertEquals(trans.getOperatorId(), dbTrans.getOperatorId());
        assertEquals(trans.getResponseCode(), dbTrans.getResponseCode());
        assertEquals(trans.getTicketSerialNo(), dbTrans.getTicketSerialNo());
        assertEquals(trans.getTraceMessageId(), dbTrans.getTraceMessageId());
        assertEquals(date2String(trans.getTransTimestamp()), date2String(dbTrans.getTransTimestamp()));
        assertEquals(date2String(trans.getCreateTime()), date2String(dbTrans.getCreateTime()));
        assertEquals(trans.getParentMerchants(), dbTrans.getParentMerchants());
        assertEquals(trans.getBatchNumber(), dbTrans.getBatchNumber());
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

}
