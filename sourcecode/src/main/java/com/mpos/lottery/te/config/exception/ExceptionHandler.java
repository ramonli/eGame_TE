package com.mpos.lottery.te.config.exception;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.zabbix.ZabbixSender;

import java.text.MessageFormat;

/**
 * Handle all exceptions, assemble a error message from exception.
 */
public class ExceptionHandler {
    private Throwable throwable;
    private MLotteryContext prop = MLotteryContext.getInstance();

    public ExceptionHandler(Throwable throwable) {
        assert throwable != null : "t can NOT be null.";
        this.throwable = throwable;
    }

    /**
     * Find the root cause of a exception.
     */
    public static Throwable getCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        } else {
            return getCause(cause);
        }
    }

    /**
     * Get error message which is assembled from meta data and properties file. This message can be returned to client.
     */
    public String getErrorMessage() {
        int errorCode = this.getErrorCode();
        String textPattern = prop.get(MLotteryContext.ENTRY_ERRORCODE + errorCode);
        MessageFormat mf = new MessageFormat(textPattern);
        return mf.format(this.getMetaData());
    }

    /**
     * Handle the exception. Here the main handling is notify monitoring system if internal error occured.
     */
    public void handle(Context req, Context resp) {
        // send internal error to Zabbix monitoring system
        if (SystemException.CODE_INTERNAL_SERVER_ERROR == this.getErrorCode()) {
            ZabbixSender sender = new ZabbixSender(prop.get("zabbix.server.host"), prop.getInt("zabbix.server.port"));
            sender.asyncSend(prop.get("zabbix.host.name"), prop.get("zabbix.item.name.internal_error"),
                    prop.get("zabbix.module.name"), getCause(this.throwable).getMessage());
        }
    }

    /**
     * retrieve error code from exception.
     */
    public int getErrorCode() {
        if (throwable instanceof TeException) {
            return ((TeException) throwable).getErrorCode();
        } else {
            // return default error code for other exceptions
            return SystemException.CODE_INTERNAL_SERVER_ERROR;
        }
    }

    private Object[] getMetaData() {
        if (throwable instanceof TeException) {
            return ((TeException) throwable).getMetaData();
        } else {
            // return default error code for other exceptions
            return null;
        }
    }
}
