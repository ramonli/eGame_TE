package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

import org.json.simple.JSONObject;

public class LastTicketActiveStrategy extends AbstractCriteriaActiveStrategy {

    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        InstantTicketSerialNo no = new InstantTicketSerialNo(criteria.getValue());

        // check if the criteria supports the first ticket serial
        IgOperationParameter param = this.getIgOperationParameter(no.getGGG());
        checkCriteria(criteria, no, param);

        // find the tickets by first and last ticket
        String beginSerial = this.assembleSerialNo(no, param.getBeginTicketIndex());
        String endSerial = this.assembleSerialNo(no, param.getEndTicketIndex());

        ActiveResult result = this.batchActive(beginSerial, endSerial, criteria.getTrans());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(criteria.getValue(), result.getErrorCode());
        result.setActiveResult(criteria.toJSONString(jsonObject));
        return result;
    }

    protected void checkCriteria(ActiveCriteria criteria, InstantTicketSerialNo no, IgOperationParameter param)
            throws ApplicationException {
        if (no.getLongIndex() != param.getEndTicketIndex()) {
            throw new ApplicationException(SystemException.CODE_NOT_LAST_SERIAL, "The serial number("
                    + criteria.getValue() + ") isn't the last " + "serial in book(number=" + no.getGGG() + no.getBBB()
                    + ") of game instance(" + no.getGGG() + ").");
        }
    }

}
