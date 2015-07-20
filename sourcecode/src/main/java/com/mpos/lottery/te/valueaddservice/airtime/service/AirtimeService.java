package com.mpos.lottery.te.valueaddservice.airtime.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;

import org.quartz.JobExecutionContext;

public interface AirtimeService {

    /**
     * Topup the balance of mobile phone by airtime. There will be multiple different airtime provider, this service
     * will route the request to appropriate airtime provider.
     * <p/>
     * When call remote airtime interface, this service(the client of remote service) may get read timeout, in this
     * case, this service will create a persisted quartz job which will keep calling airtime interface(transaction
     * enquiry) to get a definite result. Once got result, this job will update locak airtime topup records, and update
     * response code of transaction accordingly(200 for success, 502 for failure, 501 for pending).
     * <p/>
     * <b>JDBC Transaction</b> In generate TE will rollback transaction if <code>ApplicationException</code> or
     * <code>RuntimeException</code> thrown out, however there is a special case that, when got read timeout or 3rd
     * parth service, TE has to commit the transaction for quartz job to update later.
     * <p/>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>THe amount must meed the game parameters.</li>
     * <li>Operator got enough sale balance</li>
     * </ul>
     * <p/>
     * <b>Post-Condition</b>
     * <p/>
     * If got success response from airtime service provider,
     * <ul>
     * <li>Generate airtime topup transaction if successful or pending.</li>
     * <li>Generate balance transaction records following the sale style.</li>
     * <li>Deduct the sale balance.</li>
     * <li>Publish AMQP message of airtime transaction.</li>
     * </ul>
     * <p/>
     * <b>Usage Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by authorized GPEs.</li>
     * <li>Concurrent Access - To a single device, this interface must be called sequentially. To multiple devices,
     * there are no limitation on the number of concurrent accesses.</li>
     * </ul>
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param topupReq
     *            The request DTO of client, the following components must be set: mobileNo, amount, game.Id
     * @throws ApplicationException
     *             if encounter any business exception.
     */
    AirtimeTopup topup(Context respCtx, AirtimeTopup topupReq) throws ApplicationException;

    /**
     * The job service for updating previous pending airtime transaction. The service will be called by quartz job which
     * will be created if {@link #topup(Context, AirtimeTopup)} response 501(remote service timeout).
     * <p/>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>The transaction must be pending status.</li>
     * </ul>
     * <b>Post-Condition</b>
     * <ul>
     * <li>Update transaction response code to 200(airtime service provider response success) or 502(airtime service
     * provider response failure).</li>
     * <li>Update the status of airtime topup transaction response code to success(airtime service provider response
     * success) or failure(airtime service provider response failure).</li> *
     * <li>And if airtime service provider response success:
     * <ul>
     * <li>Deduct sale balance.</li>
     * <li>Generate balance transaction records</li>
     * <li>Publish AMQP message of airtime transaction.</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @param jobContext
     *            the context of quartz job.
     * @param transactionId
     *            The id of pending transaction.
     * @param result
     *            The result responded by remote service.
     * @throws ApplicationException
     *             if encounter any business related exception.
     */
    void jobTopup(JobExecutionContext jobContext, String transactionId, AirtimeTopup result)
            throws ApplicationException;
}
