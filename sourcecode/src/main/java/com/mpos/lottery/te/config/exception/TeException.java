package com.mpos.lottery.te.config.exception;

public interface TeException {

    /**
     * Get error code of a exception,.
     */
    int getErrorCode();

    /**
     * Get the meta data which will be used to assemble response message.
     */
    Object[] getMetaData();
}
