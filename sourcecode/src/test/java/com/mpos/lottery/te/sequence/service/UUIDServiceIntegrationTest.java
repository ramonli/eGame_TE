package com.mpos.lottery.te.sequence.service;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.sequence.domain.Sequence;
import com.mpos.lottery.te.sequence.domain.TicketSerialSpec;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.math.BigInteger;

import javax.annotation.Resource;

/**
 * To run this integration test, you have to load a fresh database first.
 * 
 * @author Ramon
 * 
 */
public class UUIDServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "uuidManager")
    private UUIDService uuidService;

    @Test
    public void testGetReferenceNo() throws Exception {
        printMethod();
        String serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        System.out.println(serialNo);
        assertEquals(20, serialNo.length());
        assertEquals(1, parseSequenceOfSerialNo(serialNo).intValue());
        // retrieve 2
        serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        assertEquals(2, parseSequenceOfSerialNo(serialNo).intValue());
        // retrieve 3
        serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        assertEquals(3, parseSequenceOfSerialNo(serialNo).intValue());
        // retrieve 4
        serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        assertEquals(4, parseSequenceOfSerialNo(serialNo).intValue());
        // reset all...then 5..5000 will be skipped
        this.getUuidService().reset(null);
        serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        assertEquals(5001, parseSequenceOfSerialNo(serialNo).intValue());
        // skip 5000..10000
        this.getUuidService().reset(null);
        serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        assertEquals(10001, parseSequenceOfSerialNo(serialNo).intValue());
        serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        assertEquals(10002, parseSequenceOfSerialNo(serialNo).intValue());

        logger.debug("Run to the end of cycle.");
        // reach tail of sequence
        for (int i = 0; i < 198; i++) {
            this.getUuidService().reset(Sequence.NAME_REFERENCE_NO);
            serialNo = this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE);
        }
        assertEquals(1, parseSequenceOfSerialNo(serialNo).intValue());
    }

    @Test
    public void testGetSerialNo() throws Exception {
        printMethod();
        String serialNo = this.getUuidService().getTicketSerialNo(1);
        System.out.println(serialNo);

        assertEquals(20, serialNo.length());
        assertEquals(1, parseSequenceOfSerialNo(serialNo).intValue());
        // retrieve 2
        serialNo = this.getUuidService().getTicketSerialNo(1);
        assertEquals(2, parseSequenceOfSerialNo(serialNo).intValue());
        // retrieve 3
        serialNo = this.getUuidService().getTicketSerialNo(1);
        assertEquals(3, parseSequenceOfSerialNo(serialNo).intValue());
        // retrieve 4
        serialNo = this.getUuidService().getTicketSerialNo(1);
        assertEquals(4, parseSequenceOfSerialNo(serialNo).intValue());
        // reset all...then 5..5000 will be skipped
        this.getUuidService().reset(null);
        serialNo = this.getUuidService().getTicketSerialNo(1);
        assertEquals(5001, parseSequenceOfSerialNo(serialNo).intValue());
        // skip 5001..10000
        this.getUuidService().reset(null);
        serialNo = this.getUuidService().getTicketSerialNo(1);
        assertEquals(10001, parseSequenceOfSerialNo(serialNo).intValue());
        serialNo = this.getUuidService().getTicketSerialNo(1);
        assertEquals(10002, parseSequenceOfSerialNo(serialNo).intValue());
    }

    @Test
    public void testGetTransactionId() throws Exception {
        printMethod();
        // System will adjust the illegal setting automatically
        String id = this.getUuidService().getGeneralID();
        System.out.println(id);

        // nextMin = oldSeq.nextMax + 1
        assertEquals(1, parseSequenceOfTransId(id, 7).intValue());
        id = this.getUuidService().getGeneralID();
        assertEquals(2, parseSequenceOfTransId(id, 7).intValue());
        // reset...1..5000 will be skipped
        this.getUuidService().reset(Sequence.NAME_GENERAL);
        id = this.getUuidService().getGeneralID();
        assertEquals(5001, parseSequenceOfTransId(id, 7).intValue());
    }

    protected BigInteger parseSequenceOfSerialNo(String serialNo) throws ApplicationException {
        TicketSerialSpec spec = new TicketSerialSpec(serialNo);
        String strSeq = spec.getSequence();
        return new BigInteger(strSeq);
    }

    protected BigInteger parseSequenceOfTransId(String id, int seqStartPosition) throws ApplicationException {
        String strSeq = id.substring(seqStartPosition);
        return new BigInteger(strSeq);
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

}
