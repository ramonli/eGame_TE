package com.mpos.lottery.te.sequence.domain;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * According to the customer, the serial No. or reference No. should be added with randomness as below:
 * <p>
 * OYDHGHDYGMDMXXXXXXZZ
 * <p>
 * where
 * <ul>
 * <li>O - Online or offline mode.</li>
 * <li>YY - year</li>
 * <li>DDD - day of year</li>
 * <li>GG - game type or second in minute, it depends client is requesting serial No. or reference No.</li>
 * <li>HH - hour of day</li>
 * <li>MM - minute of hour</li>
 * <li>XXXXXX - sequential number</li>
 * <li>ZZ - random number</li>
 * </ul>
 * 
 * @author Ramon Li
 */
public class TicketSerialSpec {
    public static final int ONLINE_MODE = 0;
    public static final int OFFLINE_MODE = 1;
    public static final char PADDING = '0';
    private int lengthOfSerial = 20;
    // elements of serialNo
    private int mode;
    private String year;
    private String dayOfYear;
    private String gameTypeOrSecond;
    private String hour;
    private String minute;
    private String sequence;
    private String randomNum;

    /**
     * Construct a ticket serial number by given elements.
     * 
     * @param saleMode
     *            online or offline?
     * @param timestamp
     *            The timestamp used to generate time information in serial No.
     * @param gameTypeOrSecond
     *            the game type of a ticket.
     * @param sequence
     *            The sequential number.
     */
    public TicketSerialSpec(int saleMode, Date timestamp, int gameTypeOrSecond, BigInteger sequence) {
        this.mode = saleMode;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        this.year = (calendar.get(Calendar.YEAR) + "").substring(2);
        this.dayOfYear = SimpleToolkit.fillLeft(3, PADDING, calendar.get(Calendar.DAY_OF_YEAR) + "");
        this.gameTypeOrSecond = SimpleToolkit.fillLeft(2, PADDING, gameTypeOrSecond + "");
        this.hour = SimpleToolkit.fillLeft(2, PADDING, calendar.get(Calendar.HOUR_OF_DAY) + "");
        this.minute = SimpleToolkit.fillLeft(2, PADDING, calendar.get(Calendar.MINUTE) + "");
        this.sequence = SimpleToolkit.fillLeft(6, PADDING, sequence.toString());
        this.randomNum = SimpleToolkit.fillLeft(2, PADDING, this.calRandom() + "");
    }

    /**
     * De-construct a ticket serial number.
     */
    public TicketSerialSpec(String serialNo) throws ApplicationException {
        if (lengthOfSerial != serialNo.length()) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SERIALNO,
                    "The length of serialNo/refNo must be " + lengthOfSerial);
        }
        try {
            this.mode = Integer.parseInt("" + serialNo.charAt(0));
            this.year = "" + serialNo.charAt(1) + serialNo.charAt(7);
            this.dayOfYear = "" + serialNo.charAt(2) + serialNo.charAt(6) + serialNo.charAt(10);
            this.gameTypeOrSecond = "" + serialNo.charAt(4) + serialNo.charAt(8);
            this.hour = "" + serialNo.charAt(3) + serialNo.charAt(5);
            this.minute = "" + serialNo.charAt(9) + serialNo.charAt(11);
            this.sequence = serialNo.substring(12, 18);
            this.randomNum = serialNo.substring(18);
        } catch (Exception e) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SERIALNO, e.getMessage());
        }
    }

    public String toSerialNo() {
        // OYDHGHDYGMDMXXXXXXZZ
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.mode);
        buffer.append(this.year.charAt(0)).append(this.dayOfYear.charAt(0));
        buffer.append(this.hour.charAt(0)).append(this.gameTypeOrSecond.charAt(0));
        buffer.append(this.hour.charAt(1)).append(this.dayOfYear.charAt(1));
        buffer.append(this.year.charAt(1)).append(this.gameTypeOrSecond.charAt(1));
        buffer.append(this.minute.charAt(0)).append(this.dayOfYear.charAt(2));
        buffer.append(this.minute.charAt(1));
        buffer.append(this.sequence).append(this.randomNum);
        return buffer.toString();
    }

    public int calRandom() {
        return new Random().nextInt(99);
    }

    public int getMode() {
        return mode;
    }

    public String getYear() {
        return year;
    }

    public String getDayOfYear() {
        return dayOfYear;
    }

    public String getGameTypeOrSecond() {
        return gameTypeOrSecond;
    }

    public String getHour() {
        return hour;
    }

    public String getMinute() {
        return minute;
    }

    public String getSequence() {
        return sequence;
    }

    public String getRandomNum() {
        return randomNum;
    }

    @Override
    public String toString() {
        return "TicketSerialSpec [saleMode=" + mode + ", year=" + year + ", dayOfYear=" + dayOfYear
                + ", gameTypeOrSecond=" + gameTypeOrSecond + ", hour=" + hour + ", minute=" + minute + ", sequence="
                + sequence + ", randomNum=" + randomNum + "]";
    }

}
