package com.mpos.lottery.te.merchant.service.balance;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;

public class PayoutBalanceStrategy extends AbstractBalanceStrategy {

    @Override
    public Integer supportedBalanceType() {
        return BalanceService.BALANCE_TYPE_PAYOUT;
    }

    @Override
    public Operator balanceOperator(Context<?> respCtx, Transaction targetTrans, Operator operator,
            Merchant leafMerchant, boolean isTopup) throws ApplicationException {
        BigDecimal transAmount = targetTrans.getTotalAmount();
        if (isTopup) { // payout
            operator.setPayoutCreditLevel(operator.getPayoutCreditLevel().add(transAmount));
        } else { // payout cancellation...the payout balance can be negative
            operator.setPayoutCreditLevel(operator.getPayoutCreditLevel().subtract(transAmount));
        }
        return operator;
    }

    @Override
    public Merchant balanceMerchant(Context<?> respCtx, Transaction targetTrans, Operator operator,
            Merchant definitiveMerchant, boolean isTopup) throws ApplicationException {
        BigDecimal transAmount = targetTrans.getTotalAmount();
        if (isTopup) { // payout
            definitiveMerchant.setPayoutCreditLevel(definitiveMerchant.getPayoutCreditLevel().add(transAmount));
        } else { // payout cancellation
            definitiveMerchant.setPayoutCreditLevel(definitiveMerchant.getPayoutCreditLevel().subtract(transAmount));
        }
        return definitiveMerchant;
    }
}
