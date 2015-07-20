package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.airtime.support.AbstractAirtimeProvider;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

import javax.annotation.Resource;

@Component("cooBillAirtimeProvider")
public class CooBillAirtimeProvider extends AbstractAirtimeProvider {
    private Log logger = LogFactory.getLog(CooBillAirtimeProvider.class);
    private static final int COOBILL_PROVIDER_ID = 1;
    public static final int COOBILL_TOPOUP_STATUS_OK = 0;
    // all other value of COOBILL_QUERY_STATUS is failure
    public static final int COOBILL_QUERY_STATUS_PENDING = -1;
    public static final int COOBILL_QUERY_STATUS_SUCCESS = 0;
    @Resource(name = "teJobScheduler")
    private Scheduler scheduler;

    @Override
    public int supportProvider() {
        return COOBILL_PROVIDER_ID;
    }

    /**
     * Call CooBill topup serivce. If get read timeout, a quartz job will be created, which will keep calling
     * 'transaction enquiry' interface of CooBill till get a definite result.
     */
    @Override
    public AirtimeTopup topup(Context respCtx, AirtimeTopup topupReq) throws ApplicationException {
        // each provider may request a different refNo algorithm.
        topupReq.setSerialNo(this.generateRefNo());
        MLotteryContext context = MLotteryContext.getInstance();
        try {
            TopupMsisdnReq coobillReq = new TopupMsisdnReq();
            coobillReq.setAgent(context.get("coobill.agent"));
            coobillReq.setPassword(context.get("coobill.password"));
            coobillReq.setRefTrx(topupReq.getSerialNo());
            // the unit of topup amount of Coobill must be cent
            coobillReq.setAmout(topupReq.getAmount().multiply(new BigDecimal("100.0")).intValue());
            coobillReq.setMsisdn(topupReq.getMobileNo());

            // access remote service
            CoobillClient client = new CoobillClient(context.get("coobill.wsdl"), context.getInt(
                    "coobill.connection.timeout", 10) * 1000, context.getInt("coobill.read.timeout", 30) * 1000);
            if (logger.isDebugEnabled()) {
                logger.debug("Request of transaction(" + coobillReq.getRefTrx() + "):"
                        + ToStringBuilder.reflectionToString(coobillReq));
            }
            TopupMsisdnResult result = client.topupMsisdn(coobillReq);
            if (logger.isDebugEnabled()) {
                logger.debug("Result of transaction(" + coobillReq.getRefTrx() + "):"
                        + ToStringBuilder.reflectionToString(result));
            }

            // assemble response
            topupReq.setStatus(COOBILL_TOPOUP_STATUS_OK == result.getStatus()
                    ? AirtimeTopup.STATUS_SUCCESS
                    : AirtimeTopup.STATUS_FAIL);
            topupReq.setRespMessageOfRemoteService(result.getDescription());
            topupReq.setTelcCommTransId(result.getTransactionId());
            if (result.getStatus() != COOBILL_TOPOUP_STATUS_OK) {
                logger.warn("Failed result of transaction(" + coobillReq.getRefTrx() + ") from Coobill:"
                        + result.getDescription());
            }
        } catch (SocketTimeoutException e) {
            // read timeoout
            topupReq.setStatus(AirtimeTopup.STATUS_PENDING);
            // create a quartz job for timeout response.
            this.createPendingJob(respCtx, topupReq);
        } catch (Exception e) {
            throw new SystemException(SystemException.CODE_REMOTE_SERVICE_FAILUER, e.getMessage(), e);
        }
        return topupReq;
    }

    /**
     * Create a quartz job with name 'airtime_job_${TRANS_ID}' and job data,
     * <ul>
     * <li>key(transactionId):value(${TRANS_ID})
     * </ul>
     * and a trigger with name 'airtime_trigger_${TRANS_ID}'. Both job and trigger will be in group 'airtime'.
     */
    private void createPendingJob(Context respCtx, AirtimeTopup topupReq) {
        String transactionId = respCtx.getTransaction().getId();
        // define the job and tie it to our HelloJob class
        JobDetail job = newJob(CoobillAirtimePendingJob.class)
                .withIdentity(JOB_GROUP + "_job_" + transactionId, JOB_GROUP)
                .usingJobData(JOB_DATA_KEY_TRANSID, transactionId).build();

        // Trigger the job to run now, and then repeat every 40 seconds
        Trigger trigger = newTrigger()
                .withIdentity(JOB_GROUP + "_trigger_" + transactionId, JOB_GROUP)
                .startNow()
                .withSchedule(
                        simpleSchedule().withIntervalInSeconds(
                                MLotteryContext.getInstance().getInt("airtime.job.interval", 30)).repeatForever())
                .build();
        try {
            this.getScheduler().scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Read timeout of transaction(" + respCtx.getTransaction().getId() + "), has scheduled a job("
                    + job.getJobClass() + ":" + job.getKey() + ") for this transaction.");
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
