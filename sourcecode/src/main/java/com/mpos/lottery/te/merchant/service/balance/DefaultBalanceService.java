package com.mpos.lottery.te.merchant.service.balance;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

public class DefaultBalanceService implements BalanceService {
    private Log logger = LogFactory.getLog(DefaultBalanceService.class);
    // Key will be BalanceService.BALANCE_TYPE.AAA
    private Map<Integer, BalanceStrategy> transactionBalanceStrategyMap = new HashMap<Integer, BalanceStrategy>();

    // spring dependencies
    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;
    private BaseJpaDao baseJpaDao;
    private OperatorMerchantDao operatorMerchantDao;

    @Override
    public Object balance(Context<?> respCtx, Transaction targetTrans, int balanceType, String operatorId,
                    boolean isTopup) throws ApplicationException {
        Assert.notNull(targetTrans.getTotalAmount(), "No totalAmount field of transaction has been set.");

        Operator operator = this.getBaseJpaDao().findById(Operator.class, operatorId, false);
        if (operator.isIgnoreCredit()) {
            logger.info("Ignore the balance setting of operator(" + operator + ")");
            return null;
        }
        // lookup leaf merchant
        OperatorMerchant operatorMerchant = this.getOperatorMerchantDao().findByOperator(operatorId);
        if (operatorMerchant == null) {
            throw new ApplicationException(SystemException.CODE_OPERATOR_NO_MERCHANT, "operator(id=" + operatorId
                            + ") doesn't belong to any merchant, allocate it first.");
        }
        Merchant leafMerchant = this.getBaseJpaDao().findById(Merchant.class, operatorMerchant.getMerchantID(), false);

        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == operator.getCreditType()) {
            return doOperatorBalance(respCtx, targetTrans, balanceType, operator, leafMerchant, isTopup);
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == operator.getCreditType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore balance calculation of " + operator
                                + ", as its credit type is USE PARENT... check its parent merchant");
            }
            return this.doMerchantBalance(respCtx, targetTrans, balanceType, operator, leafMerchant, isTopup);
        } else {
            throw new SystemException("Unsupported credit type:" + operator.getCreditType() + " of operator("
                            + operator + ")");
        }
    }

    @Override
    public Object balance(Context<?> respCtx, int balanceType, String operatorId, boolean isTopup)
                    throws ApplicationException {
        return this.balance(respCtx, respCtx.getTransaction(), balanceType, operatorId, isTopup);
    }

    @Override
    public Object lockAndVerifySaleBalance(String operatorId, long lefaMerchantId, BigDecimal transAmount)
                    throws ApplicationException {
        Operator operator = this.getBaseJpaDao().findById(Operator.class, operatorId, false);
        if (operator.isIgnoreCredit()) {
            logger.info("Ignore the balance maintenance of operator(" + operator + ")");
            return null;
        }

        Object balanceNode = null;
        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == operator.getCreditType()) {
            balanceNode = operator;
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == operator.getCreditType()) {
            Merchant leafMerchant = this.getBaseJpaDao().findById(Merchant.class, lefaMerchantId);
            balanceNode = this.lookupDefinteMerchant(leafMerchant);
        } else {
            throw new SystemException("Unsupported credit type:" + operator.getCreditType() + " of operator("
                            + operator + ")");
        }

        if (balanceNode != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found the balance node(" + balanceNode + ")");
            }
            this.getEntityManager().lock(balanceNode, LockModeType.PESSIMISTIC_READ);
            BigDecimal saleBalance = null;
            if (balanceNode instanceof Operator) {
                saleBalance = ((Operator) balanceNode).getSaleCreditLevel();
            } else if (balanceNode instanceof Merchant) {
                saleBalance = ((Merchant) balanceNode).getSaleCreditLevel();
            } else {
                throw new SystemException("Unsupported balance node instance(" + balanceNode + ")");
            }
            if (saleBalance.compareTo(transAmount) < 0) {
                throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT, "The sale balance ("
                                + saleBalance + ") of " + balanceNode + " isn't enough for transacion(amount="
                                + transAmount + ").");
            }
        }
        return balanceNode;
    }

    /**
     * Register a {@code TransactionBalanceStrategy} for a given transaction type. For a given transaction type, only a
     * single strategy implementation can be registered, however a strategy implementation can support multiple
     * transaction types.
     */
    public void registerBalanceStrategy(BalanceStrategy strategy) {
        BalanceStrategy existed = this.transactionBalanceStrategyMap.get(strategy.supportedBalanceType());
        if (existed != null) {
            throw new SystemException("Fail to register (" + strategy + ") with balance type("
                            + strategy.supportedBalanceType() + "), it has been registered by " + existed);
        }
        this.transactionBalanceStrategyMap.put(strategy.supportedBalanceType(), strategy);
    }

    /**
     * Lookup a {@code TransactionBalanceStrategy} for a give transaction type, if no found a {@code SystemException}
     * will be thrown out. null will be returned if no strategy implementation found.
     */
    public BalanceStrategy lookupBalanceStrategy(Integer balanceType) {
        BalanceStrategy strategy = this.transactionBalanceStrategyMap.get(balanceType);
        if (strategy == null) {
            throw new SystemException("No " + BalanceStrategy.class + " found for balance type:" + balanceType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Found balance strategy:" + strategy);
        }
        return strategy;
    }

    // ----------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------

    /**
     * Maintain all supported type of balance of operator.
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param targetTrans
     *            The target transaction. To a cancellation, it is the transaction which has been cancelled, however to
     *            a normal transactions, such as sale, payout etc, it should be right current transaction.
     * @param balanceType
     *            Which balance should be maintained?
     * @param operator
     *            Whose balance should be maintained?
     * @param leafMerchant
     *            The leaf merchant of given operator.
     * @param isTopup
     *            topup or deduct the balance?
     * @return The operator whose balance has been maintained.
     * @throws ApplicationException
     *             if encounters any business exception.
     */
    protected final Operator doOperatorBalance(Context<?> respCtx, Transaction targetTrans, int balanceType,
                    Operator operator, Merchant leafMerchant, boolean isTopup) throws ApplicationException {
        // Lock the entity first for late updating.
        //
        // If try to refresh a entity before flush state changes into underlying
        // database, the changes of entity will be lost.
        this.getEntityManager().flush();
        // Refresh entity to latest state of underlying database and lock it.
        this.getEntityManager().refresh(operator, LockModeType.PESSIMISTIC_READ);

        if (logger.isDebugEnabled()) {
            logger.debug("Operator before transaction -  " + operator);
        }

        BalanceStrategy balanceStrategy = this.lookupBalanceStrategy(balanceType);
        Operator targetOperator = balanceStrategy
                        .balanceOperator(respCtx, targetTrans, operator, leafMerchant, isTopup);

        if (targetOperator != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Operator after transaction -  " + operator);
            }

            this.getBaseJpaDao().update(operator);
        }
        return targetOperator;
    }

    private Merchant lookupDefinteMerchant(Merchant merchant) {
        if (merchant.getId() == Merchant.SUPER_MERCHANT_ID) {
            throw new SystemException("can't find difinite-credit type merchant, as has reached the top merchant(id="
                            + Merchant.SUPER_MERCHANT_ID + ")");
        }
        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == merchant.getCreditType()) {
            return merchant;
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == merchant.getCreditType()) {
            // invoke method recursively
            return this.lookupDefinteMerchant(merchant.getParentMerchant());
        } else {
            return null;
        }
    }

    /**
     * Maintain all supported type of balance of merchant.
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param targetTrans
     *            The target transaction. To a cancellation, it is the transaction which has been cancelled, however to
     *            a normal transactions, such as sale, payout etc, it should be right current transaction.
     * @param balanceType
     *            Which balance should be maintained?
     * @param operator
     *            Whose balance should be maintained?
     * @param merchant
     *            The leaf merchant of given operator.
     * @param isTopup
     *            topup or deduct the balance?
     * @return The operator whose balance has been maintained.
     * @throws ApplicationException
     *             if encounters any business exception.
     */
    protected final Merchant doMerchantBalance(Context<?> respCtx, Transaction targetTrans, int balanceType,
                    Operator operator, Merchant merchant, boolean isTopup) throws ApplicationException {
        if (merchant.getId() == Merchant.SUPER_MERCHANT_ID) {
            throw new SystemException("can't calculate balance, as has reached the top merchant(id="
                            + Merchant.SUPER_MERCHANT_ID + ")");
        }
        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == merchant.getCreditType()) {
            // If try to refresh a entity before flush state changes into
            // underlying database, the changes of entity will be lost.
            this.getEntityManager().flush();
            // Refresh entity to latest state of underlying database and lock
            // it.
            this.getEntityManager().refresh(merchant, LockModeType.PESSIMISTIC_READ);

            if (logger.isDebugEnabled()) {
                logger.debug("Merchant before transaction: " + merchant);
            }

            BalanceStrategy balanceStrategy = this.lookupBalanceStrategy(balanceType);
            Merchant targetMerchant = balanceStrategy
                            .balanceMerchant(respCtx, targetTrans, operator, merchant, isTopup);
            if (targetMerchant != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Merchant after transaction -  " + operator);
                }

                this.getBaseJpaDao().update(merchant);
            }

            return merchant;
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == merchant.getCreditType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore balance calculation, as the credit type of" + merchant
                                + " is USE PARENT... check its parent merchant.");
            }
            // invoke method recursively
            return this.doMerchantBalance(respCtx, targetTrans, balanceType, operator, merchant.getParentMerchant(),
                            isTopup);
        } else {
            logger.warn("Ignore calculation of balance, as the credit type of " + merchant + " is "
                            + merchant.getCreditType());
            return null;
        }
    }

    // ----------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------

    public OperatorMerchantDao getOperatorMerchantDao() {
        return operatorMerchantDao;
    }

    public void setOperatorMerchantDao(OperatorMerchantDao operatorMerchantDao) {
        this.operatorMerchantDao = operatorMerchantDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
