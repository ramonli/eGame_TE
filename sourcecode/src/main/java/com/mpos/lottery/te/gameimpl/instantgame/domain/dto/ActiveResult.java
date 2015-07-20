package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Active result will be stored into TE_TRANSACTIN_MSG in following format.
 * <p/>
 * batch book : {"activeType":5,"activeResult":{"111000000120-111000000130":200, "111000013120-111000013130":400}}
 * <p/>
 * batch range : {"activeType":4,"activeResult":{"111000001":200,"111000010":400}}
 * <p/>
 * first ticket: {"activeType":3,"activeResult":{"111000001120":200}}
 * <p/>
 * last ticket : {"activeType":1,"activeResult":{"111000001130":200}}
 * <p/>
 * range ticket: {"activeType":2,"activeResult":{"111000001120,111000001120":200}}
 */
public class ActiveResult implements Serializable {
    private static final long serialVersionUID = -5789379024421411300L;
    private int count;
    // private List<InstantTicketDto> tickets = new
    // ArrayList<InstantTicketDto>();
    private String failure;
    private int errorCode = SystemException.CODE_OK;
    private boolean batchSuccessful = true;
    private String activeResult;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isBatchSuccessful() {
        return batchSuccessful;
    }

    public void setBatchSuccessful(boolean batchSuccessful) {
        this.batchSuccessful = batchSuccessful;
    }

    public String getActiveResult() {
        return activeResult;
    }

    public void setActiveResult(String activeResult) {
        this.activeResult = activeResult;
    }

    public void appendFailure(String criteriaEle) {
        if (criteriaEle == null || criteriaEle.equals("")) {
            return;
        }

        StringBuffer buffer = new StringBuffer();
        if (this.failure != null) {
            buffer.append(this.failure).append(ActiveCriteria.TOKEN_SEPERATOR).append(criteriaEle);
        } else {
            buffer.append(criteriaEle);
        }
        this.failure = buffer.toString();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
