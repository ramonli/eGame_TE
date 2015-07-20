package com.mpos.lottery.te.merchant.service.balance;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;

public class CommissionBalanceStrategy extends AbstractBalanceStrategy {

    @Override
    public Integer supportedBalanceType() {
        return BalanceService.BALANCE_TYPE_COMMISSION;
    }

    @Override
    public Operator balanceOperator(Context<?> respCtx, Transaction targetTrans, Operator operator,
            Merchant leafMerchant, boolean isTopup) throws ApplicationException {
        BigDecimal transAmount = targetTrans.getTotalAmount();
        if (isTopup) { // operator cashout
            operator.setCommisionBalance(operator.getCommisionBalance().add(transAmount));
        } else { // operator cashout cancellation.
            operator.setCommisionBalance(operator.getCommisionBalance().subtract(transAmount));
        }
        return operator;
    }

    @Override
    public Merchant balanceMerchant(Context<?> respCtx, Transaction targetTrans, Operator operator,
            Merchant definitiveMerchant, boolean isTopup) throws ApplicationException {
        BigDecimal transAmount = targetTrans.getTotalAmount();
        if (isTopup) { // operator cashout
            definitiveMerchant.setCommisionBalance(definitiveMerchant.getCommisionBalance().add(transAmount));
        } else { // cancellation of operator cashout
            definitiveMerchant.setCommisionBalance(definitiveMerchant.getCommisionBalance().subtract(transAmount));
        }
        return definitiveMerchant;
    }
}
