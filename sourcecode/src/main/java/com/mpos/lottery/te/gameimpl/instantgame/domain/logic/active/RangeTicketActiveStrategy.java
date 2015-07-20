package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

import org.json.simple.JSONObject;

public class RangeTicketActiveStrategy extends AbstractCriteriaActiveStrategy {

    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        String value = criteria.getValue();
        int index = value.indexOf(",");
        if (index == -1) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SERIALNO, "Wrong criteria format for type "
                    + criteria.getType() + ": " + criteria.getValue() + ". The serial number must be seperated by ','.");
        }
        String beginSerial = value.substring(0, index);
        String endSerial = value.substring(index + 1);

        checkRange(criteria, beginSerial, endSerial);

        ActiveResult result = this.batchActive(beginSerial, endSerial, criteria.getTrans());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(criteria.getValue(), result.getErrorCode());
        result.setActiveResult(criteria.toJSONString(jsonObject));
        return result;
    }

    protected void checkRange(ActiveCriteria criteria, String beginSerial, String endSerial)
            throws ApplicationException {
        InstantTicketSerialNo begin = new InstantTicketSerialNo(beginSerial);
        InstantTicketSerialNo end = new InstantTicketSerialNo(endSerial);
        // check if the begin/end serial number are in same book.
        if (!begin.getGGG().equals(end.getGGG()) || !begin.getBBB().equals(end.getBBB())) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_CRITERIA, "Wrong criteria format for type "
                    + criteria.getType() + ": " + criteria.getValue() + ". Two serialNos must belong to same book.");
        }
        // check if the index of end ticket is greader than begin ticket.
        if (begin.getLongIndex() > end.getLongIndex()) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_CRITERIA, "Wrong criteria format for type "
                    + criteria.getType() + ": " + criteria.getValue()
                    + ". The end serialNo must be greater then begin serialNo.");
        }
        // check if the begin/end serial number are between MIN and MAX ticket
        // serial number.
        IgOperationParameter param = this.getIgOperationParameter(begin.getGGG());
        if (begin.getLongIndex() < param.getBeginTicketIndex() || end.getLongIndex() > param.getEndTicketIndex()) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_CRITERIA,
                    "The index of begin/end serial number(" + beginSerial + "/" + endSerial + ") should be between "
                            + param.getBeginTicketIndex() + " and " + param.getEndTicketIndex());
        }
    }

}
