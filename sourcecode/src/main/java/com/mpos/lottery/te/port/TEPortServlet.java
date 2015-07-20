package com.mpos.lottery.te.port;

import com.mpos.lottery.te.common.Command;
import com.mpos.lottery.te.common.KeyValuePair;
import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.jmx.TransactionTimeout;
import com.mpos.lottery.te.common.jmx.TransactionTimeoutMBean;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.ExceptionHandler;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.protocol.ProtocolSerializer;
import com.mpos.lottery.te.port.service.FacadeService;
import com.mpos.lottery.te.sequence.domain.TicketSerialSpec;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;
import com.mpos.lottery.te.workingkey.domain.WorkingKeyCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is only port exposed by TE. All requests will received by <code>TEPortServlet</code>, and \ then the servlet
 * will dispatch request to <code>DispatchController</code>.
 */
public class TEPortServlet extends HttpServlet {
    private static final long serialVersionUID = 9086524025083659379L;

    private static Log logger = LogFactory.getLog(TEPortServlet.class);

    // private Log accessLog = LogFactory.getLog("log.access");

    /**
     * Initialize PropertiesLoader and BeanFactory.
     */
    public void init(ServletConfig config) throws ServletException {
        MLotteryContext prop = MLotteryContext.getInstance();
        ApplicationContext beanFactory = WebApplicationContextUtils
                .getWebApplicationContext(config.getServletContext());
        prop.setBeanFactory(beanFactory);

        try {
            // startup quartz scheduler
            Scheduler scheduler = (Scheduler) beanFactory.getBean(prop.get(MLotteryContext.ENTRY_BEAN_JOB_SCHEDULER));
            scheduler.start();
            if (logger.isDebugEnabled()) {
                logger.debug("Startup job scheduer(" + scheduler + ") successfully.");
            }
        } catch (SchedulerException e) {
            throw new ServletException(e);
        }

        this.printCopyright(config.getServletContext().getRealPath("/"));
        this.registerMBeans();
    }

