package com.mpos.lottery.te.gameimpl.lfn.sale.support.validator;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnEntry;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumberValidatorFactory;

public class LfnSelectedNumberValidatorFactory implements SelectedNumberValidatorFactory {

    @Override
    public AbstractSelectedNumberValidator newSelectedNumberValidator(Game game, int betOption) {
        MLotteryContext mlotteryContext = MLotteryContext.getInstance();
        AbstractSelectedNumberValidator validator = null;
        // 1..50 is N option, and 51..100 is P option
        if (betOption <= LfnEntry.BETOPTION_INTERVAL) {
            validator = new LfnNmodeSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(game.getType(),
                    betOption), betOption);
        } else if (betOption < LfnEntry.BETOPTION_INTERVAL * 2) {
            validator = new LfnPmodeSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(game.getType(),
                    betOption), betOption);
        } else {
            throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported bet option:" + betOption);
        }
        return validator;
    }

}
