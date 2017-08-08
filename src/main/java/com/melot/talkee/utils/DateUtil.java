package com.melot.talkee.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 类说明：日期操作工具类
 * <p>
 * 作者：宋建明<a href="mailto:jianming.song@melot.cn">
 * </p>
 * <p>
 * 创建日期：2014-9-2
 * </p>
 * <p>
 * 版本：V1.0
 * </p>
 * <p>
 * 修改历史：
 * </p>
 */
public class DateUtil {

    /**
     * 得到日期作为上传目录
     * 
     * @return 日期目录路径
     */
    public static String getDateName() {
        GregorianCalendar now = new GregorianCalendar();
        SimpleDateFormat fmtrq = new SimpleDateFormat("yyyyMMdd ", Locale.CHINA);
        String nowDate = fmtrq.format(now.getTime());
        return nowDate.trim();
    }

    /**
     * 获取当前时间所在天的开始时间
     * 
     * @param millis
     *            当前时间
     * @return 时间戳
     */
    public static Long getDayBeginTime(long millis) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return Long.valueOf(dateFormat.parse(
                    dateFormat.format(new Date(millis))).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取下一天 0 点
     * 
     * @param date
     *            指定的日期
     * @return 下一天 0 点日期对象
     */
    public static Date getNextDay(Date date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return new Date(
                    dateFormat.parse(dateFormat.format(date)).getTime() + 86400000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前时间所在星期的开始时间
     * 
     * @param millis
     *            当前时间
     * @return 当前时间所在星期的开始时间
     */
    public static long getWeekBeginTime(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * 获取指定日期时间指定域值
     * 
     * @param date
     *            日期
     * @param field
     *            日历字段，引用 Calendar.field
     * @return 指定域的结果
     */
    public static int getFieldOfDate(Date date, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }

    /**
     * 根据日历的规则，为给定的日历字段添加或减去指定的时间量
     * 
     * @param date
     * @param field
     *            日历字段，引用 Calendar.field
     * @param amount
     *            为字段添加的日期或时间量
     * @return 更新后的日期对象
     */
    public static Date addOnField(Date date, int field, int amount) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 转换日期对象成指定日期格式的字符串
     * 
     * @param date
     *            被转换的日期对象
     * @param pattern
     *            被转换的格式，默认 "yyyy-MM-dd"
     * @return 转换成的指定格式的日期字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        if (pattern == null || pattern.isEmpty()) {
            pattern = "yyyy-MM-dd";
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换日期对象成指定日期时间格式的字符串
     * 
     * @param date
     *            被转换的日期对象
     * @param pattern
     *            被转换的格式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return 转换成的指定格式的日期时间字符串
     */
    public static String formatDateTime(Date date, String pattern) {
        if (pattern == null || "".equals(pattern.trim())) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return formatDate(date, pattern);
    }

    /**
     * 转换指定日期格式的字符串为日期对象
     * 
     * @param dateString
     *            日期字符串
     * @param pattern
     *            日期格式，默认 "yyyy-MM-dd"
     * @return 日期对象
     */
    public static Date parseDateStringToDate(String dateString, String pattern) {
        if (dateString == null || "".equals(dateString.trim())) {
            return null;
        }
        if (pattern == null || "".equals(pattern.trim())) {
            pattern = "yyyy-MM-dd";
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换指定日期时间格式的字符串为日期对象
     * 
     * @param dateTimeString
     *            日期时间字符串
     * @param pattern
     *            日期时间格式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return 日期对象
     */
    public static Date parseDateTimeStringToDate(String dateTimeString,
            String pattern) {
        if (pattern == null || "".equals(pattern.trim())) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return parseDateStringToDate(dateTimeString, pattern);
    }

    /**
     * 转换指定日期格式的字符串为日期时间戳
     * 
     * @param dateString
     *            日期字符串
     * @param pattern
     *            日期格式，默认 "yyyy-MM-dd"
     * @return 日期时间戳
     */
    public static long parseDateStringToLong(String dateString, String pattern) {
        Date date = parseDateStringToDate(dateString, pattern);
        if (date != null) {
            return date.getTime();
        }
        return 0;
    }

    /**
     * 转换指定日期时间格式的字符串为日期时间戳
     * 
     * @param dateTimeString
     *            日期时间字符串
     * @param pattern
     *            日期时间格式，默认 "yyyy-MM-dd HH:mm:ss"
     * @return 日期时间戳
     */
    public static long parseDateTimeStringToLong(String dateTimeString,
            String pattern) {
        Date date = parseDateTimeStringToDate(dateTimeString, pattern);
        if (date != null) {
            return date.getTime();
        }
        return 0;
    }

}
