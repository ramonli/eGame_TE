package com.mpos.lottery.te.sequence.domain;

import com.mpos.lottery.te.config.exception.SystemException;

public class NoSequenceException extends SystemException {

    public NoSequenceException(String seqName, Exception t) {
        super(CODE_INTERNAL_SERVER_ERROR, "can NOT find sequence with name=" + seqName, t);
    }
}
