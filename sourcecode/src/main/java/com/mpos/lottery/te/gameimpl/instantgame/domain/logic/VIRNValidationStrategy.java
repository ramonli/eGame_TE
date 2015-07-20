package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantVIRNPrize;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelItemDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VIRNValidationStrategy extends AbstractValidationStrategy {
    private Log logger = LogFactory.getLog(VIRNValidationStrategy.class);

    /**
     * @see ValidationStrategy#validate(InstantTicket, String, boolean).
     */
    public PrizeLevelDto validate(InstantTicket ticket, String virn, boolean isEnquiry) throws ApplicationException {
        // retrieve instant virn prize
        InstantVIRNPrize prize = this.getInstantVIRNPrizeDao().getByGameDrawAndVIRN(ticket.getGameDraw().getId(), virn);
        if (prize == null) {
            // if (!isEnquiry)
            // count retry-validation only when a incorrect validation.
            this.checkValidationRetry(ticket);
            throw new ApplicationException(SystemException.CODE_NO_INSTANTPRIZE,
                    "can NOT find instant VIRN prize record, this ticket(serialNo=" + ticket.getSerialNo()
                            + ", gameInstanceId=" + ticket.getGameDraw().getId() + ",VIRN=" + virn
                            + "') doesn't win a prize.");
        }
        // check if the VIRN has been validated. Different instant ticket will
        // associate with a
        // different VIRN, it means VIRN is unique.
        if (prize.isValidated()) {
            throw new ApplicationException(SystemException.CODE_VIRN_VALIDATED, "The VIRN(" + prize.getVirn()
                    + ") has been validated.");
        }

        // translate InstantVIRNPrize into PrizeLevel
        PrizeLevelDto instantPrize = new PrizeLevelDto();
        // InstantTicketService will calculate stat of prize based on prize
        // level item.
        PrizeLevelItemDto prizeItem = new PrizeLevelItemDto();
        prizeItem.setPrizeAmount(prize.getPrizeAmount());
        prizeItem.setTaxAmount(prize.getTaxAmount());
        prizeItem.setActualAmount(prize.getActualPayout());
        prizeItem.setNumberOfObject(1);
        // TODO how to support FREE_IG and FREE_LOTTO
        prizeItem.setPrizeType(prize.getIntPirzeType());
        instantPrize.getLevelItems().add(prizeItem);
        // set VIRN prize
        instantPrize.setVirnPrize(prize);
        return instantPrize;
    }

}
