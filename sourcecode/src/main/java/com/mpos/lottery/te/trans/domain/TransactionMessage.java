package com.mpos.lottery.te.trans.domain;

/**
 * For any transaction which want to persist JSON string to
 * {@code requestMsg} and {@code responseMsg} for later convenient usage,
 * you shouldn't access {@link #setRequestMsg(String)} and
 * {@link TransactionMessage#setResponseMsg(String)} directly, the better
 * way is to add a new entry to {@code reqJsonMap} or {@code respJsonMap}.
 * <p>
 * If call {@link #setRequestMsg(String)} directly, the code conflict may
 * occur, for example a sale transaction may involve {@code TicketService}
 * and {@code RiskControlService}, a developer call
 * {@link #setRequestMsg(String)} at {@code TicketService} to store some
 * JSON information for later usage, however another developer may call
 * {@link #setRequestMsg(String)} at {@code RiskControlService} again which
 * will clear the previous JSON information.
 * <p>
 * However we can't hide {@link #setRequestMsg(String)} and
 * {@link #setResponseMsg(String)}, they are necessary for JPA entity.
 * <p>
 * The better approach is to call {@link #addReqJsonEntry(String, Object)}
 * and {@link #addRespJsonEntry(String, Object)} accordingly, and when
 * persist transaction, TE will call {@link #} convert both {@code reqJsonMap} and
 * {@code respJsonMap} to JSON string, and persist them as well.
 */
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity(name = "TE_TRANSACTION_MSG")
public class TransactionMessage implements java.io.Serializable {
    private static final long serialVersionUID = -9040980125804902807L;

    @Id
    @Column(name = "TRANSACTION_ID")
    private String transactionId;

    @Column(name = "REQ_MESSAGE")
    private String requestMsg;

    @Column(name = "RES_MESSAGE")
    private String responseMsg;

    @Transient
    private Map<String, Object> reqJsonMap = new HashMap<String, Object>();
    @Transient
    private Map<String, Object> respJsonMap = new HashMap<String, Object>();

    public void addReqJsonEntry(String key, Object value) {
        if (this.reqJsonMap.get(key) != null) {
            throw new IllegalStateException("can NOT override the existed key[" + key + "] in request JSON map.");
        }
        reqJsonMap.put(key, value);
    }

    public void addRespJsonEntry(String key, Object value) {
        if (this.respJsonMap.get(key) != null) {
            throw new IllegalStateException("can NOT override the existed key[" + key + "] in response JSON map.");
        }
        respJsonMap.put(key, value);
    }

    public String encodeReqJsonMap() {
        return new Gson().toJson(this.reqJsonMap);
    }

    public String encodeRespJsonMap() {
        return new Gson().toJson(this.respJsonMap);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRequestMsg() {
        return requestMsg;
    }

    public void setRequestMsg(String requestMsg) {
        this.requestMsg = requestMsg;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

}
