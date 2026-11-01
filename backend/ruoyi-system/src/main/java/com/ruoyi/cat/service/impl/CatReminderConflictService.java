package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.common.utils.DateUtils;

/**
 * 提醒的「用药冲突」判定
 *
 * 业务背景：猫咪到了该打下一针疫苗的日期，但如果它正在用药期，按兽医要求不能接种，
 * 只能往后推迟几天。所以提醒列表和疫苗列表里都要能一眼看出「这只猫在用药，先别打」。
 *
 * 判定口径：该猫最近 N 天（配置 reminder.medication.check.days，默认 7）内有用药记录，
 * 就算「在用药期间」。cat_medication 是按天打卡的流水，没有疗程结束日，
 * 因此只能用回溯窗口近似判断，窗口天数做成可配置项便于按实际情况调整。
 *
 * 注意：冲突提示只作为接口返回的非表字段（conflictTip），**不能写进 content**——
 * content 是提醒判重键（catId + 类型 + content），一旦随用药状态变化，
 * 每天都会重复生成一条新提醒。
 *
 * @author ruoyi
 */
@Service
public class CatReminderConflictService
{
    /** 判断「在用药期间」的回溯天数 */
    private static final String KEY_MEDICATION_DAYS = "reminder.medication.check.days";

    /** 只有这两类提醒需要做用药冲突判断 */
    private static final Set<String> CHECK_TYPES = new LinkedHashSet<>();

    static
    {
        CHECK_TYPES.add("疫苗");
        CHECK_TYPES.add("驱虫");
    }

    @Autowired
    private CatMedicationMapper catMedicationMapper;

    @Autowired
    private CatConfigMapper catConfigMapper;

    /**
     * 单只猫的近期用药摘要
     */
    private static class MedSummary
    {
        private final Set<String> drugs = new LinkedHashSet<>();
        private Date lastDate;

        private void merge(CatMedication m)
        {
            if (m.getDrugName() != null && !m.getDrugName().trim().isEmpty())
            {
                drugs.add(m.getDrugName().trim());
            }
            if (m.getUseDate() != null && (lastDate == null || m.getUseDate().after(lastDate)))
            {
                lastDate = m.getUseDate();
            }
        }
    }

    /**
     * 读取整数配置项，缺失时返回默认值
     */
    private int cfgInt(String key, int def)
    {
        try
        {
            CatConfig query = new CatConfig();
            query.setConfigKey(key);
            List<CatConfig> list = catConfigMapper.selectCatConfigList(query);
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null)
            {
                return Integer.parseInt(list.get(0).getConfigValue().trim());
            }
        }
        catch (Exception ignored)
        {
        }
        return def;
    }

    /**
     * 一次查出这批猫最近 N 天的用药并聚合，返回 catId → 摘要
     */
    private Map<Long, MedSummary> loadRecent(Collection<Long> catIds, int days)
    {
        Map<Long, MedSummary> result = new HashMap<>();
        if (catIds == null || catIds.isEmpty() || days <= 0)
        {
            return result;
        }
        List<CatMedication> meds;
        try
        {
            meds = catMedicationMapper.selectRecentMedicationByCatIds(new ArrayList<>(catIds), days);
        }
        catch (Exception e)
        {
            // 用药数据异常不能影响提醒/疫苗列表本身
            return result;
        }
        if (meds == null || meds.isEmpty())
        {
            return result;
        }
        for (CatMedication m : meds)
        {
            if (m.getCatId() == null)
            {
                continue;
            }
            result.computeIfAbsent(m.getCatId(), k -> new MedSummary()).merge(m);
        }
        return result;
    }

    /**
     * 拼一句提示文案
     */
    private String buildTip(MedSummary s, String action, int days)
    {
        String drugText = s.drugs.isEmpty() ? "药品未登记" : String.join("、", s.drugs);
        String dateText = s.lastDate == null ? "近日" : DateUtils.parseDateToStr("MM-dd", s.lastDate);
        return "该猫近 " + days + " 天有用药记录（" + drugText + "，最近 " + dateText + " 用药）。"
            + "用药期间不宜" + action + "。";
    }

    /**
     * 为一批提醒填充用药冲突提示（原地修改）
     */
    public void fillConflictTips(List<CatReminder> reminders)
    {
        if (reminders == null || reminders.isEmpty())
        {
            return;
        }

        // 1. 挑出需要判断的提醒，收集猫咪 id
        Set<Long> catIds = new LinkedHashSet<>();
        for (CatReminder r : reminders)
        {
            if (r.getCatId() != null && CHECK_TYPES.contains(r.getReminderType()))
            {
                catIds.add(r.getCatId());
            }
        }
        if (catIds.isEmpty())
        {
            return;
        }

        // 2. 聚合近期用药
        int days = cfgInt(KEY_MEDICATION_DAYS, 7);
        Map<Long, MedSummary> meds = loadRecent(catIds, days);
        if (meds.isEmpty())
        {
            return;
        }

        // 3. 回填提示文案
        for (CatReminder r : reminders)
        {
            if (r.getCatId() == null || !CHECK_TYPES.contains(r.getReminderType()))
            {
                continue;
            }
            MedSummary s = meds.get(r.getCatId());
            if (s == null)
            {
                continue;
            }
            String action = "疫苗".equals(r.getReminderType())
                ? "接种疫苗，请确认停药后再接种，或点「推迟」顺延提醒"
                : "进行驱虫，请确认停药后再驱虫，或点「推迟」顺延提醒";
            r.setConflictFlag(Boolean.TRUE);
            r.setConflictTip(buildTip(s, action, days));
        }
    }

    /**
     * 单条提醒也支持填充
     */
    public void fillConflictTip(CatReminder reminder)
    {
        if (reminder == null)
        {
            return;
        }
        List<CatReminder> one = new ArrayList<>(1);
        one.add(reminder);
        fillConflictTips(one);
    }

    /**
     * 批量给一组猫咪生成「当前是否在用药期间」的提示，供疫苗/驱虫列表批量展示
     *
     * @param catIds 猫咪 id 集合
     * @param action 提示里的动作词，如「接种疫苗」「进行驱虫」
     * @return catId → 提示文案，不在用药期的猫不会出现在结果里
     */
    public Map<Long, String> medicationTips(Collection<Long> catIds, String action)
    {
        Map<Long, String> tips = new HashMap<>();
        int days = cfgInt(KEY_MEDICATION_DAYS, 7);
        Map<Long, MedSummary> meds = loadRecent(catIds, days);
        for (Map.Entry<Long, MedSummary> e : meds.entrySet())
        {
            tips.put(e.getKey(), buildTip(e.getValue(), action, days));
        }
        return tips;
    }

    /**
     * 给某只猫生成「当前是否在用药期间」的一句话提示，未在用药期返回 null
     */
    public String medicationTipForCat(Long catId, String action)
    {
        if (catId == null)
        {
            return null;
        }
        List<Long> ids = new ArrayList<>(1);
        ids.add(catId);
        return medicationTips(ids, action).get(catId);
    }

    /**
     * 今天零点，用于推迟计算的基准
     */
    public static Date today()
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
