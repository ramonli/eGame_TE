package com.mpos.lottery.te.merchant.service.balance;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

public interface BalanceStrategy {

    Integer supportedBalanceType();

    Operator balanceOperator(Context<?> respCtx, Transaction targetTrans, Operator operator, Merchant leafMerchant,
            boolean isTopup) throws ApplicationException;

    Merchant balanceMerchant(Context<?> respCtx, Transaction targetTrans, Operator operator, Merchant merchant,
            boolean isTopup) throws ApplicationException;
}
