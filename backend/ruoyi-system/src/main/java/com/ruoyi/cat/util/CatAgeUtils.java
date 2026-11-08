package com.ruoyi.cat.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 猫咪年龄计算工具
 *
 * <p>设计原则（2026-11-05 用户确认）：档案里<b>不再保存「当前年龄」</b>，只保存出生日期，
 * 每次查询时用当前日期实时算年龄。年龄自然增长，不需要任何定时任务。</p>
 *
 * <p>录入方式保持不变，仍然填「约 2 岁」「3 个月」「10 天」这类描述，系统结合救助日期
 * （没有救助日期就用今天）反推出出生日期；反推出来的出生日期标记为「估算」，
 * 展示时带「约」字，知道确切生日的可以直接填日期、标记为「准确」。</p>
 */
public final class CatAgeUtils
{
    /** 「2 岁」「2岁」「2.5岁」 */
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:周岁|岁)");

    /** 「3 个月」「3月」「3月龄」 */
    private static final Pattern MONTH_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:个月|月龄|月)");

    /** 「10 天」「10日」 */
    private static final Pattern DAY_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:天|日)");

    private CatAgeUtils()
    {
    }

    /**
     * 把「约 2 岁」「1 岁 2 个月」「半年」「10 天」这类描述换成出生日期的估算
     *
     * @param ageText  录入的年龄描述
     * @param baseDate 反推基准日（一般传救助日期）；为空则用今天
     * @return 估算的出生日期；解析不出任何数字时返回 null
     */
    public static LocalDate estimateBirthDate(String ageText, Date baseDate)
    {
        if (ageText == null || ageText.trim().isEmpty())
        {
            return null;
        }
        LocalDate base = baseDate == null ? LocalDate.now() : toLocalDate(baseDate);

        int totalMonths = 0;
        int totalDays = 0;
        boolean matched = false;

        // 「半年」「半个月」这类口语说法先处理，避免被「月」正则吃成 0
        if (ageText.contains("半年"))
        {
            totalMonths += 6;
            matched = true;
        }
        else if (ageText.contains("半月") || ageText.contains("半个月"))
        {
            totalDays += 15;
            matched = true;
        }

        Matcher year = YEAR_PATTERN.matcher(ageText);
        while (year.find())
        {
            totalMonths += Math.round(Float.parseFloat(year.group(1)) * 12);
            matched = true;
        }
        Matcher month = MONTH_PATTERN.matcher(ageText);
        while (month.find())
        {
            totalMonths += Math.round(Float.parseFloat(month.group(1)));
            matched = true;
        }
        Matcher day = DAY_PATTERN.matcher(ageText);
        while (day.find())
        {
            totalDays += Math.round(Float.parseFloat(day.group(1)));
            matched = true;
        }
        if (!matched)
        {
            return null;
        }
        // 出生日期不可能落到未来：年龄填得比基准日还大时，按基准日当天处理
        LocalDate birth = base.minusMonths(totalMonths).minusDays(totalDays);
        return birth.isAfter(LocalDate.now()) ? LocalDate.now() : birth;
    }

    /**
     * 按出生日期实时算年龄文案
     *
     * <p>精度到天：不足 1 个月显示「X 天」，不足 1 岁显示「X 个月」，满 1 岁显示
     * 「X 岁」或「X 岁 Y 个月」。估算出来的年龄带「约」字，准确的生日不带。</p>
     *
     * @param birthDate 出生日期
     * @param estimated 是否估算（true 时文案前缀「约」）
     * @return 年龄文案；出生日期为空返回 null（由调用方决定显示「未知」还是原始录入值）
     */
    public static String formatAge(Date birthDate, boolean estimated)
    {
        if (birthDate == null)
        {
            return null;
        }
        LocalDate birth = toLocalDate(birthDate);
        LocalDate today = LocalDate.now();
        if (birth.isAfter(today))
        {
            // 数据异常（出生日期填到了未来）时按 0 天显示，别让页面出现负数
            return (estimated ? "约" : "") + "0天";
        }
        long days = ChronoUnit.DAYS.between(birth, today);
        Period period = Period.between(birth, today);
        int months = period.getYears() * 12 + period.getMonths();
        String prefix = estimated ? "约" : "";

        if (months < 1)
        {
            return prefix + days + "天";
        }
        if (months < 12)
        {
            return prefix + months + "个月";
        }
        if (period.getMonths() == 0)
        {
            return prefix + period.getYears() + "岁";
        }
        return prefix + period.getYears() + "岁" + period.getMonths() + "个月";
    }

    /**
     * 出生至今的整月数（年龄区间筛选用，和 SQL 里的 timestampdiff(month, ...) 口径一致）
     *
     * @return 月数；出生日期为空返回 null
     */
    public static Integer monthsBetween(Date birthDate)
    {
        if (birthDate == null)
        {
            return null;
        }
        LocalDate birth = toLocalDate(birthDate);
        LocalDate today = LocalDate.now();
        if (birth.isAfter(today))
        {
            return 0;
        }
        Period period = Period.between(birth, today);
        return period.getYears() * 12 + period.getMonths();
    }

    /**
     * 是否已满指定岁数（「满 7 岁」这类判断用，按整年算，不看月日）
     */
    public static boolean isAtLeastYears(Date birthDate, int years)
    {
        if (birthDate == null)
        {
            return false;
        }
        return !toLocalDate(birthDate).plusYears(years).isAfter(LocalDate.now());
    }

    /**
     * Date → LocalDate，统一按服务器时区换算，避免跨时区差一天
     */
    public static LocalDate toLocalDate(Date date)
    {
        // 用毫秒数换算：MyBatis 映射 date 列有时给出 java.sql.Date，它重写了 toInstant() 会直接抛异常
        return java.time.Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
