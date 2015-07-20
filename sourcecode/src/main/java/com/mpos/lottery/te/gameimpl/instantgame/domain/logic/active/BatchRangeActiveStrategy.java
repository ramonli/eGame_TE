package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BatchRangeActiveStrategy extends RangeTicketActiveStrategy {

    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        ActiveResult batchResult = new ActiveResult();
        JSONObject jsonResult = new JSONObject();
        List<SerialRange> rangeList = this.parseCriteria(criteria);
        for (SerialRange range : rangeList) {
            // check the range
            ActiveResult result = new ActiveResult();
            try {
                this.checkRange(criteria, range.getBeginSerial().getSerialNo(), range.getEndSerial().getSerialNo());
                result = this.batchActive(range.getBeginSerial().getSerialNo(), range.getEndSerial().getSerialNo(),
                        criteria.getTrans());
            } catch (ApplicationException e) {
                result.setBatchSuccessful(false);
                result.setErrorCode(e.getErrorCode());
                logger.warn(e.getMessage(), e);
            }

            if (result.isBatchSuccessful()) {
                batchResult.setCount(batchResult.getCount() + result.getCount());
            } else {
                batchResult.appendFailure(range.getRange());
                batchResult.setErrorCode(result.getErrorCode());
                batchResult.setBatchSuccessful(false);
            }
            jsonResult.put(range.getRange(), result.getErrorCode());
        }
        batchResult.setActiveResult(criteria.toJSONString(jsonResult));
        return batchResult;
    }

    private List<SerialRange> parseCriteria(ActiveCriteria criteria) throws ApplicationException {
        List<SerialRange> rangeList = new ArrayList<SerialRange>();
        String criteriaTokens[] = criteria.getValue().split(ActiveCriteria.TOKEN_SEPERATOR);
        for (String criteriaToken : criteriaTokens) {
            if (!"".equals(criteriaToken)) {
                int index = criteriaToken.indexOf(SerialRange.RANGE_SEPERATOR);
                if (index == -1) {
                    throw new ApplicationException(SystemException.CODE_WRONGFORMAT_CRITERIA,
                            "NO '-' found in criteria for 'batch range activation'.");
                }
                InstantTicketSerialNo beginSerial = new InstantTicketSerialNo(criteriaToken.substring(0, index));
                InstantTicketSerialNo endSerial = new InstantTicketSerialNo(criteriaToken.substring(index + 1));
                SerialRange range = new SerialRange();
                range.setBeginSerial(beginSerial);
                range.setEndSerial(endSerial);
                rangeList.add(range);
            }
        }
        return rangeList;
    }

    private class SerialRange {
        public final static String RANGE_SEPERATOR = "-";
        private InstantTicketSerialNo beginSerial;
        private InstantTicketSerialNo endSerial;

        public InstantTicketSerialNo getBeginSerial() {
            return beginSerial;
        }

        public void setBeginSerial(InstantTicketSerialNo beginSerial) {
            this.beginSerial = beginSerial;
        }

        public InstantTicketSerialNo getEndSerial() {
            return endSerial;
        }

        public void setEndSerial(InstantTicketSerialNo endSerial) {
            this.endSerial = endSerial;
        }

        public String getRange() {
            return new StringBuffer().append(this.beginSerial.getSerialNo()).append(RANGE_SEPERATOR)
                    .append(this.endSerial.getSerialNo()).toString();
        }
    }
}
