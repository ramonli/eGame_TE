package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseFunType;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

public class DefaultTicketValidator implements TicketValidator {
    private Log logger = LogFactory.getLog(DefaultTicketValidator.class);
    // Spring dependencies
    private SelectedNumberValidatorFactory selectedNumberValidatorFactory = new DefaultSelectedNumberValidatorFactory();
    private BaseJpaDao baseJpaDao;

    @Override
    public final void validate(Context respCtx, BaseTicket clientTicket, Game game) throws ApplicationException {
        BaseFunType funType = this.lookupFunType(respCtx, game);
        BaseOperationParameter operationParam = this.lookupOperationParameter(respCtx, game);

        this.customizeValidateTicketBefore(respCtx, clientTicket, game, funType, operationParam);
        this.validateMaxAllowedMultipleDraw(respCtx, clientTicket, game, funType, operationParam);
        // validate entries one by one
        for (BaseEntry entry : clientTicket.getEntries()) {
            entry.setTicketSerialNo(clientTicket.getSerialNo());
            AbstractSelectedNumberValidator selectedNumberValidator = this.selectedNumberValidatorFactory
                    .newSelectedNumberValidator(game, entry.getBetOption());
            // configure validator
            selectedNumberValidator.setFunType(funType);
            selectedNumberValidator.setOperationParam(operationParam);
            // perform validation
            selectedNumberValidator.validate(respCtx, clientTicket, game, entry);

            // calculate total bets first
            long totalBets = selectedNumberValidator.calTotalBets(entry);
            entry.setTotalBets(totalBets);
            this.calculateEntryAmount(respCtx, entry, selectedNumberValidator);
            this.validateMaxAllowedEntryBets(respCtx, clientTicket, game, entry);
            this.customizeValidateTicketEntry(respCtx, clientTicket, game, entry);
        }

        this.validateAmount(respCtx, clientTicket, game);
        this.customizeValidateTicketAfter(respCtx, clientTicket, game, funType, operationParam);
    }

    protected void calculateEntryAmount(Context respCtx, BaseEntry entry,
            AbstractSelectedNumberValidator selectedNumberValidator) throws ApplicationException {
        BigDecimal entryAmount = selectedNumberValidator.calEntryAmount(entry);
        entry.setEntryAmount(entryAmount);
    }

    protected void validateMaxAllowedMultipleDraw(Context respCtx, BaseTicket clientTicket, Game game,
            BaseFunType funType, BaseOperationParameter operationParam) throws ApplicationException {
        if (operationParam == null) {
            return;
        }
        // check whether this sale exceeds the allowed multi-draw
        Merchant merchant = respCtx.getMerchant();
        // use the smaller one
        int maxAllowedMultiDraw = (merchant.getAllowedMultiDraw() > operationParam.getMaxAllowedMultiDraw())
                ? operationParam.getMaxAllowedMultiDraw()
                : merchant.getAllowedMultiDraw();
        if (logger.isDebugEnabled()) {
            logger.debug("THe max allowed multi-draw of operation parameter is "
                    + operationParam.getMaxAllowedMultiDraw() + ", merchant limit to " + merchant.getAllowedMultiDraw()
                    + ", the smaller " + maxAllowedMultiDraw + " will be used.");
        }
        if (clientTicket.getMultipleDraws() < operationParam.getMinAllowedMultiDraw()
                || clientTicket.getMultipleDraws() > maxAllowedMultiDraw) {
            throw new ApplicationException(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, "THe multiple draw["
                    + clientTicket.getMultipleDraws() + "] is not in range of allowed multiple-draws["
                    + operationParam.getMinAllowedMultiDraw() + "," + maxAllowedMultiDraw + "] of game(id="
                    + game.getId() + ")");
        }
    }

    protected void customizeValidateTicketEntry(Context respCtx, BaseTicket clientTicket, Game game, BaseEntry entry)
            throws ApplicationException {
        // template method
    }

    /**
     * verify the allowed bets of a entry.
     */
    protected void validateMaxAllowedEntryBets(Context respCtx, BaseTicket clientTicket, Game game, BaseEntry entry)
            throws ApplicationException {
        Merchant merchant = respCtx.getMerchant();
        // 0 means no limit
        if (merchant.getMaxMultipleBets() > 0 && entry.getTotalBets() > merchant.getMaxMultipleBets()) {
            throw new ApplicationException(SystemException.CODE_EXCEED_MAX_MULTIPLE,
                    "The total bets of selectd number:" + entry.getSelectNumber() + " is " + entry.getTotalBets()
                            + ", it exceeds the max allowed bets: " + merchant.getMaxMultipleBets()
                            + " of merchant(id=" + merchant.getId() + ").");
        }
    }

    /**
     * Validate whether ticket's amount is correct.
     */
    protected void validateAmount(Context respCtx, BaseTicket clientTicket, Game game) throws ApplicationException {
        BigDecimal expectedTotalAmount = this.doCalculateExpectTotalAmount(respCtx, clientTicket, game);

        if (clientTicket.getTotalAmount().compareTo(expectedTotalAmount) != 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The total amount("
                    + clientTicket.getTotalAmount() + ") of client ticket is "
                    + "unmatched with the server side(totalAmount=" + expectedTotalAmount + ",totalBets="
                    + clientTicket.getTotalBets() + ")");
        }
    }

    protected BigDecimal doCalculateExpectTotalAmount(Context respCtx, BaseTicket clientTicket, Game game) {
        BigDecimal expectedTotalAmount = new BigDecimal("0");
        int totalBets = 0;
        for (BaseEntry entry : clientTicket.getEntries()) {
            expectedTotalAmount = expectedTotalAmount.add(entry.getEntryAmount());
            totalBets += entry.getTotalBets();
        }
        expectedTotalAmount = expectedTotalAmount.multiply(new BigDecimal(clientTicket.getMultipleDraws()));
        clientTicket.setTotalBets(totalBets);

        return expectedTotalAmount;
    }

    /**
     * Template method for subclass to apply more checking before any validations.
     */
    protected void customizeValidateTicketBefore(Context respCtx, BaseTicket clientTicket, Game game,
            BaseFunType funType, BaseOperationParameter opParam) throws ApplicationException {
        // Template method for subclass
    }

    /**
     * Template method for subclass to apply more checking.
     */
    protected void customizeValidateTicketAfter(Context respCtx, BaseTicket clientTicket, Game game,
            BaseFunType funType, BaseOperationParameter opParam) throws ApplicationException {
    }

    protected BaseFunType lookupFunType(Context respCtx, Game game) {
        return this.getBaseJpaDao()
                .findById(GameType.fromType(game.getType()).getFunType(), game.getFunTypeId(), false);
    }

    protected BaseOperationParameter lookupOperationParameter(Context respCtx, Game game) {
        return this.getBaseJpaDao().findById(GameType.fromType(game.getType()).getOperationParametersType(),
                game.getOperatorParameterId(), false);
    }

    // ------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ------------------------------------------------------

    public SelectedNumberValidatorFactory getSelectedNumberValidatorFactory() {
        return selectedNumberValidatorFactory;
    }

    public void setSelectedNumberValidatorFactory(SelectedNumberValidatorFactory selectedNumberValidatorFactory) {
        this.selectedNumberValidatorFactory = selectedNumberValidatorFactory;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
