package com.mpos.lottery.te.gameimpl.union.sale.support;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumberValidatorFactory;
import com.mpos.lottery.te.gamespec.sale.support.validator.SingleSelectedNumberValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a default implementation to return default <code>SelectedNumberValidator</code>. It fits for Lotto game.
 */
public class UnionSelectedNumberValidatorFactory implements SelectedNumberValidatorFactory {
    private Log logger = LogFactory.getLog(UnionSelectedNumberValidatorFactory.class);

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
                validator = new UnionMultipleSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(
                        game.getType(), betOption));
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
