package com.mpos.lottery.te.config.exception;

public class EntityNotFoundException extends SystemException {

    public EntityNotFoundException(int code, String message) {
        super(code, message);
    }
}
