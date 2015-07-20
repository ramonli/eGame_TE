package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

public class CriteriaActiveStrategyProxy extends AbstractCriteriaActiveStrategy {

    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        CriteriaActiveStrategy strategy = this.createStrategy(criteria.getType());

        return strategy.active(criteria);
    }

    private CriteriaActiveStrategy createStrategy(int activeType) {
        CriteriaActiveStrategy strategy = null;
        if (ActiveCriteria.TYPE_BYLASTTICKET == activeType) {
            strategy = new LastTicketActiveStrategy();
        } else if (ActiveCriteria.TYPE_BYRANGETICKET == activeType) {
            strategy = new RangeTicketActiveStrategy();
        } else if (ActiveCriteria.TYPE_BYFIRSTTICKET == activeType) {
            strategy = new FirstTicketActiveStrategy();
        } else if (ActiveCriteria.TYPE_BYBATCHBOOK == activeType) {
            strategy = new BatchBookActiveStrategy();
        } else if (ActiveCriteria.TYPE_BYSINGLETICKET == activeType) {
            strategy = new SingleTicketActiveStrategy();
        } else if (ActiveCriteria.TYPE_BYBATCHRANGE == activeType) {
            strategy = new BatchRangeActiveStrategy();
        } else {
            throw new SystemException("Unsupported active criteria type:" + activeType);
        }
        // dependencies injection
        ((AbstractCriteriaActiveStrategy) strategy).setInstantGameDrawDao(this.getInstantGameDrawDao());
        ((AbstractCriteriaActiveStrategy) strategy).setInstantTicketDao(this.getInstantTicketDao());
        ((AbstractCriteriaActiveStrategy) strategy).setOperationParameterDao(this.getOperationParameterDao());
        return strategy;
    }
}
