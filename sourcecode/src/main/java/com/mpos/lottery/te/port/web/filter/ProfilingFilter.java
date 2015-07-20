package com.mpos.lottery.te.port.web.filter;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.zabbix.ZabbixSender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A simple filter to profile web application. Write data as below:
 * <p/>
 * [transaction id],[transaction type],[response code],[elapse time :second]
 */
public class ProfilingFilter implements Filter {
    private Log logger = LogFactory.getLog(ProfilingFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        StopWatch stopWatch = new CommonsLogStopWatch();
        // delegate request
        fc.doFilter(req, resp);

        String strTransType = ((HttpServletRequest) req).getHeader(Context.HEADER_TRANSACTION_TYPE);
        if (strTransType != null) {
            try {
                // write profiling data
                stopWatch.stop(this.getStopWatchTag(strTransType),
                        ((HttpServletRequest) req).getHeader(Context.HEADER_TRACE_MESSAGE_ID));
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }

            this.monitorLongTrans(req, resp, stopWatch);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // do nothing
    }

    protected String getStopWatchTag(String strTransType) {
        int transType = Integer.parseInt(strTransType.trim());
        StringBuffer tag = new StringBuffer("Request{");
        tag.append(TransactionType.getTransactionType(transType));
        tag.append("}");
        return tag.toString();
    }

    private void monitorLongTrans(ServletRequest req, ServletResponse resp, StopWatch stopWatch) {
        String traceMsgId = ((HttpServletRequest) req).getHeader(Context.HEADER_TRACE_MESSAGE_ID);
        String devId = ((HttpServletRequest) req).getHeader(Context.HEADER_TERMINAL_ID);
        MLotteryContext prop = MLotteryContext.getInstance();
        if (prop.getInt("zabbix.item.name.long_transaction.threshold") < stopWatch.getElapsedTime() / 1000) {
            ZabbixSender sender = new ZabbixSender(prop.get("zabbix.server.host"), prop.getInt("zabbix.server.port"));
            sender.asyncSend(prop.get("zabbix.host.name"), prop.get("zabbix.item.name.long_transaction"),
                    prop.get("zabbix.module.name"), " Transaction(devId=" + devId + ",traceMsgId=" + traceMsgId
                            + ") elapsed " + stopWatch.getElapsedTime() / 1000 + " seconds.");
        }
    }
}
