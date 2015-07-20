package com.mpos.lottery.te.test.accptancetest;

import com.mpos.lottery.te.common.encrypt.HMacMd5Cipher;
import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.protocol.CastorHelper;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.AuthSSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public abstract class BaseAcceptanceTest {
    public static String GPE_ID = "GPE-111";
    public static int DEVICE_ID = 111;
    public static String OPERATOR_ID = "OPERATOR-111";
    private Log logger = LogFactory.getLog(BaseAcceptanceTest.class);

    private String host = "localhost";
    private int httpPort = 8090;
    private int httpsPort = 8091;
    // private String contextUrl = "/mlottery_te.test/transaction_engine/";
    private String contextUrl = "/mlottery_te/transaction_engine/";
    // private String certPath = "file:d:/apache-tomcat-5.5.27/conf/certs/";
    private String certPath = "file:E:/project/apache-tomcat-6.0.36/conf/certs/";

    private int traceIndex = 1;

    /**
     * Get working key from TE.
     * 
     * @param host
     *            The server host.
     * @param port
     *            The server post for get_working_key
     * @param contextUrl
     *            The cotnext url.
     * @return a WorkingKey associated to GPE.
     */
    protected Context prepare(Context request) throws Exception {
        return prepareWorkingKey(request);
    }

    private Context prepareWorkingKey(Context request) throws MalformedURLException, Exception {
        ProtocolSocketFactory psf = new AuthSSLProtocolSocketFactory(new URL(certPath + "igpe_192.168.2.173.jks"),
                "111111", new URL(certPath + "igpe_192.168.2.173_trust.jks"), "111111");
        // new URL(certPath + "igpe_igpe_trust.jks"),"111111");
        Protocol authhttps = new Protocol("https", psf, httpsPort);

        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(host, httpsPort, authhttps);
        // customize request context
        request.setTransType(TransactionType.GET_WORKING_KEY.getRequestType());
        request.setOperatorId(null);
        // request.setTerminalId(null);
        request.setTraceMessageId(null);
        // use relative url only
        PostMethod post = new PostMethod(contextUrl);
        this.assemblePostHeader(post, request);

        try {
            // GetMethod httpget = new GetMethod("/");
            client.executeMethod(post);
            String teCode = post.getResponseHeader(Context.HEADER_REPONSE_CODE).getValue();
            if (!teCode.equals("200")) {
                throw new IllegalStateException("Server encounter a exception, response code:" + teCode);
            }

            return this.assembleContextFromResponse(post, null);
        } catch (Exception e) {
            throw e;
        } finally {
            // Release current connection to the connection pool once you are
            // done
            post.releaseConnection();
        }
    }

    /**
     * Send a post request to TE. This work flow of methods:
     * <ul>
     * <li>MAC and DES encrypt the request context.</li>
     * <li>send post request to TE.</li>
     * <li>DES and MAC decrypt the response.</li>
     * </ul>
     * 
     * @param request
     *            The request context.
     * @return a response context.
     * @throws Exception
     */
    protected Context post(Context request) throws Exception {
        // get working key for today's transaction
        WorkingKey key = request.getWorkingKey();
        assert key != null : "context.getWorkingKey() can NOT be null";

        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(host, httpPort);
        PostMethod post = new PostMethod(contextUrl);
        this.assemblePostHeader(post, request);
        this.assemblePostRequest(post, request);
        if (request.getEncrptedBody() != null) {
            RequestEntity entity = new StringRequestEntity(request.getEncrptedBody(), "text/xml", "UTF-8");
            if (logger.isDebugEnabled()) {
                logger.debug("Start to send request(type=" + request.getTransType() + "):" + request.getOriginalBody());
                logger.debug("Start to send request(type=" + request.getTransType() + "):" + request.getEncrptedBody());
            }
            post.setRequestEntity(entity);
        }

        try {
            client.executeMethod(post);
            int status = Integer.parseInt(post.getResponseHeader(Context.HEADER_REPONSE_CODE).getValue());
            if (status == 200 || status == SystemException.CODE_FAILTO_CANCEL) {
                // assemble response Context
                return this.assembleContextFromResponse(post, key);
            } else {
                Context response = new Context();
                response.setResponseCode(status);
                return response;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            post.releaseConnection();
        }
    }

    protected Context doPost() throws Exception {
        Context req = this.mockRequestContext();
        int originType = req.getTransType();
        req.setWorkingKey(this.getDefaultWorkingKey());
        req.setTransType(originType);

        return this.post(req);
    }

    protected WorkingKey getDefaultWorkingKey() throws Exception {
        WorkingKey workingKey = new WorkingKey();
        workingKey.setGpeId(GPE_ID);
        workingKey.setDataKey("W0JAMWQ1MGZkMjc2N2U2M2Y2LWVkYTIt");
        workingKey.setMacKey("P2Bbo6+bSSR9O2Qc89f3b4oHTyE1V2gF");
        return workingKey;
        // Context request = this.mockRequestContext();
        // return (WorkingKey) this.prepare(request).getModel();
    }

    /**
     * Subclass can invoke this method to get a mock Context.
     */
    protected Context mockRequestContext() throws Exception {
        // initial spring context
        MLotteryContext prop = MLotteryContext.getInstance();
        prop.setBeanFactory(new ClassPathXmlApplicationContext(new String[] { "/spring/spring-core.xml",
                "/spring/spring-core-dao.xml", "/spring/game/spring-raffle.xml", "/spring/game/spring-ig.xml",
                "/spring/game/spring-extraball.xml", "/spring/game/spring-lotto.xml", "/spring/game/spring-toto.xml",
                "/spring/game/spring-lfn.xml", "/spring/spring-3rdparty.xml", "/spring/game/spring-magic100.xml",
                "/spring/game/spring-digital.xml", "/spring/game/spring-union.xml", "/spring/spring-amqp.xml",
                "/spring/game/spring-vat.xml", "/spring/game/spring-bingo.xml", "spring/vas/spring-airtime.xml" }));

        Context ctx = new Context();
        ctx.setProtocalVersion("1.0");
        ctx.setTimestamp(new Date());
        String traceMessageId = ctx.getStrTimestamp();
        traceMessageId = traceMessageId.substring(6) + (traceIndex++);
        ctx.setTraceMessageId(traceMessageId);
        Gpe gpe = new Gpe();
        gpe.setId(GPE_ID);
        ctx.setGpe(gpe);
        ctx.setTerminalId(111);
        ctx.setOperatorId("OPERATOR-111");
        ctx.setBatchNumber(SimpleToolkit.formatDate(new Date(), "yyyyMMdd"));
        customizeRequestContext(ctx);
        return ctx;
    }

    /**
     * Subclass should set transaction type and assemble model.
     */
    protected void customizeRequestContext(Context ctx) throws Exception {

    }

    /**
     * Assemble a Context from response message.
     */
    protected Context assembleContextFromResponse(PostMethod post, WorkingKey key) throws Exception {
        Context responseCtx = new Context();
        responseCtx.setProtocalVersion(post.getResponseHeader(Context.HEADER_PROTOCAL_VERSION).getValue());
        Gpe gpe = new Gpe();
        gpe.setId(post.getResponseHeader(Context.HEADER_GPE_ID).getValue());
        responseCtx.setGpe(gpe);
        responseCtx.setResponseCode(Integer.parseInt(post.getResponseHeader(Context.HEADER_REPONSE_CODE).getValue()));
        responseCtx.setStrTimestamp(post.getResponseHeader(Context.HEADER_TIMESTAMP).getValue());
        responseCtx.setTransType(Integer.parseInt(post.getResponseHeader(Context.HEADER_TRANSACTION_TYPE).getValue()));

        if (post.getResponseHeader(Context.HEADER_BATCHNUMBER) != null) {
            responseCtx.setBatchNumber(post.getResponseHeader(Context.HEADER_BATCHNUMBER).getValue());
        }
        if (post.getResponseHeader(Context.HEADER_MAC) != null) {
            responseCtx.setMac(post.getResponseHeader(Context.HEADER_MAC).getValue());
        }
        if (post.getResponseHeader(Context.HEADER_OPERATOR_ID) != null) {
            responseCtx.setOperatorId(post.getResponseHeader(Context.HEADER_OPERATOR_ID).getValue());
        }
        if (post.getResponseHeader(Context.HEADER_TRANSACTION_ID) != null) {
            responseCtx.setTransactionID(post.getResponseHeader(Context.HEADER_TRANSACTION_ID).getValue());
        }
        if (post.getResponseHeader(Context.HEADER_TRACE_MESSAGE_ID) != null) {
            responseCtx.setTraceMessageId(post.getResponseHeader(Context.HEADER_TRACE_MESSAGE_ID).getValue());
        }
        if (post.getResponseHeader(Context.HEADER_TERMINAL_ID) != null) {
            responseCtx.setStrTerminalId(post.getResponseHeader(Context.HEADER_TERMINAL_ID).getValue());
        }
        if (post.getResponseHeader(Context.HEADER_GAME_TYPE_ID) != null) {
            responseCtx.setGameTypeId(post.getResponseHeader(Context.HEADER_GAME_TYPE_ID).getValue());
        }

        // handle response body, trim() must be called, or '\r\n' will cause
        // fail to decryption.
        String body = post.getResponseBodyAsString().trim();
        // System.out.println("Encrypted response(hash=" + body.hashCode() +
        // ",length=" + body.length() + "):" + body);
        responseCtx.setEncrptedBody(body);
        responseCtx.setOriginalBody(body);
        int transType = responseCtx.getTransType();
        if (transType != TransactionType.GET_WORKING_KEY.getResponseType() && !responseCtx.getEncrptedBody().equals("")) {
            // do decryption
            String xml = TriperDESCipher
                    .decrypt(key.getDataKey(), body, MLotteryContext.getInstance().getTriperDesIV());
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

    protected void assemblePostHeader(PostMethod post, Context request) throws Exception {
        post.addRequestHeader(Context.HEADER_TRANSACTION_TYPE, request.getTransType() + "");
        post.addRequestHeader(Context.HEADER_PROTOCAL_VERSION, request.getProtocalVersion());
        post.addRequestHeader(Context.HEADER_GPE_ID, request.getGpe().getId());
        post.addRequestHeader(Context.HEADER_TIMESTAMP, request.getStrTimestamp());
        post.addRequestHeader(Context.HEADER_BATCHNUMBER, request.getBatchNumber());
        if (request.getTerminalId() != Context.UNINITIAL_VALUE) {
            post.addRequestHeader(Context.HEADER_TERMINAL_ID, request.getTerminalId() + "");
        }
        if (request.getStrTimestamp() != null) {
            post.addRequestHeader(Context.HEADER_TRACE_MESSAGE_ID, request.getTraceMessageId());
        }
        if (request.getOperatorId() != null) {
            post.addRequestHeader(Context.HEADER_OPERATOR_ID, request.getOperatorId());
        }
        if (request.getGameTypeId() != null) {
            post.addRequestHeader(Context.HEADER_GAME_TYPE_ID, request.getGameTypeId());
        }
    }

    protected void assemblePostRequest(PostMethod post, Context request) throws Exception {
        if (request.getTransType() != TransactionType.GET_WORKING_KEY.getRequestType()) {
            // do MAC
            if (request.getModel() != null) {
                String mappingFile = MLotteryContext.getInstance().getMappingFile(request.getTransType(),
                        request.getProtocalVersion(), request.getGameTypeId());
                String xml = CastorHelper.marshal(request.getModel(), mappingFile);
                request.setOriginalBody(xml);

                // do DES encryption
                String encryptedXml = TriperDESCipher.encrypt(request.getWorkingKey().getDataKey(),
                        request.getOriginalBody(), MLotteryContext.getInstance().getTriperDesIV());
                request.setEncrptedBody(encryptedXml);
            }
            String macString = request.getMacString().trim();
            String mac = HMacMd5Cipher.doDigest(macString, request.getWorkingKey().getMacKey());
            if (logger.isDebugEnabled()) {
                logger.debug("Do MAC(key=" + request.getWorkingKey().getMacKey() + ") on (" + macString + ",hash="
                        + macString.hashCode() + "), get digest:" + mac);
            }
            request.setMac(mac);
            post.addRequestHeader(Context.HEADER_MAC, mac);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(String contextUrl) {
        this.contextUrl = contextUrl;
    }

    protected <T> T getBean(Class<T> clazz, String beanName) {
        return (T) MLotteryContext.getInstance().getBeanFactory().getBean(beanName);
    }

}
