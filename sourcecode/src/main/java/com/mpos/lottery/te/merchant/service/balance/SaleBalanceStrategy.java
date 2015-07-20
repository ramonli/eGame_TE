package com.mpos.lottery.te.merchant.service.balance;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

public class SaleBalanceStrategy extends AbstractBalanceStrategy {
    private Log logger = LogFactory.getLog(SaleBalanceStrategy.class);
    public static final String PROP_SOLD_BY_CREDIT_CARD = SaleBalanceStrategy.class + ".PROP.SALE_BY_CREDITCARD";

    @Override
    public Integer supportedBalanceType() {
        return BalanceService.BALANCE_TYPE_SALE;
    }

    @Override
    public Operator balanceOperator(Context<?> respCtx, Transaction targetTrans, Operator operator,
            Merchant leafMerchant, boolean isTopup) throws ApplicationException {
        if (!isMaintainBalanceForCreditCardSale(respCtx, leafMerchant)) {
            // the commission calculation will depends on this operator
            return operator;
        }

        BigDecimal transAmount = targetTrans.getTotalAmount();
        if (!isTopup) { // sale
            BigDecimal tmpCreditLevel = operator.getSaleCreditLevel().subtract(transAmount);
            if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0) {
                throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT, "The sale balance ("
                        + operator.getSaleCreditLevel() + ") of " + operator + " isn't enough for sale(amount="
                        + transAmount + ").");
            }
            operator.setSaleCreditLevel(tmpCreditLevel);
        } else { // sale cancellation
            operator.setSaleCreditLevel(operator.getSaleCreditLevel().add(transAmount));
        }
        return operator;
    }

    @Override
    public Merchant balanceMerchant(Context<?> respCtx, Transaction targetTrans, Operator operator,
            Merchant definitiveMerchant, boolean isTopup) throws ApplicationException {
        if (!isMaintainBalanceForCreditCardSale(respCtx, definitiveMerchant)) {
            return definitiveMerchant;
        }

        BigDecimal transAmount = targetTrans.getTotalAmount();
        if (!isTopup) { // sale
            BigDecimal tmpCreditLevel = definitiveMerchant.getSaleCreditLevel().subtract(transAmount);
            if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0) {
                throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT, "The balance of sale ("
                        + definitiveMerchant.getSaleCreditLevel() + ") of " + definitiveMerchant
                        + " isn't enough for sale(amount=" + transAmount + ").");
            }
            definitiveMerchant.setSaleCreditLevel(tmpCreditLevel);
        } else { // sale cancellation
            definitiveMerchant.setSaleCreditLevel(definitiveMerchant.getSaleCreditLevel().add(transAmount));
        }
        return definitiveMerchant;
    }

    protected boolean isMaintainBalanceForCreditCardSale(Context<?> respCtx, Merchant leafMerchant) {
        boolean soldByCreditCard = false;
        Object value = respCtx.getProperty(PROP_SOLD_BY_CREDIT_CARD);
        if (value != null) {
            soldByCreditCard = ((Boolean) value).booleanValue();
        }

        if (soldByCreditCard && !leafMerchant.isDeductSaleByCreditCard()) {
            if (logger.isInfoEnabled()) {
                logger.info("The sale is paid by credit card, and merchant(" + leafMerchant
                        + ") has been configured to no need to decut/topup sale balance for credit card"
                        + " sale...will ignore balance calculation");
            }
            return false;
        }
        return true;
    }
}
