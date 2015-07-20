package com.mpos.lottery.te.trans.domain.logic;

import com.mpos.lottery.te.port.domain.router.RoutineKey;

import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractReversalOrCancelStrategy implements ReversalOrCancelStrategy, InitializingBean {
    protected ReversalOrCancelStrategyFactory reversalStrategyFactory;
    private String reversalStrategyKey;

    /**
     * Register a reversal strategy implementation.
     * <p/>
     * NOTE: As implement <code>InitializaingBean</code>, we are coupled with Spring framework. A alternative approach
     * is announcing a 'init-method' attribute in spring configuration file. My consideration is if by announcing
     * 'init-method', a developer may forget to do it.
     * <p/>
     * Anyway we always make tradeoff in our life :).
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.getReversalStrategyFactory().register(this.supportedReversalRoutineKey(), this);
    }

    public ReversalOrCancelStrategyFactory getReversalStrategyFactory() {
        return reversalStrategyFactory;
    }

    public void setReversalStrategyFactory(ReversalOrCancelStrategyFactory factory) {
        this.reversalStrategyFactory = factory;
    }

    /**
     * Each <code>ReversalOrCancelStrategy</code> instance must be assigned a unique key,
     * <code>ReversalOrCancelStrategyFactory</code> will use the key to lookup appreciate
     * <code>ReversalOrCancelStrategy</code> instance to handle incoming request.
     * <p/>
     * The transaction type of routine key must be target transaction type, for example, a incoming request of cancel by
     * transactin(type=206) intends to cancel sale(type=200), so the transaction type of routine key should be 200, not
     * 206.
     * <p/>
     * The game type and version definition should be the setting in incoming reversal or cancel request.
     */
    public String getReversalStrategyKey() {
        return reversalStrategyKey;
    }

    /**
     * Set the routine key which maps to a strategy. You can override {@link #supportedReversalRoutineKey()} or set
     * property <code>'reversalStrategyKey'</code> in spring configuration file to change the routine key.
     * <p/>
     * How to configure if multiple routine keys will map to a single strategy?? You can define multiple instances of
     * <code>AbstractReversalOrCancelStrategy</code> and allocate each of them a different routine key in spring
     * configuration file.
     * 
     * @param strategyKey
     *            The JSON string which represents the routine key, for example {gameType:1,transType:200,version:1.0}
     */
    public void setReversalStrategyKey(String strategyKey) {
        this.reversalStrategyKey = strategyKey;
    }

    /**
     * @see ReversalOrCancelStrategy#supportedReversalRoutineKey().
     */
    public RoutineKey supportedReversalRoutineKey() {
        if (this.getReversalStrategyKey() == null) {
            throw new IllegalStateException("No RoutineKey value has been assigned(" + this + ").");
        }
        RoutineKey key = new RoutineKey();
        key.from(this.getReversalStrategyKey());
        return key;
    }
}
