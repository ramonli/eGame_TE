package com.mpos.lottery.te.merchant.service.commission;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.dao.OperatorCommissionDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.OperatorCommission;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

@Service("saleCommissionBalanceService")
public class SaleCommissionBalanceService extends AbstractCommissionBalanceService {
    @Resource(name = "operatorCommissionDao")
    private OperatorCommissionDao operatorCommissionDao;

    @Override
    protected List<CommissionUnit> determineCommUnits(Transaction trans) throws ApplicationException {
        List<CommissionUnit> commUnits = new LinkedList<CommissionUnit>();

        OperatorCommission operatorComm = this.getOperatorCommissionDao().getByOperatorAndMerchantAndGame(
                trans.getOperatorId(), trans.getMerchantId(), trans.getGameId());
        if (operatorComm == null) {
            throw new DataIntegrityViolationException("No operator commission setting found by(operatorId="
                    + trans.getOperatorId() + ",merchantId=" + trans.getMerchantId() + ", gameId=" + trans.getGameId()
                    + ").");
        }
        CommissionUnit commUnit = new CommissionUnit(trans.getGameId(), operatorComm.getSaleRate(),
                trans.getTotalAmount(), BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);
        commUnits.add(commUnit);
        return commUnits;
    }

    public OperatorCommissionDao getOperatorCommissionDao() {
        return operatorCommissionDao;
    }

    public void setOperatorCommissionDao(OperatorCommissionDao operatorCommissionDao) {
        this.operatorCommissionDao = operatorCommissionDao;
    }

}
