package com.mpos.lottery.te.test.integration;

import com.mpos.lottery.te.port.InternalTEPortServlet;
import com.mpos.lottery.te.port.TEPortServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BaseInternalServletIntegrationTest extends BaseServletIntegrationTest {
    private static Log logger = LogFactory.getLog(BaseInternalServletIntegrationTest.class);

    @Override
    protected TEPortServlet newTEPortServlet() {
        TEPortServlet servlet = new InternalTEPortServlet();
        return servlet;
    }
}
