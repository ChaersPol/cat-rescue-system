package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.domain.CatReminderSyncResult;
import com.ruoyi.cat.mapper.CatReminderMapper;
import com.ruoyi.common.utils.DateUtils;

/**
 * 台账 → 提醒 的反向同步
 *
 * 正向同步（{@link CatReminderServiceImpl#defer}）解决的是「在提醒页推迟 → 台账跟着改」；
 * 这里解决相反的情况：有人直接在疫苗 / 驱虫台账里改了「下次接种 / 下次驱虫」，
 * 已有的提醒还停在旧日期，到点会按旧日期报，两处又对不上了。
 *
 * 只在台账的「计划日期」真的变了时才动提醒；改负责人、改备注这类操作不受影响。
 * 注意：正向同步改台账走的是 mapper，不经过 Service，所以不会绕回来重复触发本类。
 *
 * @author ruoyi
 */
@Service
public class CatReminderSyncService
{
    /** 来源业务类型：疫苗 */
    public static final String SOURCE_VACCINE = "vaccine";
    /** 来源业务类型：驱虫 */
    public static final String SOURCE_DEWORM = "deworm";

    @Autowired
    private CatReminderMapper catReminderMapper;

    /**
     * 台账计划日期变更后，把对应的提醒日期同步过去
     *
     * @param sourceType 来源业务类型 vaccine / deworm
     * @param sourceId   来源业务主键
     * @param catId      猫咪id（历史提醒回填来源时按猫匹配，可空）
     * @param oldDate    改之前的计划日期
     * @param newDate    改之后的计划日期
     * @param result     回填同步结果，为空则只同步不返回说明
     */
    public void syncFromPlan(String sourceType, Long sourceId, Long catId, Date oldDate, Date newDate,
                             CatReminderSyncResult result)
    {
        if (sourceId == null || newDate == null)
        {
            return;
        }
        String typeName = reminderTypeOf(sourceType);
        String label = labelOf(sourceType);
        if (typeName == null)
        {
            return;
        }

        List<CatReminder> reminders = catReminderMapper.selectRemindersBySource(sourceType, sourceId);
        if (reminders == null || reminders.isEmpty())
        {
            // 早期生成的提醒没有记来源，按「同猫 + 同类型 + 旧提醒日期」兜底找一次
            reminders = fallbackByCatAndDate(sourceType, sourceId, catId, typeName, oldDate);
        }
        if (reminders == null || reminders.isEmpty())
        {
            // 这条台账还没有生成过提醒，等下次生成任务按新日期生成即可，不必打扰用户
            return;
        }

        int changed = 0;
        for (CatReminder reminder : reminders)
        {
            // 日期没变就不用动；重复保存表单时不会产生多余写操作
            if (sameDay(reminder.getRemindDate(), newDate))
            {
                continue;
            }
            if (catReminderMapper.updateRemindDate(reminder.getReminderId(), newDate) > 0)
            {
                // 计划日期变了，原本已读的人需要重新看到这条提醒
                catReminderMapper.deleteReminderReadByReminderId(reminder.getReminderId());
                changed++;
            }
        }
        if (changed <= 0)
        {
            return;
        }

        String dateText = DateUtils.parseDateToStr("yyyy-MM-dd", newDate);
        if (result != null)
        {
            result.setSynced(Boolean.TRUE);
            result.setSyncCount(changed);
            result.setNewDate(newDate);
            result.setSyncTip(changed == 1
                ? label + "关联提醒日期已同步改为 " + dateText
                : label + "关联的 " + changed + " 条提醒日期已同步改为 " + dateText);
        }
    }

    /**
     * 台账记录被删除时，连带清理它的提醒，避免提醒挂在一张已经不存在的记录上
     *
     * @return 被清理的提醒条数
     */
    public int removeBySource(String sourceType, Long[] sourceIds)
    {
        if (sourceType == null || sourceIds == null || sourceIds.length == 0)
        {
            return 0;
        }
        List<Long> reminderIds = catReminderMapper.selectReminderIdsBySourceIds(sourceType, sourceIds);
        if (reminderIds == null || reminderIds.isEmpty())
        {
            return 0;
        }
        // 先清已读记录，再删提醒，避免 cat_reminder_read 里留悬空数据
        catReminderMapper.deleteReminderReadByReminderIds(reminderIds.toArray(new Long[0]));
        catReminderMapper.deleteRemindersBySourceIds(sourceType, sourceIds);
        return reminderIds.size();
    }

    /**
     * 历史提醒没记来源时，按「同一只猫 + 同类型 + 原提醒日期」找一遍，
     * 找到就顺手把来源回填，之后改期就能直接用 source_id 定位
     */
    private List<CatReminder> fallbackByCatAndDate(String sourceType, Long sourceId, Long catId,
                                                   String typeName, Date oldDate)
    {
        if (catId == null || oldDate == null)
        {
            return null;
        }
        List<CatReminder> found = catReminderMapper.selectRemindersByCatAndDate(catId, typeName, oldDate);
        if (found == null || found.isEmpty())
        {
            return null;
        }
        List<CatReminder> hit = new ArrayList<>();
        for (CatReminder reminder : found)
        {
            // 已经被别的记录占用的提醒不要抢
            if (reminder.getSourceId() != null)
            {
                continue;
            }
            catReminderMapper.updateReminderSource(reminder.getReminderId(), sourceType, sourceId);
            hit.add(reminder);
        }
        return hit.isEmpty() ? null : hit;
    }

    private String reminderTypeOf(String sourceType)
    {
        if (SOURCE_VACCINE.equals(sourceType))
        {
            return "疫苗";
        }
        if (SOURCE_DEWORM.equals(sourceType))
        {
            return "驱虫";
        }
        return null;
    }

    private String labelOf(String sourceType)
    {
        return SOURCE_VACCINE.equals(sourceType) ? "疫苗台账「下次接种」" : "驱虫台账「下次驱虫」";
    }

    /**
     * 两个日期是否同一天（库里是 date，前端传 yyyy-MM-dd，直接比字符串最稳）
     */
    private boolean sameDay(Date a, Date b)
    {
        if (a == null || b == null)
        {
            return false;
        }
        return DateUtils.parseDateToStr("yyyy-MM-dd", a).equals(DateUtils.parseDateToStr("yyyy-MM-dd", b));
    }
}
