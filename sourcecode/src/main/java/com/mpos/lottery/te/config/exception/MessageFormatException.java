package com.mpos.lottery.te.config.exception;

public class MessageFormatException extends SystemException {
    private static final long serialVersionUID = -6407667666750480810L;

    public MessageFormatException(String message) {
        super(SystemException.CODE_WRONG_MESSAGEBODY, message);
    }

    public MessageFormatException(int code, Object[] metaData) {
        super(code, metaData);
    }

    public MessageFormatException(int code, String message) {
        super(code, message);
    }

    public MessageFormatException(int code, String message, Throwable t) {
        super(code, message, t);
    }
}
