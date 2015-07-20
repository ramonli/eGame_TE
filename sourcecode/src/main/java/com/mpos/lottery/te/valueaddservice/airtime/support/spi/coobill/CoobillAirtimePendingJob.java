package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.airtime.service.AirtimeService;
import com.mpos.lottery.te.valueaddservice.airtime.support.AirtimeProvider;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Refer to http://quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-03
 */
@DisallowConcurrentExecution
public class CoobillAirtimePendingJob implements Job {
    private Log logger = LogFactory.getLog(CoobillAirtimePendingJob.class);
    private static final String BEAN_AIRTIME_SERVICE = "airtimeService";

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Start to execute job(" + jobContext.getJobDetail().getKey() + ").");
        }
        MLotteryContext teContext = MLotteryContext.getInstance();
        // call remote service to query the final result of previous pending transaction.
        BaseJpaDao baseJpaDao = (BaseJpaDao) teContext.getBeanFactory().getBean(
                teContext.get(MLotteryContext.ENTRY_BEAN_BASEDAO));
        String transactionId = jobContext.getJobDetail().getJobDataMap()
                .getString(AirtimeProvider.JOB_DATA_KEY_TRANSID);

        // resend the topup request, Coobill will return the status if the request has been handled before.
        QueryMsisdnReq coobillReq = new QueryMsisdnReq();
        coobillReq.setReqTransid(transactionId);
        coobillReq.setTelNo(baseJpaDao.findById(AirtimeTopup.class, transactionId).getMobileNo());
        OrderPaymentItem result = null;
        try {
            // Here we don't need to care about readTimeout, as if got timeout, the job will be executed next time.
            CoobillClient client = new CoobillClient(teContext.get("coobill.wsdl"), teContext.getInt(
                    "coobill.connection.timeout", 10) * 1000, teContext.getInt("coobill.read.timeout", 30) * 1000);
            result = client.queryMsisdn(coobillReq);
            if (logger.isDebugEnabled()) {
                logger.debug("Result of transaction(" + transactionId + "):"
                        + ToStringBuilder.reflectionToString(result));
            }
        } catch (Exception e) {
            // get exception when interact with Coobill, simply try next time.
            logger.warn(e.getMessage(), e);
        }
        if (result == null) {
            return;
        }

        try {
            if (CooBillAirtimeProvider.COOBILL_QUERY_STATUS_PENDING != result.getPayStatus()) {
                AirtimeTopup req = new AirtimeTopup();
                req.setStatus(CooBillAirtimeProvider.COOBILL_QUERY_STATUS_SUCCESS == result.getPayStatus()
                        ? AirtimeTopup.STATUS_SUCCESS
                        : AirtimeTopup.STATUS_FAIL);
                req.setTelcCommTransId(result.getTransactionId() + "");
                req.setMobileNo(coobillReq.getTelNo());
                // call generate service to maintain transaction status
                AirtimeService airtimeService = (AirtimeService) MLotteryContext.getInstance().getBeanFactory()
                        .getBean(BEAN_AIRTIME_SERVICE);
                airtimeService.jobTopup(jobContext,
                        jobContext.getJobDetail().getJobDataMap().getString(AirtimeProvider.JOB_DATA_KEY_TRANSID), req);
            } else {
                // if coobill response pending, the job should keep querying till get definite result.
                if (logger.isDebugEnabled()) {
                    logger.debug("The transaction is in pending, will try the job next time.");
                }
            }

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            JobExecutionException jobExe = new JobExecutionException(e);
            jobExe.setUnscheduleAllTriggers(true);
            if (logger.isDebugEnabled()) {
                logger.debug("Got exception when execution job, will unschedule all triggers of this job("
                        + jobContext.getJobDetail().getKey() + ").");
            }
            throw jobExe;
        }
    }

}
