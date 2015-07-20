package com.mpos.lottery.te.gameimpl.union.sale.support;

import com.mpos.lottery.te.gameimpl.union.game.UnionOperationParameter;
import com.mpos.lottery.te.gameimpl.union.game.dao.UnionCOperationParameterDao;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.port.Context;

public class UnionTicketValidator extends DefaultTicketValidator {
    private UnionCOperationParameterDao cOperationParameterDao;

    @Override
    protected BaseOperationParameter lookupOperationParameter(Context respCtx, Game game) {
        UnionOperationParameter opParameter = (UnionOperationParameter) super.lookupOperationParameter(respCtx, game);
        opParameter.setcOperationParameters(this.getcOperationParameterDao().findByOperationParameter(
                opParameter.getId()));
        return opParameter;
    }

    public UnionCOperationParameterDao getcOperationParameterDao() {
        return cOperationParameterDao;
    }

    public void setcOperationParameterDao(UnionCOperationParameterDao cOperationParameterDao) {
        this.cOperationParameterDao = cOperationParameterDao;
    }

}
