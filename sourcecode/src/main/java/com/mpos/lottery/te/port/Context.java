package com.mpos.lottery.te.port;

import com.mpos.lottery.te.common.Command;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.merchant.domain.Device;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.thirdpartyservice.amqp.MessagePack;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Define the transaction context(request/response)
 */
public class Context<T> implements Cloneable {
    public static final String KEY_MESSAGE_CONTEXT = "KEY_MESSAGE_CONTEXT";
    public static final String KEY_PUBLISH_AMQP_MESSAGE = "KEY_PUBLISH_AMQP_MESSAGE";
    // header definition
    public static final String HEADER_PROTOCAL_VERSION = RequestHeaders.HEADER_PROTOCAL_VERSION;
    public static final String HEADER_TRACE_MESSAGE_ID = RequestHeaders.HEADER_TRACE_MESSAGE_ID;
    public static final String HEADER_TIMESTAMP = RequestHeaders.HEADER_TIMESTAMP;
    public static final String HEADER_TRANSACTION_ID = RequestHeaders.HEADER_TRANSACTION_ID;
    public static final String HEADER_TRANSACTION_TYPE = RequestHeaders.HEADER_TRANSACTION_TYPE;
    public static final String HEADER_GPE_ID = RequestHeaders.HEADER_GPE_ID;
    public static final String HEADER_BATCHNUMBER = RequestHeaders.HEADER_BATCHNUMBER;
    public static final String HEADER_TERMINAL_ID = RequestHeaders.HEADER_TERMINAL_ID;
    public static final String HEADER_OPERATOR_ID = RequestHeaders.HEADER_OPERATOR_ID;
    public static final String HEADER_REPONSE_CODE = RequestHeaders.HEADER_REPONSE_CODE;
    public static final String HEADER_MAC = RequestHeaders.HEADER_MAC;
    public static final String HEADER_GAME_TYPE_ID = RequestHeaders.HEADER_GAME_TYPE_ID;

    public static final int UNINITIAL_VALUE = -1;
    private String protocalVersion = "1.0";
    private String traceMessageId;
    private String strTimestamp; // The string representation of X-Timestamp
    private String transactionID;
    private int transType = UNINITIAL_VALUE;
    private Gpe gpe;
    private String batchNumber;
    private long terminalId = UNINITIAL_VALUE;
    private String operatorId;
    private int responseCode = UNINITIAL_VALUE;
    private String mac;
    private String gpsLocation;
    private String encrptedBody; // message body without decryption
    private String originalBody; // the original message body.
    private T model;
    private List<Command> commandList = new ArrayList<Command>();
    private String gameTypeId;
    /**
     * Whether the request is for internal administration. Some functions is only available to internal users, such as
     * payout a cancel-declined ticket. In general, all requests enter from <code>InternalTEPortServlet</code> will be
     * marked as internal call.
     */
    private boolean internalCall;
    private WorkingKey workingKey;
    // AMQP Message will be published to 3rd party system
    private MessagePack transMessage;

    // assembled object from headers
    private Transaction transaction;
    private Device device;
    private Operator operator;
    private Merchant merchant;
    // for customized properties of current transaction context.
    private Map<String, Object> properties = new HashMap<String, Object>();

