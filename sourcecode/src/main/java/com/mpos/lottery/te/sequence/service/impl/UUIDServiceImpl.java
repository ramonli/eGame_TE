package com.mpos.lottery.te.sequence.service.impl;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.sequence.domain.Sequence;
import com.mpos.lottery.te.sequence.domain.SequenceUnique;
import com.mpos.lottery.te.sequence.domain.TicketSerialSpec;
import com.mpos.lottery.te.sequence.service.SequenceManager;
import com.mpos.lottery.te.sequence.service.UUIDService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UUIDServiceImpl implements UUIDService {
    private Log logger = LogFactory.getLog(UUIDServiceImpl.class);
    private Map<String, Sequence> sequenceMap = new HashMap<String, Sequence>(0);
    private String dateFormat = "yyMMdd";
    private SequenceManager sequenceService;

    /**
     * Generate generatel transaction id.
     */
    public synchronized String getGeneralID() throws ApplicationException {
        String timestamp = SimpleToolkit.formatDate(new Date(), dateFormat);
        BigInteger sequence = this.retrieveCurrentSeq(Sequence.NAME_GENERAL);
        String maxAllowedSeq = MLotteryContext.getInstance().get(Sequence.NAME_GENERAL.toLowerCase() + ".max");
        String seqPrefix = MLotteryContext.getInstance().get("seq.prefix");
        StringBuffer buffer = new StringBuffer().append(fillZero(2, seqPrefix)).append(timestamp)
                .append(this.fillZero(maxAllowedSeq.length(), sequence.toString()));
        return buffer.toString();
    }

    /**
     * Generate ticket serial No.
     */
    public synchronized String getTicketSerialNo(int saleMode, int gameType) throws ApplicationException {
        String serialNo = this.doGenerateSerialNo(saleMode, gameType, new Date(), Sequence.NAME_TICKETSERIALNO);
        if (logger.isDebugEnabled()) {
            logger.debug("Assemble a new ticket serial number:" + serialNo);
        }
        return serialNo;
    }

    public synchronized String getTicketSerialNo(int gameType) throws ApplicationException {
        return this.getTicketSerialNo(TicketSerialSpec.ONLINE_MODE, gameType);
    }

    /**
     * Generate reference No.
     */
    public synchronized String getReferenceNo(int saleMode) throws ApplicationException {
        Date timestamp = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        int secondOfMinute = calendar.get(Calendar.SECOND);
        String referenceNo = this.doGenerateSerialNo(saleMode, secondOfMinute, new Date(), Sequence.NAME_REFERENCE_NO);
        if (logger.isDebugEnabled()) {
            logger.debug("Assemble a new reference No.:" + referenceNo);
        }
        return referenceNo;
    }

    @Override
    public void reset(String sequenceName) throws ApplicationException {
        if (sequenceName == null) {
            // reset all sequences
            this.sequenceMap.clear();
        } else {
            this.sequenceMap.remove(sequenceName);
        }
    }

    @Override
    public BigInteger retrieveCurrentSeq(String name) throws ApplicationException {
        BigInteger currentValue = null;
        Sequence seq = sequenceMap.get(name);
        if (seq != null) {
            currentValue = seq.getCurrentValue();
            if (currentValue != null) {
                return currentValue;
            }
        }
        // retrieve new sequence from database
        seq = this.getSequenceService().fetchNewSequence(name);
        try {
            this.verifySequence(seq, MLotteryContext.getInstance().get(name.toLowerCase() + ".max"));
        } catch (SystemException e) {
            logger.warn(e.getMessage());
            // retrieve the sequence again...as the system will adjust illegal
            // setting automatically
            seq = this.getSequenceService().fetchNewSequence(name);
        }
        // check whether the sequence is duplicated
        for (int i = 0; SequenceUnique.getIntance().isDulplicate(seq); i++) {
            // retrieve new sequence again
            seq = this.getSequenceService().fetchNewSequence(name);
            if (i >= 5) {
                throw new SystemException("Tried the fifth time, can NOT find valid Sequence!");
            }
        }

        sequenceMap.put(name, seq);
        currentValue = seq.getCurrentValue();
        if (logger.isTraceEnabled()) {
            logger.trace("Get current sequence:" + currentValue.intValue());
        }
        return currentValue;
    }

    // --------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------

    protected String doGenerateSerialNo(int saleMode, int gameType, Date timestamp, String sequenceName)
            throws ApplicationException {
        BigInteger sequence = this.retrieveCurrentSeq(sequenceName);
        TicketSerialSpec serialSpec = new TicketSerialSpec(saleMode, timestamp, gameType, sequence);
        return serialSpec.toSerialNo();
    }

    protected void verifySequence(Sequence seq, String strMaxSequence) {
        BigInteger maxSequence = new BigInteger(strMaxSequence);
        if (maxSequence.compareTo(seq.getMinValue()) < 0) {
            throw new SystemException("The minValue of sequence[" + seq.getName() + "] is greater than "
                    + strMaxSequence + "!!!");
        }
        if (maxSequence.compareTo(seq.getMaxValue()) < 0) {
            throw new SystemException("The maxValue of sequence[" + seq.getName() + "] is greater than "
                    + strMaxSequence + "!!!");
        }
        if (maxSequence.compareTo(seq.getNextMin()) < 0) {
            throw new SystemException("The nextMin of sequence[" + seq.getName() + "] is greater than "
                    + strMaxSequence + "!!!");
        }
        if (maxSequence.compareTo(seq.getNextMax()) < 0) {
            throw new SystemException("The nextMax of sequence[" + seq.getName() + "] is greater than "
                    + strMaxSequence + "!!!");
        }
        if (seq.getMinValue().compareTo(seq.getMaxValue()) >= 0) {
            throw new SystemException("The minValue(" + seq.getMinValue() + ") of sequence[" + seq.getName()
                    + "] must be less than maxValue(" + seq.getMaxValue() + ")!!!");
        }
        if (seq.getNextMin().compareTo(seq.getNextMax()) > 0) {
            throw new SystemException("The nextMin(" + seq.getNextMin() + ") of sequence[" + seq.getName()
                    + "] must be less than or equal with nextMax(" + seq.getNextMax() + ")!!!");
        }
        if (seq.getNextMin().compareTo(seq.getMinValue()) < 0) {
            throw new SystemException("The nextMin(" + seq.getNextMin() + ") of sequence[" + seq.getName()
                    + "] must be greater than or equal with minValue(" + seq.getMinValue() + ")!!!");
        }
        if (seq.getNextMax().compareTo(seq.getMaxValue()) > 0) {
            throw new SystemException("The nextMax(" + seq.getNextMax() + ") of sequence[" + seq.getName()
                    + "] must be less than or equal with maxValue(" + seq.getMaxValue() + ")!!!");
        }
        if (seq.getInterval().compareTo(BigInteger.ONE) < 0) {
            throw new SystemException("The interval of sequence[" + seq.getName()
                    + "] must be greater than or equals with 1 !!!");
        }
    }

    /**
     * Get a zero-filled string representation of current value.
     * 
     * @param size
     *            The length of string.
     */
    public String fillZero(int size, String input) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < size; i++) {
            buffer.append("0");
        }
        String tmp = buffer.toString() + input;
        return tmp.substring(tmp.length() - size);
    }

    // --------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------
    public SequenceManager getSequenceService() {
        return sequenceService;
    }

    public void setSequenceService(SequenceManager sequenceService) {
        this.sequenceService = sequenceService;
    }

}
