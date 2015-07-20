package com.mpos.lottery.te.test.integration;

import com.mpos.lottery.te.common.encrypt.HMacMd5Cipher;
import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.RequestHeaders;
import com.mpos.lottery.te.port.TEPortServlet;
import com.mpos.lottery.te.port.protocol.CastorHelper;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class BaseServletIntegrationTest extends BaseTransactionalIntegrationTest {
    private static Log logger = LogFactory.getLog(BaseServletIntegrationTest.class);
    /**
     * The key definition must be identical with the data defined in oracle_testdata.sql If you want to run test,
     * oracle_testdata.sql must be performed once per day.
     */
    public static String KEY_DATA = "W0JAMWQ1MGZkMjc2N2U2M2Y2LWVkYTIt";
    public static String KEY_MAC = "P2Bbo6+bSSR9O2Qc89f3b4oHTyE1V2gF";
    protected TEPortServlet servlet;

    /**
     * logic to verify the initial state before a transaction is started.
     * <p/>
     * The @BeforeTransaction methods declared in superclass will be run after those of the current class.
     */
    @BeforeTransaction
    public void prepareServlet() throws Exception {
        logger.trace("@BeforeTransaction:verifyInitialDatabaseState()");
        // logger.debug("EntityManager:" + this.entityManager);
        // logger.debug("ApplicationContext:" + this.applicationContext);

        // print all registered BeanPostProcessor.
        ConfigurableApplicationContext ac = (ConfigurableApplicationContext) this.applicationContext;
        Map<String, BeanPostProcessor> map = ac.getBeanFactory().getBeansOfType(BeanPostProcessor.class);
        Iterator<String> keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            logger.trace("BeanPostProcessor - " + key + ":" + map.get(key));
        }

        // initialize MLotteryContext
        MLotteryContext.getInstance().setBeanFactory(this.applicationContext);

        // initialize DispatcherServlet
        MockServletContext servletCtx = new MockServletContext();
        // must set this ApplicationContext which loaded by spring test-context
        // framework to ServletContext, it will be the parent of the
        // ApplicationContext loaded by DispatcherServlet from XXX-servlet.xml.
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.applicationContext);
        MockServletConfig servletCfg = new MockServletConfig(servletCtx, "lottery");
        servlet = new TEPortServlet();
        // initialize the servlet
        servlet.init(servletCfg);
    }

    protected final Context doPost(MockHttpServletRequest req) throws Exception {
        TEPortServlet servlet = newTEPortServlet();
        MockHttpServletResponse mockResp = this.mockResponse();
        servlet.doPost(req, mockResp);
        // NOTE: shouldn't end transaction here, as the concret sub test class
        // may wants different logic, such as setComplete(). And the most
        // important is that endTransaction() will rollback transaction by
        // default, once a teatcase(it assume the transaction is successful) try
        // a assert a database column, it will fail.....
        // end transaction, default to rollback. You can setComplete() to mark
        // transaction as commited, or setDefaultRollback() to mark it as
        // rollback.
        // this.endTransaction();
        return this.assembleContextFromResponse(mockResp);
    }

    protected TEPortServlet newTEPortServlet() {
        TEPortServlet servlet = new TEPortServlet();
        return servlet;
    }

    /**
     * Make a mock servlet request.
     * 
     * @param reqContext
     *            The request context.
     */
    protected MockHttpServletRequest mockRequest(Context reqContext) throws Exception {
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        // assemble request headers
        this.assembleReqHeaders(reqContext, mockReq);
        // assemble request body, including DES/MAC
        this.assembleReqBody(reqContext, mockReq);

        return mockReq;
    }

    protected MockHttpServletResponse mockResponse() {
        return new MockHttpServletResponse();
    }

    /**
     * Assemble Context from http response.
     */
    protected Context assembleContextFromResponse(MockHttpServletResponse mockResp) throws Exception {
        Context responseCtx = new Context();
        responseCtx.setProtocalVersion((String) mockResp.getHeader(Context.HEADER_PROTOCAL_VERSION));
        Gpe gpe = new Gpe();
        gpe.setId((String) mockResp.getHeader(Context.HEADER_GPE_ID));
        responseCtx.setGpe(gpe);
        responseCtx.setResponseCode(Integer.parseInt((String) mockResp.getHeader(Context.HEADER_REPONSE_CODE)));
        responseCtx.setStrTimestamp((String) mockResp.getHeader(Context.HEADER_TIMESTAMP));
        responseCtx.setTransType(Integer.parseInt((String) mockResp.getHeader(Context.HEADER_TRANSACTION_TYPE)));

        if (mockResp.getHeader(Context.HEADER_BATCHNUMBER) != null) {
            responseCtx.setBatchNumber((String) mockResp.getHeader(Context.HEADER_BATCHNUMBER));
        }
        if (mockResp.getHeader(Context.HEADER_MAC) != null) {
            responseCtx.setMac((String) mockResp.getHeader(Context.HEADER_MAC));
        }
        if (mockResp.getHeader(Context.HEADER_OPERATOR_ID) != null) {
            responseCtx.setOperatorId((String) mockResp.getHeader(Context.HEADER_OPERATOR_ID));
        }
        if (mockResp.getHeader(Context.HEADER_TRANSACTION_ID) != null) {
            responseCtx.setTransactionID((String) mockResp.getHeader(Context.HEADER_TRANSACTION_ID));
        }
        if (mockResp.getHeader(Context.HEADER_TRACE_MESSAGE_ID) != null) {
            responseCtx.setTraceMessageId((String) mockResp.getHeader(Context.HEADER_TRACE_MESSAGE_ID));
        }
        if (mockResp.getHeader(Context.HEADER_TERMINAL_ID) != null) {
            responseCtx.setStrTerminalId((String) mockResp.getHeader(Context.HEADER_TERMINAL_ID));
        }
        if (mockResp.getHeader(Context.HEADER_GAME_TYPE_ID) != null) {
            responseCtx.setGameTypeId((String) mockResp.getHeader(Context.HEADER_GAME_TYPE_ID));
        }

        // handle response body, trim() must be called, or '\r\n' will cause
        // fail to decryption.
        String body = mockResp.getContentAsString().trim();
        // System.out.println("Encrypted response(hash=" + body.hashCode() +
        // ",length=" + body.length() + "):" + body);
        responseCtx.setEncrptedBody(body);
        responseCtx.setOriginalBody(body);
        int transType = responseCtx.getTransType();
        if (transType != TransactionType.GET_WORKING_KEY.getResponseType() && !responseCtx.getEncrptedBody().equals("")) {
            // do decryption
            String xml = TriperDESCipher.decrypt(getDefaultWorkingKey().getDataKey(), body, MLotteryContext
                    .getInstance().getTriperDesIV());
            responseCtx.setOriginalBody(xml);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got response(code=" + responseCtx.getResponseCode() + ",length="
                    + responseCtx.getEncrptedBody().length() + "): " + responseCtx.getOriginalBody());
        }

        if (responseCtx.getOriginalBody() != null && !responseCtx.getOriginalBody().equals("")) {
            Object model = CastorHelper.unmarshal(responseCtx.getOriginalBody(), MLotteryContext.getInstance()
                    .getMappingFile(transType, responseCtx.getProtocalVersion(), responseCtx.getGameTypeId()));
            responseCtx.setModel(model);
        }

        return responseCtx;
    }

    protected void assembleReqHeaders(Context ctx, MockHttpServletRequest req) {
        req.addHeader(Context.HEADER_PROTOCAL_VERSION, ctx.getProtocalVersion());
        req.addHeader(Context.HEADER_GPE_ID, ctx.getGpe().getId());
        req.addHeader(Context.HEADER_TRANSACTION_TYPE, ctx.getTransType() + "");
        req.addHeader(Context.HEADER_TIMESTAMP, ctx.getStrTimestamp());
        req.addHeader(Context.HEADER_BATCHNUMBER, ctx.getBatchNumber());
        req.addHeader(Context.HEADER_TRACE_MESSAGE_ID, ctx.getTraceMessageId());
        req.addHeader(Context.HEADER_OPERATOR_ID, ctx.getOperatorId());
        req.addHeader(Context.HEADER_TERMINAL_ID, ctx.getTerminalId() + "");
        if (ctx.getGameTypeId() != null) {
            req.addHeader(Context.HEADER_GAME_TYPE_ID, ctx.getGameTypeId());
        }
        if (ctx.getGpsLocation() != null) {
            req.addHeader(RequestHeaders.HEADER_GPS_LOCATION, ctx.getGpsLocation());
        }
    }

    /**
     * Assemble the body of request.
     * 
     * @param ctx
     *            THe context of request
     * @param mockReq
     *            THe mocked servlet request.
     * @param gameType
     *            The game type which will be used to identify the castor mapping file.
     */
    protected void assembleReqBody(Context ctx, MockHttpServletRequest mockReq) throws Exception {
        TransactionType transType = TransactionType.getTransactionType(ctx.getTransType());

        // Changed by James on 24th June to void mac missing

        if (!transType.isRequireBody()) {
            String tempmacString = ctx.getMacString().trim();
            String tempMac = HMacMd5Cipher.doDigest(tempmacString, ctx.getWorkingKey().getMacKey());
            mockReq.addHeader(Context.HEADER_MAC, tempMac);
            return;
        }
        // do MAC and DES
        if (ctx.getModel() != null) {
            String mappingFile = MLotteryContext.getInstance().getMappingFile(ctx.getTransType(),
                    ctx.getProtocalVersion(), ctx.getGameTypeId());
            String xml = CastorHelper.marshal(ctx.getModel(), mappingFile);
            ctx.setOriginalBody(xml);

            // do DES encryption
            String encryptedXml = TriperDESCipher.encrypt(ctx.getWorkingKey().getDataKey(), ctx.getOriginalBody(),
                    MLotteryContext.getInstance().getTriperDesIV());
            ctx.setEncrptedBody(encryptedXml);
        }
        String macString = ctx.getMacString().trim();
        String mac = HMacMd5Cipher.doDigest(macString, ctx.getWorkingKey().getMacKey());
        if (logger.isDebugEnabled()) {
            logger.debug("Do MAC(key=" + ctx.getWorkingKey().getMacKey() + ") on (" + macString + ",hash="
                    + macString.hashCode() + "), get digest:" + mac);
        }
        ctx.setMac(mac);
        // Set mac header and message body
        mockReq.addHeader(Context.HEADER_MAC, mac);
        mockReq.setContent(ctx.getEncrptedBody().getBytes());
    }

    /**
     * Subclass can overwrite this method to customize context.
     */
    protected Context getDefaultContext(int transType, Object model) throws Exception {
        if (!TransactionType.isSupported(transType)) {
            throw new IllegalArgumentException("unsupported transaction type:" + transType);
        }
        if (model == null) {
            throw new IllegalArgumentException("argument 'model' can NOT be null.");
        }

        Context ctx = new Context();
        ctx.setProtocalVersion("1.5");
        Gpe gpe = new Gpe();
        gpe.setId("GPE-111");
        ctx.setGpe(gpe);
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // ctx.setTimestamp(sdf.parse("20091201121212"));
        ctx.setTimestamp(new Date());
        ctx.setTransType(transType);
        ctx.setTerminalId(111);
        ctx.setGpsLocation("40.714353,-74.005973");
        Merchant merchant = new Merchant();
        merchant.setId(111);
        ctx.setMerchant(merchant);
        ctx.setTraceMessageId(System.currentTimeMillis() + "");
        ctx.setOperatorId("OPERATOR-111");
        ctx.setBatchNumber(SimpleToolkit.formatDate(new Date(), "yyyyMMdd"));
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        ctx.setModel(model);
        // set working key
        ctx.setWorkingKey(this.getDefaultWorkingKey());
        return ctx;
    }

    protected Context getDefaultContext(int transType) throws Exception {
        if (!TransactionType.isSupported(transType)) {
            throw new IllegalArgumentException("unsupported transaction type:" + transType);
        }

        Context ctx = new Context();
        ctx.setProtocalVersion("1.5");
        Gpe gpe = new Gpe();
        gpe.setId("GPE-111");
        ctx.setGpe(gpe);
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // ctx.setTimestamp(sdf.parse("20091201121212"));
        ctx.setTimestamp(new Date());
        ctx.setTransType(transType);
        ctx.setTerminalId(111);
        ctx.setGpsLocation("40.714353,-74.005973");
        Merchant merchant = new Merchant();
        merchant.setId(111);
        ctx.setMerchant(merchant);
        ctx.setTraceMessageId(System.currentTimeMillis() + "");
        ctx.setOperatorId("OPERATOR-111");
        ctx.setBatchNumber(SimpleToolkit.formatDate(new Date(), "yyyyMMdd"));
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        // set working key
        ctx.setWorkingKey(this.getDefaultWorkingKey());
        return ctx;
    }

    protected WorkingKey getDefaultWorkingKey() {
        WorkingKey workingKey = new WorkingKey();
        workingKey.setDataKey(KEY_DATA);
        workingKey.setMacKey(KEY_MAC);
        return workingKey;
    }
}