    public static MLotteryContext getPropertiesLoader() {
        try {
            return MLotteryContext.getInstance();
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public String getProtocalVersion() {
        return protocalVersion;
    }

    public void setProtocalVersion(String version) {
        // MLotteryContext prop = this.getPropertiesLoader();
        // String value = prop.get(MLotteryContext.ENTRY_PROTOCAL_VERSION,
        // protocalVersion);
        // if (!(value.trim().equals(version))) {
        // throw new
        // MessageFormatException(SystemException.CODE_UNSUPPORTED_PROTOCAL_VERSION,
        // new String[] { version, value });
        // }
        this.protocalVersion = version;
    }

    public String getTraceMessageId() {
        return traceMessageId;
    }

    public void setTraceMessageId(String traceMessageId) {
        if (traceMessageId != null) {
            this.traceMessageId = traceMessageId;
        }
    }

    public String getStrTimestamp() {
        return this.strTimestamp;
    }

    public void setStrTimestamp(String strTimestamp) {
        this.strTimestamp = strTimestamp;
    }

    public Date getTimestamp() {
        return SimpleToolkit.parseDate(this.getStrTimestamp(), this.getPropertiesLoader().getTimestampFormat());
    }

    public void setTimestamp(Date timestamp) {
        this.strTimestamp = SimpleToolkit.formatDate(timestamp, this.getPropertiesLoader().getTimestampFormat());
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        if (batchNumber != null) {
            this.batchNumber = batchNumber;
        }
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setStrTransType(String strTransType) {
        try {
            this.transType = Integer.parseInt(strTransType);
            if (!TransactionType.isSupported(transType)) {
                throw new SystemException(SystemException.CODE_UNSUPPORTED_TRANSTYPE, "Unsupported transaction type:"
                        + strTransType);
            }
        } catch (NumberFormatException e) {
            throw new MessageFormatException(SystemException.CODE_UNSUPPORTED_TRANSTYPE,
                    "Wrong format of transaction type:" + strTransType);
        }
    }

    public String getStrTransType() {
        return this.transType + "";
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public Gpe getGpe() {
        return gpe;
    }

    public void setGpe(Gpe gpe) {
        this.gpe = gpe;
    }

    public void setStrTerminalId(String strTerminalId) {
        if (strTerminalId == null) {
            return;
        }
        try {
            this.terminalId = Long.parseLong(strTerminalId);
        } catch (Exception e) {
            throw new MessageFormatException(SystemException.CODE_NO_DEVICE, "Wrong terminal identifier:"
                    + strTerminalId);
        }
    }

    public long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(long terminalId) {
        this.terminalId = terminalId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        if (operatorId != null) {
            this.operatorId = operatorId;
        }
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        if (mac != null) {
            this.mac = mac;
        }
    }

    public String getEncrptedBody() {
        return encrptedBody;
    }

    public void setEncrptedBody(String encrptedBody) {
        this.encrptedBody = encrptedBody;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    public WorkingKey getWorkingKey() {
        return workingKey;
    }

    public void setWorkingKey(WorkingKey workingKey) {
        this.workingKey = workingKey;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String getOriginalBody() {
        return originalBody;
    }

    public void setOriginalBody(String originalBody) {
        this.originalBody = originalBody;
    }

    public boolean isInternalCall() {
        return internalCall;
    }

    public void setInternalCall(boolean internalCall) {
        this.internalCall = internalCall;
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<Command> commandList) {
        this.commandList = commandList;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public MessagePack getTransMessage() {
        return transMessage;
    }

    public void setTransMessage(MessagePack transMessage) {
        this.transMessage = transMessage;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String generateAmqpRoutingKey() {
        return MessagePack.PREFIX + "." + (this.getTransaction() == null ? "_" : this.getTransaction().getType()) + "."
                + (this.getGameTypeId() == null ? "_" : this.getGameTypeId());
    }

    @Override
    public Object clone() {
        try {
            Context t = (Context) super.clone();
            // deep clone... while all cloned tickets should associate with same
            // transaction.
            if (this.getTransaction() != null) {
                t.setTransaction((Transaction) this.getTransaction().clone());
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Assemble a mac input string according to 'Te Transaction Interface Definition'.
     */
    public String getMacString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Protocal-Version:").append(this.protocalVersion != null ? this.protocalVersion : "");
        buffer.append("|").append("GPE_Id:").append(this.gpe != null ? this.gpe.getId() : "");
        buffer.append("|").append("Terminal-Id:").append(this.terminalId != UNINITIAL_VALUE ? this.terminalId : "");
        buffer.append("|").append("Operator-Id:").append(this.operatorId != null ? this.operatorId : "");
        buffer.append("|").append("Trans-BatchNumber:").append(this.batchNumber != null ? this.batchNumber : "");
        buffer.append("|").append("TractMsg-Id:").append(this.traceMessageId != null ? this.traceMessageId : "");
        buffer.append("|").append("Timestamp:").append(this.strTimestamp != null ? this.strTimestamp : "");
        buffer.append("|").append("Transaction-Type:")
                .append(this.transType != UNINITIAL_VALUE ? (this.transType + "") : "");
        buffer.append("|").append("Transaction-Id:").append(this.transactionID != null ? this.transactionID : "");
        buffer.append("|").append("Response-Code:")
                .append(this.responseCode != UNINITIAL_VALUE ? (this.responseCode + "") : "");
        buffer.append("|").append(this.originalBody != null ? this.originalBody : "");
        return buffer.toString();
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    /**
     * Set the game type by a string representation.
     */
    public void setGameTypeId(String gameTypeId) {
        if (gameTypeId != null) {
            this.gameTypeId = gameTypeId;
            try {
                GameType.fromType(Integer.parseInt(gameTypeId.trim()));
            } catch (NumberFormatException e) {
                throw new SystemException(SystemException.CODE_UNSUPPORTED_TRANSTYPE, "Unsupported game type:"
                        + gameTypeId);
            }
        }
    }

    public int getGameTypeIdIntValue() {
        if (gameTypeId != null) {
            return Integer.parseInt(gameTypeId.trim());
        } else {
            return Game.TYPE_UNDEF;
        }
    }

}
