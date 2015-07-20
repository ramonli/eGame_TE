package com.mpos.lottery.te.merchant.service.commission;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

@Service("cashoutMobileCommissionBalanceService")
public class CashoutMobileCommissionBalanceService extends AbstractCommissionBalanceService{
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    @Override
    protected List<CommissionUnit> determineCommUnits(Transaction trans) throws ApplicationException {
        List<CommissionUnit> commUnits = new LinkedList<CommissionUnit>();

        Operator operator = this.getBaseJpaDao().findById(Operator.class, trans.getOperatorId());
        CommissionUnit commUnit = new CommissionUnit(null, operator.getCashoutRate(), trans.getTotalAmount(),
                BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY);
        commUnits.add(commUnit);
        return commUnits;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }
}
