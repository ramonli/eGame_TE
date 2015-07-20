package com.mpos.lottery.te.common.http;

import com.mpos.lottery.te.config.exception.SystemException;

public class RemoteServiceException extends SystemException {
    private static final long serialVersionUID = -2899990759704226267L;

    public RemoteServiceException(Exception cause) {
        super(cause);
    }

}
