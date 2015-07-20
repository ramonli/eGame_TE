package com.mpos.lottery.te.sequence.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SequenceUnique {
    private static Log logger = LogFactory.getLog(SequenceUnique.class);
    private static SequenceUnique unique;
    private Map<String, Sequence> map = new HashMap<String, Sequence>();

    private SequenceUnique() {
    }

    public synchronized static SequenceUnique getIntance() {
        if (unique == null) {
            unique = new SequenceUnique();
        }
        return unique;
    }

    /**
     * Check whether this operation retrieves a duplicated sequence range. If the current value of new retrieved
     * sequence is between the range of old sequence, then it means we retrieved a overlaped sequence, just retrieve new
     * one and perform this checking.
     */
    public boolean isDulplicate(Sequence sequence) {
        Sequence seq = map.get(sequence.getName());
        if (seq != null) {
            BigInteger nextMin = sequence.getNextMin();
            if (nextMin.compareTo(seq.getNextMin()) >= 0 && nextMin.compareTo(seq.getNextMax()) <= 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The retrieved sequence(nextMin" + sequence.getNextMin() + ",nextMax="
                            + sequence.getNextMax() + ") is overlaped with original Sequence(nextMin="
                            + seq.getNextMin() + ",nextMax=" + seq.getNextMax() + "), should retrieve new sequence!");
                }
                return true;
            }
        }
        // update map
        map.put(sequence.getName(), sequence);
        return false;
    }
}
