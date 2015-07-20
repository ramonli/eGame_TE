package com.mpos.lottery.te.sequence.domain;

public class TicketSerialPattern {
    // elements of serialNo
    private int lengthOfMode;
    private int lengthOfYear;
    private int lengthOfDayOfYear;
    private int lengthOfGameTypeOrSecond;
    private int lengthOfHour;
    private int lengthOfMinute;
    private int lengthOfSequence;
    private int lengthOfRandomNum;

    public static TicketSerialPattern defaultPattern() {
        return new TicketSerialPattern(1, 2, 3, 2, 2, 2, 6, 2);
    }

    public TicketSerialPattern(int lengthOfMode, int lengthOfYear, int lengthOfDayOfYear, int lengthOfGameTypeOrSecond,
            int lengthOfHour, int lengthOfMinute, int lengthOfSequence, int lengthOfRandomNum) {
        super();
        this.lengthOfMode = lengthOfMode;
        this.lengthOfYear = lengthOfYear;
        this.lengthOfDayOfYear = lengthOfDayOfYear;
        this.lengthOfGameTypeOrSecond = lengthOfGameTypeOrSecond;
        this.lengthOfHour = lengthOfHour;
        this.lengthOfMinute = lengthOfMinute;
        this.lengthOfSequence = lengthOfSequence;
        this.lengthOfRandomNum = lengthOfRandomNum;
    }

    /**
     * Calculate the length of serial No.
     */
    public int getLengthOfSerialNo() {
        return this.getLengthOfDayOfYear() + this.getLengthOfGameTypeOrSecond() + this.getLengthOfHour()
                + this.getLengthOfMinute() + this.getLengthOfMode() + this.getLengthOfRandomNum()
                + this.getLengthOfSequence() + this.getLengthOfYear();
    }

    public int getLengthOfMode() {
        return lengthOfMode;
    }

    public int getLengthOfYear() {
        return lengthOfYear;
    }

    public int getLengthOfDayOfYear() {
        return lengthOfDayOfYear;
    }

    public int getLengthOfGameTypeOrSecond() {
        return lengthOfGameTypeOrSecond;
    }

    public int getLengthOfHour() {
        return lengthOfHour;
    }

    public int getLengthOfMinute() {
        return lengthOfMinute;
    }

    public int getLengthOfSequence() {
        return lengthOfSequence;
    }

    public int getLengthOfRandomNum() {
        return lengthOfRandomNum;
    }

}
