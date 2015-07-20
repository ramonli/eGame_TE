package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BatchBookActiveStrategy extends AbstractCriteriaActiveStrategy {

    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        ActiveResult batchResult = new ActiveResult();
        JSONObject jsonResult = new JSONObject();
        List<InstantTicketSerialNo> serialList = this.parseCriteria(criteria);
        for (InstantTicketSerialNo no : serialList) {
            // check if the criteria supports the first ticket serial
            IgOperationParameter param = this.getIgOperationParameter(no.getGGG());

            // find the tickets by first and last ticket
            String beginSerial = this.assembleSerialNo(no, param.getBeginTicketIndex());
            String endSerial = this.assembleSerialNo(no, param.getEndTicketIndex());
            ActiveResult result = this.batchActive(beginSerial, endSerial, criteria.getTrans());

            if (result.isBatchSuccessful()) {
                batchResult.setCount(batchResult.getCount() + result.getCount());
            } else {
                batchResult.appendFailure(no.getBookNumber());
                batchResult.setErrorCode(result.getErrorCode());
                batchResult.setBatchSuccessful(false);
            }
            jsonResult.put(no.getBookNumber(), result.getErrorCode());
        }
        batchResult.setActiveResult(criteria.toJSONString(jsonResult));
        return batchResult;
    }

    private List<InstantTicketSerialNo> parseCriteria(ActiveCriteria criteria) throws ApplicationException {
        List<InstantTicketSerialNo> serialList = new ArrayList<InstantTicketSerialNo>();
        String bookTokens[] = criteria.getValue().split(ActiveCriteria.TOKEN_SEPERATOR);
        for (String bookToken : bookTokens) {
            if (!"".equals(bookToken)) {
                serialList.add(new InstantTicketSerialNo(bookToken + "000"));
            }
        }
        return serialList;
    }
}
