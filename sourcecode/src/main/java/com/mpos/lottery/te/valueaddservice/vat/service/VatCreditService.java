package com.mpos.lottery.te.valueaddservice.vat.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.web.VatTransferDto;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;

public interface VatCreditService {

    /**
     * There are 2 kinds of credit: sale and payout. Sale and its corresponding cancellation will affect sale credit,
     * payout/validation and its corresponding cancellation will affect payout credit.
     * <p>
     * Refer to {@link CreditService#credit(String, long, BigDecimal, String, boolean, boolean, boolean)} for more
     * information.
     */
    VatTransferDto vatTransferCredit(Context respCtx, VatTransferDto dto) throws ApplicationException;

}
