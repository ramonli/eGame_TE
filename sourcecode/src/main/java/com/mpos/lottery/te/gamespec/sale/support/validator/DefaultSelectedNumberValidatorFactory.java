package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a default implementation to return default <code>SelectedNumberValidator</code>. It fits for Lotto game.
 */
public class DefaultSelectedNumberValidatorFactory implements SelectedNumberValidatorFactory {
    private Log logger = LogFactory.getLog(DefaultSelectedNumberValidatorFactory.class);

    @Override
    public AbstractSelectedNumberValidator newSelectedNumberValidator(Game game, int betOption) {
        MLotteryContext mlotteryContext = MLotteryContext.getInstance();
        AbstractSelectedNumberValidator validator = null;
        switch (betOption) {
            case BaseEntry.BETOPTION_SINGLE :
                validator = new SingleSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(game.getType(),
                        betOption));
                break;
            case BaseEntry.BETOPTION_MULTIPLE :
                validator = new MultipleSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(game.getType(),
                        betOption));
                break;
            case BaseEntry.BETOPTION_BANKER :
                validator = new BankerSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(game.getType(),
                        betOption));
                break;
            case BaseEntry.BETOPTION_ROLL :
                validator = new RollSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(game.getType(),
                        betOption));
                break;
            default :
                throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported bet option:"
                        + betOption);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found ticket validator(" + validator + ") for bet option:" + betOption);
        }

        return validator;
    }
}
