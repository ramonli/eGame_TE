package com.mpos.lottery.te.sequence.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.math.BigInteger;

// @Entity(name="TE_SEQUENCE")
public class Sequence {
    public static String MAX_SERIAL_NO = "";
    public static String MAX_TRANS_ID = "";

    public static final String NAME_TICKETSERIALNO = "SEQ.SERIALNO";
    public static final String NAME_GENERAL = "SEQ.GENERAL";
    public static final String NAME_REFERENCE_NO = "SEQ.REFNO";

    // @Id
    // @Column(name="ID")
    private int id;

    // @Column(name="SEQ_NAME")
    private String name; // the name of sequence

    // @Column(name="MINVALUE")
    private BigInteger minValue;

    // @Column(name="MAXVALUE")
    private BigInteger maxValue;

    // @Column(name="INTERVAL")
    private BigInteger interval;

    // @Column(name="NEXTMIN")
    private BigInteger nextMin;

    // nextMax = nextMin + interval - 1;
    // @Column(name="NEXTMAX")
    private BigInteger nextMax;

    // @Column(name="ISCYCLE")
    private boolean isCycle;

    // @Transient
    private BigInteger currentValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigInteger getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigInteger maxValue) {
        this.maxValue = maxValue;
        // this.setNextMax(maxValue);
    }

    public BigInteger getMinValue() {
        return minValue;
    }

    public void setMinValue(BigInteger minValue) {
        this.minValue = minValue;
        // this.setNextMin(minValue);
    }

    public BigInteger getInterval() {
        return interval;
    }

    public void setInterval(BigInteger interval) {
        this.interval = interval;
    }

    public BigInteger getNextMin() {
        return nextMin;
    }

    public void setNextMin(BigInteger nextMin) {
        this.nextMin = nextMin;
    }

    public BigInteger getNextMax() {
        return nextMax;
    }

    public void setNextMax(BigInteger nextMax) {
        this.nextMax = nextMax;
    }

    public boolean isCycle() {
        return isCycle;
    }

    public void setCycle(boolean isCycle) {
        this.isCycle = isCycle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the current sequence value. if nextMin=1,nextMax=3,then 1,2,3 will be returned. if (nextMin == nextMax), then
     * only 1 sequence will be returned.
     * 
     * @return a sequence, or null if current value has reached the end of sequence range.
     */
    public BigInteger getCurrentValue() {
        if (currentValue == null) {
            currentValue = nextMin.subtract(BigInteger.ONE);
        }
        currentValue = currentValue.add(BigInteger.ONE);
        if (currentValue.compareTo(this.nextMax) > 0) {
            return null;
        }
        return currentValue;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
