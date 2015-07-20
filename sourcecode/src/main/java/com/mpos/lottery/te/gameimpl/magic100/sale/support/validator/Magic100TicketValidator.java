package com.mpos.lottery.te.gameimpl.magic100.sale.support.validator;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100OperationParameter;
import com.mpos.lottery.te.gamespec.game.BaseFunType;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.port.Context;

public class Magic100TicketValidator extends DefaultTicketValidator {

    @Override
    protected BaseFunType lookupFunType(Context respCtx, Game game) {
        return null;
    }

    @Override
    protected BaseOperationParameter lookupOperationParameter(Context respCtx, Game game) {
        return this.getBaseJpaDao().findById(Magic100OperationParameter.class, game.getOperatorParameterId());
    }

}
