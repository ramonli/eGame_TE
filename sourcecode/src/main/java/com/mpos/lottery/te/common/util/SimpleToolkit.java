package com.mpos.lottery.te.common.util;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;

import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SimpleToolkit {

    /**
     * Get the root cause of a exception.
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        } else {
            return getRootCause(cause);
        }
    }

    /**
     * Check whether a int array contain duplicated values.
     */
    public static boolean containRepeatedly(int[] intarray) {
        for (int i = 0; i < intarray.length; i++) {
            for (int j = i + 1; j < intarray.length; j++) {
                if (intarray[i] == intarray[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether the 2 time are in same day.
     */
    public static boolean isSameDay(Date beginTime, Date endTime) {
        Assert.notNull(beginTime);
        Assert.notNull(endTime);
        if (formatDate(beginTime).equalsIgnoreCase(formatDate(endTime))) {
            return true;
        }
        return false;
    }

    /**
     * If a selected number is started with '0', remove it. For example if selected number is '01,20,10,09', will be
     * formatted to '1,20,10,9'.
     */
    public static String formatNumericString(String input, String delimeter) {
        int[] intArray = string2IntArray(input, delimeter, false);
        StringBuffer formattedSelectedNumber = new StringBuffer("");
        for (int i = 0; i < intArray.length; i++) {
            if (i != 0) {
                formattedSelectedNumber.append(delimeter);
            }
            formattedSelectedNumber.append(intArray[i]);
        }
        return formattedSelectedNumber.toString();
    }

    /**
     * Convert a number string into number array. for example, convert a string '09,02,23,19' into [2,9,19,23] if needs
     * to sorting, otherwise [9,2,23,19]
     */
    public static int[] string2IntArray(String numberPart, String numberDelemeter, boolean sortArray) {
        if (numberPart == null) {
            return null;
        }
        String[] strNumbers = numberPart.split(numberDelemeter);
        int[] numbers = new int[strNumbers.length];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.parseInt(strNumbers[i]);
        }
        if (sortArray) {
            // Sorts the specified array of integers into ascending numerical
            // order.
            Arrays.sort(numbers);
        }
        return numbers;
    }

    /**
     * Math divide.
     */
    public static BigDecimal mathDivide(BigDecimal denominator, BigDecimal numerator) {
        if (denominator == null || numerator == null) {
            throw new IllegalArgumentException("niether denominator nor numerator can be null");
        }
        return denominator.divide(numerator, 2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mathDivide(BigDecimal denominator, BigDecimal numerator, int precision) {
        if (denominator == null || numerator == null) {
            throw new IllegalArgumentException("niether denominator nor numerator can be null");
        }
        return denominator.divide(numerator, precision, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Math multiple.
     */
    public static BigDecimal mathMultiple(BigDecimal m1, BigDecimal m2) {
        if (m1 == null || m2 == null) {
            throw new IllegalArgumentException("niether denominator nor numerator can be null");
        }
        BigDecimal value = m1.multiply(m2);
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal mathMultiple(BigDecimal m1, BigDecimal m2, int percision) {
        if (m1 == null || m2 == null) {
            throw new IllegalArgumentException("niether denominator nor numerator can be null");
        }
        BigDecimal value = m1.multiply(m2);
        return value.setScale(percision, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * For example, the input is '12', padding char is '0' and expected length is 10, then the output will be
     * '0000000012'.
     */
    public static String fillLeft(int expectedLength, char paddingChar, String src) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < expectedLength; i++) {
            buffer.append(paddingChar);
        }
        return buffer.append(src).toString().substring(src.length());
    }

    /**
     * calcualte the date which is <code>days</code> past since <code>startDay</code>.
     */
    public static Date dayAfter(Date startDay, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDay);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    public static String formatDate(Date date) {
        return formatDate(date, "yyyyMMddHHmmss");
    }

    /**
     * Format a date by given date format.
     */
    public static String formatDate(Date date, String pattern) {
        assert date != null : "Argument 'date' can NOT be null.";
        assert pattern != null : "Argument 'pattern' can NOT be null.";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * Build a date instance from given date string.
     */
    public static Date parseDate(String input, String pattern) {
        assert input != null : "Argument 'input' can NOT be null.";
        assert pattern != null : "Argument 'pattern' can NOT be null.";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(input);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Get random 32 uuid string, all '-' will be removed.
     */
    public static String simpleUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    /**
     * Generate MD5 digest.
     */
    public static String md5(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String encoding = MLotteryContext.getInstance().getDefaultEncoding();
            byte[] outputs = md5.digest(input.getBytes(encoding));
            return HexCoder.bufferToHex(outputs);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * calculate the time between twoe dates.
     * 
     * @return millseconds.
     */
    public static long compare(Date d1, Date d2) {
        return d1.getTime() - d2.getTime();
    }

    /**
     * Translate a string into long list.
     * 
     * @param input
     *            A string composed of digital and delimiter, for example '2,12,23'.
     * @param delimiter
     *            The delimiter which used to separate digits.
     */
    public static List<Long> splitToLong(String input, String delimiter) {
        String[] slices = input.split(delimiter);
        List result = new ArrayList();
        for (int i = 0; i < slices.length; i++) {
            if (!"".equals(slices[i])) {
                result.add(Long.parseLong(slices[i]));
            }
        }
        return result;
    }

    /**
     * Try {@link org.apache.commons.lang.StringUtils#split(String, char)}
     */
    public static List<String> split(String source, String delim) {
        List<String> segList = new ArrayList<String>();
        int start = 0;
        int delimIdx = source.indexOf(delim);
        while (delimIdx != -1) {
            segList.add(source.substring(start, delimIdx));
            start = delimIdx + delim.length();
            delimIdx = source.indexOf(delim, start);
        }
        segList.add(source.substring(start, source.length()));
        return segList;
    }

    public static int generatePin(int min, int max) {
        Random generator = new Random();
        return (max - min - 1) + generator.nextInt(min);
    }

    /**
     * Generate a 32-length uuid based on UUID.
     */
    public static String uuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "");
    }

    /**
     * Join a array into string.
     */
    public static String join(List<?> list, String join) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            buffer.append(list.get(i));
            if (i != (list.size() - 1)) {
                buffer.append(join);
            }
        }
        return buffer.toString();
    }

    /**
     * Join the numbers by supplied joining char.
     * 
     * @param numbers
     *            The source numbers.
     * @param indexOfBegin
     *            The index on begin element, it starts with 0.
     * @param count
     *            How many elements will be joined.
     * @param joinChar
     *            The joining char.
     * @return a string generated from supplied numbers, such as '2,4,3'
     */
    public static String join(int[] numbers, int indexOfBegin, int count, String joinChar) {
        Assert.state(indexOfBegin >= 0, "index of being can't be less than 0");
        Assert.state(count > 0, "count must be greater than 0");
        if (numbers.length - indexOfBegin < count) {
            throw new IndexOutOfBoundsException("indexOfBegin:" + indexOfBegin + ",sizeOfArray:" + numbers.length
                    + ", required count:" + count);
        }
        int indexOfCount = 0;
        StringBuffer buffer = new StringBuffer("");
        for (int i = indexOfBegin; i < numbers.length; i++) {
            buffer.append(numbers[i]);
            if (indexOfCount < count - 1) {
                buffer.append(joinChar);
            }
            if (indexOfCount == count - 1) {
                break;
            }
            indexOfCount++;
        }
        return buffer.toString();
    }

    // test
    public static void main(String[] args) {
        System.out.println(0 - 32.03);
        System.out.println(new BigDecimal(String.valueOf((0 - 32.03))));
    }
}
