package com.mpos.lottery.te.gameimpl.magic100.sale.support.validator;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumberValidatorFactory;

public class Magic100SelectedNumberValidatorFactory implements SelectedNumberValidatorFactory {

    @Override
    public AbstractSelectedNumberValidator newSelectedNumberValidator(Game game, int betOption) {
        MLotteryContext mlotteryContext = MLotteryContext.getInstance();
        AbstractSelectedNumberValidator validator = null;
        switch (betOption) {
            case BaseEntry.BETOPTION_SINGLE :
                validator = new Magic100SingleSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(
                        game.getType(), betOption));
                break;
            default :
                throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported bet option:"
                        + betOption);
        }
        return validator;

    }

}
