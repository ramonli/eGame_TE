package com.mpos.lottery.te.sequence.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.sequence.domain.Sequence;

public interface SequenceManager {

    Sequence fetchNewSequence(String name) throws ApplicationException;
}
