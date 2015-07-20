package com.mpos.lottery.te.valueaddservice.voucher.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.merchant.dao.MerchantCommissionDao;
import com.mpos.lottery.te.merchant.domain.MerchantCommission;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.balance.SaleBalanceStrategy;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherOperationParameter;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherSale;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherStatistics;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherDao;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherSaleDao;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherStatisticsDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

public class DefaultVoucherService extends AbstractReversalOrCancelStrategy implements VoucherService {
    private Log logger = LogFactory.getLog(DefaultVoucherService.class);
    @Resource(name = "jpaVoucherDao")
    private VoucherDao voucherDao;
    @Resource(name = "jpaVoucherSaleDao")
    private VoucherSaleDao voucherSaleDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "saleCommissionBalanceService")
    private CommissionBalanceService commissionService;
    @Resource(name = "merchantCommissionDao")
    private MerchantCommissionDao merchantCommissionDao;
    @Resource(name = "jpaVoucherStatDao")
    private VoucherStatisticsDao voucherStatDao;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Voucher sell(Context respCtx, Voucher reqDto) throws ApplicationException {
        Assert.notNull(reqDto.getFaceAmount(), "The  face amount can't be null");
        Assert.notNull(reqDto.getGame().getId(), "THe game.id can't be null");

        // --------------------------------
        // Verify Pro-Conditions
        // --------------------------------
        // verify whether game have been allocated to merchant
        this.verifyGameAllocation(respCtx, reqDto);
        // verify sale balance and lock it to avoid other transaction modify it
        this.verifySaleBalance(respCtx, reqDto);

        // --------------------------------
        // Sell Voucher
        // --------------------------------
        VoucherOperationParameter opPara = this.getBaseJpaDao().findById(VoucherOperationParameter.class,
                reqDto.getGame().getOperatorParameterId());
        Voucher voucher = this.getVoucherDao().findByGameAndFaceAmount(reqDto.getGame().getId(),
                reqDto.getFaceAmount(), opPara.getBufferExpireDay());
        if (voucher == null) {
            throw new ApplicationException(SystemException.CODE_NO_VOUCHER, "No voucher found by (gameId:"
                    + reqDto.getGame().getId() + ",faceAmount:" + reqDto.getFaceAmount() + ", bufferExpireDay:"
                    + opPara.getBufferExpireDay() + ")");
        }
        voucher.setStatus(Voucher.STATUS_SOLD);
        voucher.decryptPin();
        voucher.setUpdateTime(respCtx.getTransaction().getCreateTime());
        // decrypt the PIN of voucher
        this.getVoucherDao().update(voucher);
        // generate voucher sale entity
        VoucherSale sale = new VoucherSale();
        sale.setId(respCtx.getTransaction().getId());
        sale.setCreateTime(respCtx.getTransaction().getCreateTime());
        sale.setUpdateTime(sale.getCreateTime());
        sale.setDevId(respCtx.getTransaction().getDeviceId());
        sale.setMerchantId(respCtx.getTransaction().getMerchantId());
        sale.setOperatorId(respCtx.getTransaction().getOperatorId());
        sale.setGame(reqDto.getGame());
        sale.setStatus(VoucherSale.STATUS_SUCCESS);
        sale.setTransaction(respCtx.getTransaction());
        sale.setVoucherFaceAmount(reqDto.getFaceAmount());
        sale.setVoucherId(voucher.getId());
        sale.setVoucherSerialNo(voucher.getSerialNo());
        this.getBaseJpaDao().insert(sale);

        // maintain voucher stat
        VoucherStatistics voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(reqDto.getFaceAmount(),
                reqDto.getGame().getId());
        if (voucherStat != null) {
            this.getEntityManager().lock(voucherStat, LockModeType.PESSIMISTIC_READ);
            voucherStat.setRemainCount(voucherStat.getRemainCount() - 1);
            this.getVoucherStatDao().update(voucherStat);
        }

        // --------------------------------
        // Maintain Transaction
        // --------------------------------
        respCtx.getTransaction().setGameId(reqDto.getGame().getId());
        respCtx.getTransaction().setTotalAmount(reqDto.getFaceAmount());
        respCtx.getTransaction().setTicketSerialNo(voucher.getSerialNo());

        // --------------------------------
        // Maintain sale balance and commission
        // --------------------------------
        // maintain the balance/commission the same way with sale.
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
        // update sale balance
        Object operatorMerchant = this.getBalanceService().balance(respCtx, BalanceService.BALANCE_TYPE_SALE,
                respCtx.getTransaction().getOperatorId(), false);
        // generate voucher sale transaction records
        this.getCommissionService().calCommission(respCtx, operatorMerchant);

        return voucher;
    }

    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(GameType.TELECO_VOUCHER.getType(), TransactionType.SELL_TELECO_VOUCHER.getRequestType());
    }

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        VoucherSale sale = this.getVoucherSaleDao().findByTransaction(targetTrans.getId());
        if (sale == null) {
            throw new SystemException("No voucher record found by transId=" + targetTrans.getId());
        }
        if (VoucherSale.STATUS_SUCCESS != sale.getStatus()) {
            logger.warn("Only successful transaction can be cancelled, the status of trans(transId="
                    + targetTrans.getId() + ") is " + sale.getStatus());
            return false;
        }
        sale.setUpdateTime(respCtx.getTransaction().getCreateTime());
        sale.setStatus(VoucherSale.STATUS_CANCEL);
        this.getVoucherSaleDao().update(sale);

        Voucher voucher = this.getVoucherDao().findById(Voucher.class, sale.getVoucherId());
        if (voucher == null) {
            throw new SystemException("No voucher found by id=" + sale.getVoucherId());
        }
        if (Voucher.STATUS_SOLD != voucher.getStatus()) {
            logger.warn("Only sold voucher can be cancelled, the status of voucher(id=" + voucher.getId() + ") is "
                    + voucher.getStatus());
            return false;
        }
        voucher.setUpdateTime(respCtx.getTransaction().getCreateTime());
        voucher.setStatus(Voucher.STATUS_IMPORTED);
        this.getVoucherDao().update(voucher);

        // maintain voucher stat
        VoucherStatistics voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(targetTrans.getTotalAmount(),
                targetTrans.getGameId());
        if (voucherStat != null) {
            this.getEntityManager().lock(voucherStat, LockModeType.PESSIMISTIC_READ);
            voucherStat.setRemainCount(voucherStat.getRemainCount() + 1);
            this.getVoucherStatDao().update(voucherStat);
        }

        // --------------------------------
        // Maintain sale balance and commission
        // --------------------------------
        // maintain the balance/commission the same way with sale.
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
        // update sale balance
        Object operatorMerchant = this.getBalanceService().balance(respCtx, BalanceService.BALANCE_TYPE_SALE,
                targetTrans.getOperatorId(), true);
        // generate voucher sale transaction records
        this.getCommissionService().cancelCommission(respCtx, targetTrans, operatorMerchant);

        return false;
    }

    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------

    private void verifyGameAllocation(Context respCtx, Voucher reqDto) throws ApplicationException {
        // verify game status, actually a game represent a airtime service provider(telecom operator).
        Game game = this.getBaseJpaDao().findById(Game.class, reqDto.getGame().getId());
        if (game == null) {
            throw new SystemException("No game found by id=" + reqDto.getGame().getId());
        }
        if (Game.STATUS_ACTIVE != game.getState()) {
            throw new ApplicationException(SystemException.CODE_GAME_INACTIVE, "Game(id=" + reqDto.getGame().getId()
                    + ") isn't active.");
        }
        reqDto.setGame(game);
        MerchantCommission comm = this.getMerchantCommissionDao().getByMerchantAndGame(respCtx.getMerchant().getId(),
                game.getId());
        if (comm == null) {
            throw new SystemException(SystemException.CODE_OPERATOR_SELL_NOPRIVILEDGE, "operator(id="
                    + respCtx.getOperatorId() + ") has no priviledge to sell voucher of game(teleco operator) '"
                    + game.getId() + "', allocate the game to its merchant(id=" + respCtx.getMerchant().getId()
                    + ") first.");
        }
    }

    /**
     * Verify whether the sale balance of operator/merchant is enough for transaction, and lock it for later updating.
     */
    protected void verifySaleBalance(Context respCtx, Voucher reqDto) throws ApplicationException {
        this.getBalanceService().lockAndVerifySaleBalance(respCtx.getTransaction().getOperatorId(),
                respCtx.getTransaction().getMerchantId(), reqDto.getFaceAmount());
    }

    // -------------------------------------------------------
    // SPRING DEPENDENDIES
    // -------------------------------------------------------

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

    public MerchantCommissionDao getMerchantCommissionDao() {
        return merchantCommissionDao;
    }

    public void setMerchantCommissionDao(MerchantCommissionDao merchantCommissionDao) {
        this.merchantCommissionDao = merchantCommissionDao;
    }

    public VoucherDao getVoucherDao() {
        return voucherDao;
    }

    public void setVoucherDao(VoucherDao voucherDao) {
        this.voucherDao = voucherDao;
    }

    public VoucherSaleDao getVoucherSaleDao() {
        return voucherSaleDao;
    }

    public void setVoucherSaleDao(VoucherSaleDao voucherSaleDao) {
        this.voucherSaleDao = voucherSaleDao;
    }

    public VoucherStatisticsDao getVoucherStatDao() {
        return voucherStatDao;
    }

    public void setVoucherStatDao(VoucherStatisticsDao voucherStatDao) {
        this.voucherStatDao = voucherStatDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
