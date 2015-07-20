package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutRequest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutResponse;

public interface CashoutService {

    /**
     * Player can ask to cash out prize amount from his/her account.
     */
    CashoutResponse cashout(Context<?> responseCtx, CashoutRequest request) throws ApplicationException;
}
