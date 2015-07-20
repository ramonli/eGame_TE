package com.mpos.lottery.te.gameimpl.instantgame.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.port.Context;

public interface InstantOperatorService {

    /**
     * Operator can get the batch number for partial packet transaction ,tax amount.
     */
    InstantBatchReportDto getConfirmBatchNumber(Context ctx) throws ApplicationException;

}
