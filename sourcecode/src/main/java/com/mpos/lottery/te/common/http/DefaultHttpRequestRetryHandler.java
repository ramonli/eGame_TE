package com.mpos.lottery.te.common.http;

import com.mpos.lottery.te.thirdpartyservice.HttpHeader;
import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Not all requests can be retried, it is up to the transaction type.
 */
public class DefaultHttpRequestRetryHandler implements HttpRequestRetryHandler {
    private Log logger = LogFactory.getLog(DefaultHttpRequestRetryHandler.class);
    private int maxRetryCount = 5;

    public DefaultHttpRequestRetryHandler() {
    }

    /**
     * Construct a <code>HttpRequestRetryHandler</code>.
     * 
     * @param maxRetryCount
     *            The max allowed retry count, default is 5.
     */
    public DefaultHttpRequestRetryHandler(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        boolean retry = false;
        HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        if (logger.isDebugEnabled()) {
            logger.debug("Check whether need to retry current request(transTYpe:"
                    + context.getAttribute(HttpHeader.TRANS_TYPE.getHeader()) + ")");
        }
        // Read Timeout, the parameter CoreConnectionPNames.SO_TIMEOUT will
        // result in this exception
        if (exception instanceof InterruptedIOException) {
            // retry only when Read Timeout
            if (context.getAttribute(HttpHeader.TRANS_TYPE.getHeader()) != null) {
                int transType = ((PaymentTransactionType) context.getAttribute(HttpHeader.TRANS_TYPE.getHeader()))
                        .getTransType();
                PaymentTransactionType paymentTransType = PaymentTransactionType.from(transType);
                if (paymentTransType.isNeedRetryIfTimeout()) {
                    if (executionCount > this.maxRetryCount) {
                        // Do not retry if over max retry count
                        logger.warn("Fail to retry request(transTYpe:"
                                + request.getFirstHeader(HttpHeader.TRANS_TYPE.getHeader())
                                + "), it has exceeded max allowed retry count:" + this.maxRetryCount);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Will retry request of transType(" + transType
                                    + "), and current retry counter:" + executionCount);
                        }
                        retry = true;
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug((retry ? "Will" : "No need to") + " retry current request.");
        }
        return retry;
    }

}
