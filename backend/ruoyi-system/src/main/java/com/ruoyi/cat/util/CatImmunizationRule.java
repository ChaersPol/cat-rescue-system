package com.ruoyi.cat.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 免疫与驱虫的周期规则
 * <p>
 * 现场口径（2026-11-05 与救助站确认）：
 * <ul>
 *   <li>基础免疫：第一针→第二针、第二针→第三针均为 <b>21 天</b></li>
 *   <li>狂犬、加强针：<b>11 个月</b>一个周期，到期进入年度提醒</li>
 *   <li>驱虫：体内、体外分开算，周期 <b>30 天或 60 天</b>，由录入人当场选择</li>
 * </ul>
 * 这些数值是前后端共用的唯一来源，前端 {@code utils/immunization.js} 里保持同一套常量，
 * 改规则时两边一起改，避免列表与接口算出来的日期对不上。
 *
 * @author ruoyi
 */
public class CatImmunizationRule
{
    /** 基础免疫针次间隔（天） */
    public static final int BASE_DOSE_INTERVAL_DAYS = 21;

    /** 狂犬 / 加强针周期（月） */
    public static final int ANNUAL_INTERVAL_MONTHS = 11;

    /** 基础免疫总针次 */
    public static final int BASE_DOSE_TOTAL = 3;

    /** 驱虫默认周期（天） */
    public static final int DEWORM_CYCLE_DAYS = 30;

    /** 驱虫长周期（天） */
    public static final int DEWORM_CYCLE_LONG_DAYS = 60;

    private CatImmunizationRule()
    {
    }

    /** 是否狂犬 / 加强针这类年度疫苗 */
    public static boolean isAnnual(String vaccineType)
    {
        if (isEmpty(vaccineType))
        {
            return false;
        }
        String t = vaccineType.trim();
        return t.contains("狂犬") || t.contains("加强");
    }

    /** 是否基础免疫（第一针 / 第二针 / 第三针） */
    public static boolean isBaseDose(String vaccineType)
    {
        if (isEmpty(vaccineType) || isAnnual(vaccineType))
        {
            return false;
        }
        return vaccineType.contains("针");
    }

    /**
     * 基础免疫针次序号：第一针=1、第二针=2、第三针=3，其它返回 0
     */
    public static int baseDoseIndex(String vaccineType)
    {
        if (!isBaseDose(vaccineType))
        {
            return 0;
        }
        if (vaccineType.contains("第一") || vaccineType.contains("1针") || vaccineType.contains("首针"))
        {
            return 1;
        }
        if (vaccineType.contains("第二") || vaccineType.contains("2针"))
        {
            return 2;
        }
        if (vaccineType.contains("第三") || vaccineType.contains("3针"))
        {
            return 3;
        }
        return 0;
    }

    /**
     * 按疫苗类型 + 本次接种日期推算「下次接种」：
     * 基础免疫 +21 天，狂犬/加强针 +11 个月；其它类型不推算（返回 null，由人工填）
     */
    public static Date nextDate(String vaccineType, Date vaccineDate)
    {
        if (vaccineDate == null)
        {
            return null;
        }
        if (isAnnual(vaccineType))
        {
            return addMonths(vaccineDate, ANNUAL_INTERVAL_MONTHS);
        }
        if (isBaseDose(vaccineType))
        {
            return addDays(vaccineDate, BASE_DOSE_INTERVAL_DAYS);
        }
        return null;
    }

    /** 驱虫下次日期 = 基准日期 + 周期天数（周期为空或非法时按 30 天） */
    public static Date dewormNextDate(Date baseDate, Integer cycleDays)
    {
        if (baseDate == null)
        {
            return null;
        }
        int days = cycleDays == null || cycleDays <= 0 ? DEWORM_CYCLE_DAYS : cycleDays;
        return addDays(baseDate, days);
    }

    public static Date addDays(Date date, int days)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        return startOfDay(c.getTime());
    }

    public static Date addMonths(Date date, int months)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, months);
        return startOfDay(c.getTime());
    }

    /** 去掉时分秒，避免「今天」和「明天」因为时分秒差异算错天数 */
    public static Date startOfDay(Date date)
    {
        if (date == null)
        {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /** 距今天还有几天（负数表示已经过期） */
    public static Integer daysLeft(Date date)
    {
        if (date == null)
        {
            return null;
        }
        long diff = startOfDay(date).getTime() - startOfDay(new Date()).getTime();
        return (int) Math.round(diff / 86400000d);
    }

    private static boolean isEmpty(String s)
    {
        return s == null || s.trim().isEmpty();
    }
}
