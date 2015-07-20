package com.mpos.lottery.te.merchant.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.web.IncomeBalanceDto;
import com.mpos.lottery.te.port.Context;

/**
 * Interfaces for managing Income Balance Transfer.
 * 
 * @author terry
 * 
 */
public interface IncomeBalanceService {

    /**
     * If a operator does not have enough sale balance, he/she will not be able to sell tickets, but at that time, i
     * he/she has enough income balance including payout balance, cash out balance and commission balance), so it’s will
     * not be rational to this operator because income balance belong to this operator and he/she only has no time to
     * buy vouchers to add his/her sale balance and he/she has enough money but he/she cannot sell tickets. So we need
     * to add a transaction to transfer an operator’s income balance to his sale balance, that’s Income Balance
     * Transaction needs to do.
     * 
     * @param respCtx
     *            The context of current topup transaction.
     * @param incomeBalanceDto
     *            The topup request, the field 'amount' must be provided. If no field 'operatorId' provided, system will
     *            retrieve operator from 'X-Operator-Id'.
     * @return The balance of the operator or merchant whose sale balance has been topuped.
     * @throws ApplicationException
     *             when encounter biz exception.
     */
    IncomeBalanceDto incomeBalanceTransfer(Context respCtx, IncomeBalanceDto incomeBalanceDto)
            throws ApplicationException;
}
