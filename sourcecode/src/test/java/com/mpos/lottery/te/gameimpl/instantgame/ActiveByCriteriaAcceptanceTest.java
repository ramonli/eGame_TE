package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class ActiveByCriteriaAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testActive() throws Exception {
        Context response = this.doPost();
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.ACTIVE_IG_BY_CRITERIA.getRequestType());
        ActiveCriteria criteria = new ActiveCriteria();
        criteria.setType(ActiveCriteria.TYPE_BYFIRSTTICKET);
        criteria.setValue("198415681100");
        // criteria.setValue("200415681000");
        // criteria.setType(ActiveCriteria.TYPE_BYRANGETICKET);
        // criteria.setValue("984161896310,984161896320");
        ctx.setModel(criteria);
    }

}
