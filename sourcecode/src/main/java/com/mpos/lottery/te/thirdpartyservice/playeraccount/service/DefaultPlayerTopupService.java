package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerTopupDto;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import javax.annotation.Resource;

public class DefaultPlayerTopupService extends AbstractReversalOrCancelStrategy implements PlayerTopupService {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "playerTopupCommissionBalanceService")
    private CommissionBalanceService commissionService;

    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(TransactionType.PLAYER_TOPUP.getRequestType());
    }

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        // --------------------------------
        // Maintain sale balance and commission
        // --------------------------------
        // update topup balance
        Object operatorMerchant = this.getBalanceService().balance(respCtx, BalanceService.BALANCE_TYPE_SALE,
                targetTrans.getOperatorId(), true);
        // generate voucher sale transaction records
        this.getCommissionService().cancelCommission(respCtx, targetTrans, operatorMerchant);
        return false;
    }

    @Override
    public void topup(Context respCtx, PlayerTopupDto dto) throws ApplicationException {
        respCtx.getTransaction().setTotalAmount(dto.getAmount());
        respCtx.getTransaction().setTicketSerialNo(dto.getVoucherSerialNo());
        // set accountID to VIRN...a workaround
        respCtx.getTransaction().setVirn(dto.getAccountId());

        // --------------------------------
        // Maintain sale balance and commission
        // --------------------------------
        // update topup balance
        Object operatorMerchant = this.getBalanceService().balance(respCtx, BalanceService.BALANCE_TYPE_SALE,
                respCtx.getTransaction().getOperatorId(), false);
        // generate voucher sale transaction records
        this.getCommissionService().calCommission(respCtx, operatorMerchant);
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public CommissionBalanceService getCommissionService() {
        return commissionService;
    }

    public void setCommissionService(CommissionBalanceService commissionService) {
        this.commissionService = commissionService;
    }

}
