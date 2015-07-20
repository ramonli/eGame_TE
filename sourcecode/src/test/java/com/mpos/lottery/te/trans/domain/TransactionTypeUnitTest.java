package com.mpos.lottery.te.trans.domain;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;

import org.junit.Test;

public class TransactionTypeUnitTest {

    @Test
    public void testGetRequestType() {
        TransactionType type = TransactionType.BATCH_OF_UPLOAD;
        System.out.println(type.valueOf("BATCH_OF_UPLOAD"));
        TransactionType types[] = TransactionType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println(types[i].getResponseType());
        }
    }

    @Test(expected = SystemException.class)
    public void testGetResponseType() {
        int type = 501;
        TransactionType trans = TransactionType.getTransactionType(type);
        assertEquals(4501, trans.getResponseType());

        type = 7123;
        TransactionType.getTransactionType(type);
    }

}
