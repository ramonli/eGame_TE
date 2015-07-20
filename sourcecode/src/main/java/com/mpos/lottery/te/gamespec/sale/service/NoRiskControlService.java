package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.util.List;

public class NoRiskControlService implements RiskControlService {

    @Override
    public void riskControl(Context respCtx, BaseTicket ticket, List<? extends BaseGameInstance> gameInstances)
            throws ApplicationException {
        // do nothing, that is disable risk control
    }

    @Override
    public void cancelRiskControl(Context respCtx, List<? extends BaseTicket> hostTickets) throws ApplicationException {
        // do nothing, that is disable risk control
    }

}
