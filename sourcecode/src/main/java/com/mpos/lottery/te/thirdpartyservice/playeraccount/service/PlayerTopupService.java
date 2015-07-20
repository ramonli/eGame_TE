package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerTopupDto;

public interface PlayerTopupService {

    /**
     * This service will be called once player has successfully topup account on player account system, and the
     * client(GPE) will sync this message to TE by calling this service.
     * <p/>
     * This service will,
     * <ol>
     * <li>generate transaction.</li>
     * <li>maintain operator's topup balance.</li>
     * <li>calculate commission.</li>
     * </ol>
     */
    void topup(Context respCtx, PlayerTopupDto dto) throws ApplicationException;
}
