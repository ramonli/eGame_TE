package com.mpos.lottery.te.port.web.filter;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpsFilter implements Filter {
    private Log logger = LogFactory.getLog(HttpsFilter.class);
    private static final String PROTOCOL_HTTP = "http";
    private static final String PROTOCOL_HTTPS = "https";

    // private MLotteryContext prop = MLotteryContext.getInstance();

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            // int port = req.getLocalPort();
            String schema = req.getScheme().trim();
            int transType = this.getTransactionType(req);
            if (logger.isDebugEnabled()) {
                logger.debug("Got a new request(host=" + request.getRemoteHost() + ", protocol=" + req.getProtocol()
                        + ",transactionType=" + transType + ",uri=" + req.getRequestURI() + ",localPort="
                        + req.getLocalPort() + ",schema=" + req.getScheme() + ").");
            }
            if (transType == TransactionType.GET_WORKING_KEY.getRequestType()) {
                // get working key should be protected by https
                if (!schema.equalsIgnoreCase(PROTOCOL_HTTPS)) {
                    throw new SystemException("Transaction 'GET_WORKING_KEY' is only available for https.");
                }
            } else {
                if (!schema.equalsIgnoreCase(PROTOCOL_HTTP)) {
                    throw new SystemException("Transactions, except 'GET_WORKING_KEY', are only available for http.");
                }
            }

            // pass controll to next filter.
            chain.doFilter(request, response);
        } catch (SystemException e) {
            logger.error(e.getMessage(), e);
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader(Context.HEADER_REPONSE_CODE, SystemException.CODE_INTERNAL_SERVER_ERROR + "");
            res.sendError(HttpURLConnection.HTTP_FORBIDDEN);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader(Context.HEADER_REPONSE_CODE, SystemException.CODE_INTERNAL_SERVER_ERROR + "");
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

    }

    public void init(FilterConfig arg0) throws ServletException {
        // do nothing
    }

    private int getTransactionType(HttpServletRequest req) {
        String strTransType = req.getHeader(Context.HEADER_TRANSACTION_TYPE);
        if (strTransType == null) {
            throw new IllegalArgumentException("can NOT find " + Context.HEADER_TRANSACTION_TYPE + " from headers.");
        }
        return Integer.parseInt(strTransType);
    }
}
