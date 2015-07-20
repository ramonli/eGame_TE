package com.mpos.lottery.te.gameimpl.digital.sale.support;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumberValidatorFactory;

public class DigitalSelectedNumberValidatorFactory implements SelectedNumberValidatorFactory {

    @Override
    public AbstractSelectedNumberValidator newSelectedNumberValidator(Game game, int betOption) {
        MLotteryContext mlotteryContext = MLotteryContext.getInstance();
        AbstractSelectedNumberValidator validator = null;

        String formatOfSelectedNumber = mlotteryContext.getWithNull(GameType.DIGITAL.getType() + ".selectednumber."
                + betOption);
        if (formatOfSelectedNumber != null) {
            if (betOption > 0) {
                validator = new DigitalXDSelectedNumberValidator(formatOfSelectedNumber);
            } else if (betOption == DigitalEntry.DIGITAL_BETOPTION_SUM) {
                validator = new DigitalSumSelectedNumberValidator(formatOfSelectedNumber);
            } else if (betOption == DigitalEntry.DIGITAL_BETOPTION_EVEN
                    || betOption == DigitalEntry.DIGITAL_BETOPTION_ODD) {
                validator = new DigitalOddEvenSelectedNumberValidator(formatOfSelectedNumber);
            }
        } else {
            throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported digital bet option:"
                    + betOption + ", no selecte number formmatter defintion found for given bet option");
        }

        return validator;
    }
}