    /**
     * The port of TE, only POST method is supported. The work flow is: 1) parse request to get a request Context
     * instance(depends on transaction type). 2) dispatch request context to DispatchController. 3) convert response
     * context into xml string (depends on transaction type). 4) return response xml string to client.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        StopWatch watch = new CommonsLogStopWatch();
        Context requestCtx = new Context();
        Context responseCtx = new Context();

        KeyValuePair<String, ReentrantLock> lockMap = null;
        try {
            // parse protocol
            this.parseHeader(requestCtx, request);

            this.assembleWorkingKey(requestCtx, watch);
            watch.start();
            requestCtx.setEncrptedBody(this.getRequestBody(request, requestCtx.getTransType()));
            watch.stop("getRequestBody");
            watch.start();
            new ProtocolSerializer().deserialize(requestCtx);
            watch.stop("deserializeRequest");
            // write access log
            // accessLog.info(requestCtx.getMacString());

            // dispatch request to service
            MLotteryContext prop = MLotteryContext.getInstance();
            FacadeService dispatcher = (FacadeService) prop.getBeanFactory().getBean(
                    prop.get(MLotteryContext.ENTRY_BEAN_SERVICEFACADE));
            // lockMap = this.acquireTransactionLock(requestCtx);

            watch.start();

            dispatcher.facade(requestCtx, responseCtx);
            watch.stop("Transaction{" + TransactionType.getTransactionType(requestCtx.getTransType()) + "."
                    + responseCtx.getGameTypeId() + "}");
            // execute all commands
            List<Command> commandList = responseCtx.getCommandList();
            for (Command cmd : commandList) {
                cmd.exec();
            }

            // apply MBeans...shit, why the name of attribute is 'Timeout', not
            // 'timeout'?
            long timeout = (Long) ManagementFactory.getPlatformMBeanServer().getAttribute(
                    new ObjectName(TransactionTimeoutMBean.objectName), "Timeout");
            if (timeout > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("TE will wait " + timeout + " seconds.");
                }
                Thread.currentThread().sleep(timeout * 1000);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), ExceptionHandler.getCause(e));
            // assemble response context from requestContext
            ExceptionHandler exHandler = new ExceptionHandler(e);
            exHandler.handle(requestCtx, responseCtx);
            responseCtx.setResponseCode(exHandler.getErrorCode());
        } finally {
            // release transaction lock
            // Java5TransactionLocker.getInstance().release(lockMap);
        }

        PrintWriter pw = response.getWriter();
        try {
            // write response to client
            // serialize response
            watch.start();
            new ProtocolSerializer().serialize(responseCtx);
            watch.stop("serializeResponse");
            this.assembleResponseHeader(responseCtx, response);

            if (responseCtx.getEncrptedBody() != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Write response to client(host1=" + request.getRemoteHost() + ",sizeOfPack="
                            + responseCtx.getEncrptedBody().length() + ") succesfully.");
                }
                pw.println(responseCtx.getEncrptedBody());
                // write access log, if response is returned successfully
                // accessLog.info(responseCtx.getMacString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setHeader(Context.HEADER_REPONSE_CODE, SystemException.CODE_INTERNAL_SERVER_ERROR + "");
            response.sendError(HttpURLConnection.HTTP_OK);
        } finally {
            pw.close();
        }
    }

    // ---------------------------------------
    // PRIVATE METHODS
    // ---------------------------------------

    /**
     * Determine the game type of current request. This method will try to determine game type by following order.
     * <ol>
     * <li>If there is a header 'X-Game-Type-Id' and non -1 has been set, then set game type as the value of
     * 'X-Game-Type-Id', else go to next step.</li>
     * <li>Try to determine game type by model of request.
     * <ul>
     * <li>If the mode of request is instance of <code>BaseTicket</code>, and the serialNo field is set, try to retrieve
     * game type from serialNo.</li>
     * <li>If the mode of request is instance of <code>Transaction</code>, and the fields 'devId' and 'traceMsgId' are
     * set, try to retrieve game type from transaction.</li>
     * </ul>
     * </li>
     * <li>If all above steps fail, set game type to -1.</li>
     * </ol>
     * 
     * @param reqCtx
     *            The context of request.
     * @return the final game type.
     */
    protected int determineGameType(Context reqCtx) throws ApplicationException {
        int gameType = Game.TYPE_UNDEF;
        // NO 'X-Game-Type-Id' set in headers
        if (Game.TYPE_UNDEF == reqCtx.getGameTypeIdIntValue()) {
            Object model = reqCtx.getModel();
            // try to find game type in serial number first
            if (model instanceof BaseTicket) {
                BaseTicket ticket = (BaseTicket) model;
                if (ticket.getRawSerialNo() != null) {
                    TicketSerialSpec spec = new TicketSerialSpec(ticket.getRawSerialNo());
                    gameType = Integer.parseInt(spec.getGameTypeOrSecond());
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Determine the game type as " + gameType);
        }
        if (Game.TYPE_UNDEF != gameType) {
            reqCtx.setGameTypeId(gameType + "");
        }
        return gameType;
    }

    /**
     * Retrieve a header from request. Some headers are required for each request, then this method will check if these
     * headers are present in request. Some headers are required for some specific transaction, then the transaction
     * service will check if the header (in server, it is a context field) is present in request.
     * 
     * @param header
     *            The name of request header.
     * @param request
     *            The HTTP request.
     * @param isRequired
     *            If this header is required for each request.
     * @return the value of request header.
     */
    protected String getHeader(String header, HttpServletRequest request, boolean isRequired) {
        String value = request.getHeader(header);
        if (logger.isDebugEnabled()) {
            logger.debug("Got request header(name=" + header + ", value=" + value + ").");
        }

        if (value != null) {
            value = value.trim();
        }
        if ("".equals(value)) {
            value = null;
        }
        if (value == null && isRequired) {
            throw new MessageFormatException(SystemException.CODE_REQUIRED_HEADER_MISS, "Required header '" + header
                    + "' is missed.");
        }

        return value;
    }

    /**
     * Get http message body, except Get_Working_Key, Reversal, TransactionEnquiry.
     */
    protected String getRequestBody(HttpServletRequest request, int transType) throws IOException {
        TransactionType transacionType = TransactionType.getTransactionType(transType);
        if (!transacionType.isRequireBody()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore the message body for transaction(type=" + transacionType + ").");
            }
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuffer buffer = new StringBuffer();
        for (String tmp = br.readLine(); tmp != null; tmp = br.readLine()) {
            buffer.append(tmp);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got request from " + request.getRemoteHost() + ", and the message body is:"
                    + buffer.toString() + ".");
        }
        return buffer.toString().trim();
    }

    /**
     * Get the working key for the GPE with specified identifier.
     */
    protected void assembleWorkingKey(Context requestCtx, StopWatch watch) {
        if (requestCtx.getTransType() == TransactionType.GET_WORKING_KEY.getRequestType()) {
            return;
        }
        // String beanName = prop.get(MLotteryContext.ENTRY_BEAN_WORKINGKEYDAO);
        String gpeId = requestCtx.getGpe().getId();
        // WorkingKeyDao dao = (WorkingKeyDao)
        // prop.getBeanFactory().getBean(beanName);
        // WorkingKey key = dao.getWorkingKey(WorkingKey.getCurrentDateStr(),
        // gpeId);
        watch.start();
        WorkingKeyCache workingKeyCache = WorkingKeyCache.getInstance();
        WorkingKey key = workingKeyCache.getDailyWorkingKey(gpeId, WorkingKey.getCurrentDateStr());
        watch.stop("assembleWorkingKey");
        if (key == null) {
            throw new SystemException(SystemException.CODE_NOLEGAL_WORKINGKEY, "can NOT found working key with(gpeId="
                    + gpeId + ",createDateStr=" + new Date() + ").");
        }

        MLotteryContext prop = MLotteryContext.getInstance();
        // assemble GPE too
        BaseJpaDao baseDao = (BaseJpaDao) prop.getBeanFactory().getBean(prop.get(MLotteryContext.ENTRY_BEAN_BASEDAO));
        Gpe gpe = baseDao.findById(Gpe.class, requestCtx.getGpe().getId());
        if (gpe == null) {
            throw new SystemException("No Gpe found by id:" + requestCtx.getGpe().getId());
        }
        requestCtx.setGpe(gpe);

        requestCtx.setWorkingKey(key);
    }

    /**
     * Assemble reponse headers defined by 'TE Transaction Interface Definition'. All headers will be copyed from
     * response Context.
     */
    private void assembleResponseHeader(Context responseCtx, HttpServletResponse response) {
        if (responseCtx.getProtocalVersion() != null) {
            response.setHeader(Context.HEADER_PROTOCAL_VERSION, responseCtx.getProtocalVersion());
        }
        if (responseCtx.getGpe() != null && responseCtx.getGpe().getId() != null) {
            response.setHeader(Context.HEADER_GPE_ID, responseCtx.getGpe().getId());
        }
        if (responseCtx.getMac() != null) {
            response.setHeader(Context.HEADER_MAC, responseCtx.getMac());
        }
        if (responseCtx.getOperatorId() != null) {
            response.setHeader(Context.HEADER_OPERATOR_ID, responseCtx.getOperatorId());
        }
        response.setHeader(Context.HEADER_REPONSE_CODE, responseCtx.getResponseCode() + "");
        if (responseCtx.getTerminalId() != Context.UNINITIAL_VALUE) {
            response.setHeader(Context.HEADER_TERMINAL_ID, responseCtx.getTerminalId() + "");
        }
        if (responseCtx.getStrTimestamp() != null) {
            response.setHeader(Context.HEADER_TIMESTAMP, responseCtx.getStrTimestamp());
        }
        if (responseCtx.getStrTransType() != null) {
            response.setHeader(Context.HEADER_TRANSACTION_TYPE, responseCtx.getStrTransType());
        }
        if (responseCtx.getTraceMessageId() != null) {
            response.setHeader(Context.HEADER_TRACE_MESSAGE_ID, responseCtx.getTraceMessageId());
        }
        if (responseCtx.getTransactionID() != null) {
            response.setHeader(Context.HEADER_TRANSACTION_ID, responseCtx.getTransactionID());
        }
        if (responseCtx.getBatchNumber() != null) {
            response.setHeader(Context.HEADER_BATCHNUMBER, responseCtx.getBatchNumber());
        }
        if (responseCtx.getGameTypeId() != null) {
            response.setHeader(Context.HEADER_GAME_TYPE_ID, responseCtx.getGameTypeId());
        }
    }

    /**
     * Extract header from HTTP request.
     */
    private void parseHeader(Context requestContext, HttpServletRequest request) {
        // transaction type
        requestContext.setStrTransType(getHeader(Context.HEADER_TRANSACTION_TYPE, request, true));
        // protocal version
        requestContext.setProtocalVersion(this.getHeader(Context.HEADER_PROTOCAL_VERSION, request, true));
        // timestamp
        requestContext.setStrTimestamp(this.getHeader(Context.HEADER_TIMESTAMP, request, true));
        // gpe id
        String gpeId = getHeader(Context.HEADER_GPE_ID, request, true);
        Gpe gpe = new Gpe();
        gpe.setId(gpeId);
        requestContext.setGpe(gpe);

        // above headers are mandatory for all requests.
        TransactionType transType = TransactionType.getTransactionType(requestContext.getTransType());
        // trace message id
        requestContext.setTraceMessageId(getHeader(Context.HEADER_TRACE_MESSAGE_ID, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_TRACE_MESSAGE_ID)));
        // operator id
        requestContext.setOperatorId(getHeader(Context.HEADER_OPERATOR_ID, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_OPERATOR_ID)));
        // terminal id
        requestContext.setStrTerminalId(getHeader(Context.HEADER_TERMINAL_ID, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_TERMINAL_ID)));
        // MAC
        requestContext.setMac(getHeader(Context.HEADER_MAC, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_MAC)));
        // game type
        requestContext.setGameTypeId(getHeader(Context.HEADER_GAME_TYPE_ID, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_GAME_TYPE_ID)));
        // batch number
        requestContext.setBatchNumber(getHeader(Context.HEADER_BATCHNUMBER, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_BATCHNUMBER)));
        requestContext.setGpsLocation(getHeader(RequestHeaders.HEADER_GPS_LOCATION, request,
                transType.isRequiredHeader(RequestHeaders.HEADER_GPS_LOCATION)));
    }

    protected void printCopyright(String contextPath) {
        try {
            String pathSeperator = System.getProperty("file.separator");
            String metafile = contextPath + pathSeperator + "META-INF" + pathSeperator + "MANIFEST.MF";
            File metaFile = new File(metafile);
            if (!metaFile.exists()) {
                // just ignore this
                logger.warn("can NOT find META file:" + metafile);
                return;
            }

            String lineOperator = System.getProperty("line.separator");
            String blockOperator = "---------------------------------------";
            StringBuffer buffer = new StringBuffer(lineOperator);
            buffer.append(blockOperator).append(lineOperator);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(metafile)));
            for (String line = br.readLine(); line != null;) {
                buffer.append(line).append(lineOperator);
                line = br.readLine();
            }
            buffer.append(lineOperator).append(blockOperator);

            logger.info(buffer.toString());
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    protected void registerMBeans() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(TransactionTimeoutMBean.objectName);
            TransactionTimeoutMBean mbean = new TransactionTimeout();
            if (!mbs.isRegistered(objectName)) {
                mbs.registerMBean(mbean, objectName);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Register MBean(" + mbean + ") with name(" + objectName + ") successfully.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
