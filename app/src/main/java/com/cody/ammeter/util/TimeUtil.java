package com.cody.ammeter.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    private static final SimpleDateFormat TIME_SHORT = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA);
    private static final SimpleDateFormat TIME_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA);
    private static final SimpleDateFormat TIME_YEAR = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);

    private static String formatData(Date date, SimpleDateFormat format) {
        if (date == null) {
            return "";
        }
        return format.format(date);
    }

    public static String getDateShort(Date date) {
        return formatData(date, TIME_YEAR);
    }

    public static String getDateFormatShort(Date date) {
        return formatData(date, TIME_SHORT);
    }

    public static String getDateFormatLong(Date date) {
        return formatData(date, TIME_LONG);
    }

}
