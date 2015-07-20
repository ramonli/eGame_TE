package com.mpos.lottery.te.common.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static final String DEFAULT_TIMEFORMAT = "yyyyMMddHHmmss";

    /**
     * Parse the day of the provided time, and calculate the begin and end of that day.
     * 
     * @param anytime
     *            Any timestamp.
     * @return a {@code Date} array, 2 element carried, the 1st is begin of day, and the 2nd is end of day.
     */
    public static Date[] getBeginAndEndOfDay(Date anytime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(anytime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Date[] result = new Date[2];
        result[0] = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        result[1] = cal.getTime();

        return result;
    }

    /**
     * Manipulate minute field of date.
     */
    public static Date addMinute(Date date, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, num);
        return calendar.getTime();
    }

    /**
     * Manipulate day field of date.
     */
    public static Date addDay(Date date, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, num);
        return calendar.getTime();
    }

    public static void main(String[] args) {
        System.out.println(addDay(new Date(), 1));
        System.out.println(addMinute(new Date(), 10));
    }
}
