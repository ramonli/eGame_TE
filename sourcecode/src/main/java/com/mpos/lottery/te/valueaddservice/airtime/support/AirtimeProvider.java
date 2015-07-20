package com.mpos.lottery.te.valueaddservice.airtime.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;

public interface AirtimeProvider {
    public static final String JOB_DATA_KEY_TRANSID = "transactionId";

    /**
     * Check whether this provider supports given provider id. For each service provider there should be a dedicated
     * implementation.
     * 
     * @return the id of supported airtime provider.
     */
    int supportProvider();

    /**
     * This interface will call a 3rd party service for topup function.
     * 
     * @param respCtx
     *            THe context of current transaction.
     * @param topupReq
     *            THe topup request.
     * @return the topup response message.
     * @throws ApplicationException
     *             if encounter any business exception.
     */

    AirtimeTopup topup(Context respCtx, AirtimeTopup topupReq) throws ApplicationException;
}
