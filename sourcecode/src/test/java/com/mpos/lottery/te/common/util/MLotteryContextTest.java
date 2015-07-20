package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.MLotteryContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class MLotteryContextTest {
    private Log logger = LogFactory.getLog(MLotteryContextTest.class);

    @Test
    public void test() throws Exception {
        MLotteryContext ctx = MLotteryContext.getInstance();

        assertEquals("0000000000000000", ctx.get("triperdes.iv"));
        assertEquals("paygo24", ctx.get("coobill.agent"));
        assertEquals("xml-mapping/GetWorkingKey_Res.xml", ctx.get("transtype.4501"));
        // define this key in both te_vas_airtime.properties and te_core.properties
        assertEquals("123", ctx.get("database.waitforlock"));
    }
}
