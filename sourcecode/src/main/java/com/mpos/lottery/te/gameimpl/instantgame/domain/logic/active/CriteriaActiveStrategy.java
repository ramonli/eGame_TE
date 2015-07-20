package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

public interface CriteriaActiveStrategy {

    /**
     * Find all instant tickets match the criteria.
     */
    ActiveResult active(ActiveCriteria criteria) throws ApplicationException;
}
