package com.mpos.lottery.te.trans.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionRetryLog;

import org.junit.Test;

import java.util.Date;

import javax.annotation.Resource;

public class TransactionRetryLogDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "transactionRetryLogDao")
    private TransactionRetryLogDao transactionRetryLogDao;

    @Test
    public void testInsert() {
        TransactionRetryLog log = this.mock();
        this.getTransactionRetryLogDao().insert(log);

        TransactionRetryLog dbLog = this.getTransactionRetryLogDao().getByTicketAndTransTypeAndDevice(
                log.getTicketSerialNo(), log.getTransType(), log.getDeviceId());
        this.doAssert(log, dbLog);
    }

    @Test
    public void testUpdate() {
        TransactionRetryLog log = this.mock();
        this.getTransactionRetryLogDao().insert(log);

        TransactionRetryLog dbLog = this.getTransactionRetryLogDao().getByTicketAndTransTypeAndDevice(
                log.getTicketSerialNo(), log.getTransType(), log.getDeviceId());
        dbLog.setCreateTime(new Date());
        dbLog.setTransType(201);
        dbLog.setTotalRetry(2);
        dbLog.setTicketSerialNo("T-112");
        TransactionRetryLog dbLog2 = this.getTransactionRetryLogDao().getByTicketAndTransTypeAndDevice(
                dbLog.getTicketSerialNo(), dbLog.getTransType(), 111);

        this.doAssert(dbLog, dbLog2);
    }

    private void doAssert(TransactionRetryLog log, TransactionRetryLog dbLog) {
        assertNotNull(dbLog);
        assertEquals(log.getId(), dbLog.getId());
        assertEquals(log.getVersion(), dbLog.getVersion());
        assertEquals(log.getTicketSerialNo(), dbLog.getTicketSerialNo());
        assertEquals(log.getTransType(), dbLog.getTransType());
        assertEquals(log.getTotalRetry(), dbLog.getTotalRetry());
        assertEquals(date2String(log.getCreateTime()), date2String(dbLog.getCreateTime()));
    }

    private TransactionRetryLog mock() {
        TransactionRetryLog log = new TransactionRetryLog();
        log.setId("TRL-111");
        log.setCreateTime(new Date());
        log.setTicketSerialNo("T-111");
        log.setTotalRetry(1);
        log.setTransType(200);
        log.setDeviceId(111);
        log.setVersion(TransactionRetryLog.VERSION_VALID);
        return log;
    }

    public TransactionRetryLogDao getTransactionRetryLogDao() {
        return transactionRetryLogDao;
    }

    public void setTransactionRetryLogDao(TransactionRetryLogDao transactionRetryLogDao) {
        this.transactionRetryLogDao = transactionRetryLogDao;
    }

}
