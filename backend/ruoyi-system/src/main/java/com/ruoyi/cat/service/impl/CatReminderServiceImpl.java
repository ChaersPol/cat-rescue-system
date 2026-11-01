package com.ruoyi.cat.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.domain.CatReminderDeferResult;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatReminderMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.service.ICatReminderService;

/**
 * 提醒通知 服务层实现
 *
 * 已读状态按用户独立记录在 cat_reminder_read 里，
 * 表上的 status 字段不再代表「谁读过」，只作为生成任务的历史状态保留。
 *
 * @author ruoyi
 */
@Service
public class CatReminderServiceImpl implements ICatReminderService
{
    /** 单次推迟允许的最大天数，防止误操作把提醒推到几年后 */
    private static final int MAX_DEFER_DAYS = 365;

    @Autowired
    private CatReminderMapper reminderIdMapper;

    @Autowired
    private CatConfigMapper catConfigMapper;

    @Autowired
    private CatReminderConflictService conflictService;

    @Autowired
    private CatVaccineMapper catVaccineMapper;

    @Autowired
    private CatDewormMapper catDewormMapper;

    @Override
    public CatReminder selectCatReminderById(Long reminderId)
    {
        CatReminder reminder = reminderIdMapper.selectCatReminderById(reminderId);
        conflictService.fillConflictTip(reminder);
        return reminder;
    }

    @Override
    public List<CatReminder> selectCatReminderList(CatReminder reminderId)
    {
        List<CatReminder> list = reminderIdMapper.selectCatReminderList(reminderId);
        conflictService.fillConflictTips(list);
        return list;
    }

    @Override
    public List<CatReminder> selectHeaderReminders(CatReminder reminderId, int limit)
    {
        List<CatReminder> list = reminderIdMapper.selectHeaderReminders(
            reminderId.getCurrentUserId(),
            reminderId.getCurrentUserName(),
            Boolean.TRUE.equals(reminderId.getScopeAll()),
            limit);
        conflictService.fillConflictTips(list);
        return list;
    }

    @Override
    public int selectUnreadCount(CatReminder reminderId)
    {
        return reminderIdMapper.selectUnreadCount(
            reminderId.getCurrentUserId(),
            reminderId.getCurrentUserName(),
            Boolean.TRUE.equals(reminderId.getScopeAll()));
    }

    @Override
    public int insertCatReminder(CatReminder reminderId)
    {
        return reminderIdMapper.insertCatReminder(reminderId);
    }

    @Override
    public int updateCatReminder(CatReminder reminderId)
    {
        return reminderIdMapper.updateCatReminder(reminderId);
    }

    @Override
    public int deleteCatReminderByIds(Long[] reminderIds)
    {
        if (reminderIds == null || reminderIds.length == 0)
        {
            return 0;
        }
        // 先清理已读记录，避免残留悬空数据
        reminderIdMapper.deleteReminderReadByReminderIds(reminderIds);
        return reminderIdMapper.deleteCatReminderByIds(reminderIds);
    }

    @Override
    public int markRead(Long reminderId, Long userId)
    {
        return reminderIdMapper.insertReminderRead(reminderId, userId);
    }

    @Override
    public int markReadBatch(Long[] reminderIds, Long userId)
    {
        if (reminderIds == null || reminderIds.length == 0)
        {
            return 0;
        }
        return reminderIdMapper.insertReminderReadBatch(userId, reminderIds);
    }

    @Override
    public int markAllRead(CatReminder reminderId)
    {
        List<Long> unreadIds = reminderIdMapper.selectUnreadIds(
            reminderId.getCurrentUserId(),
            reminderId.getCurrentUserName(),
            Boolean.TRUE.equals(reminderId.getScopeAll()));
        if (unreadIds == null || unreadIds.isEmpty())
        {
            return 0;
        }
        return reminderIdMapper.insertReminderReadBatch(
            reminderId.getCurrentUserId(), unreadIds.toArray(new Long[0]));
    }

    @Override
    public CatReminderDeferResult defer(Long reminderId, int days, boolean syncPlanDate)
    {
        CatReminderDeferResult result = new CatReminderDeferResult();
        if (reminderId == null || days <= 0 || days > MAX_DEFER_DAYS)
        {
            return result;
        }
        CatReminder reminder = reminderIdMapper.selectCatReminderById(reminderId);
        if (reminder == null)
        {
            return result;
        }

        // 基准日取「原提醒日期」与「今天」中较晚的一个：
        // 还没到期的从原计划日往后顺延，已经过期很久的从今天起算，避免推迟完还是过期状态
        Date today = CatReminderConflictService.today();
        Date base = reminder.getRemindDate();
        if (base == null || base.before(today))
        {
            base = today;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        c.add(Calendar.DAY_OF_MONTH, days);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date newDate = c.getTime();

        // origin_date 由 SQL 的 ifnull 保护，多次推迟始终保留最初的计划日期
        Date origin = reminder.getOriginDate() != null ? reminder.getOriginDate() : reminder.getRemindDate();
        int rows = reminderIdMapper.deferReminder(reminderId, newDate, origin);
        if (rows <= 0)
        {
            return result;
        }
        // 推迟说明事情还没处理完，清掉所有人的已读记录，让提醒重新出现在未读里
        reminderIdMapper.deleteReminderReadByReminderId(reminderId);

        result.setRows(rows);
        result.setNewDate(newDate);
        if (syncPlanDate)
        {
            syncPlanDate(reminder, newDate, origin, result);
        }
        return result;
    }

    /**
     * 把疫苗 / 驱虫台账的计划日期同步到推迟后的提醒日期。
     *
     * 不做同步的话，提醒已经顺延到 11-04，台账里的「下次接种」还写着 11-01，
     * 看台账就会以为这只猫已经过期了，两处必须保持一致。
     */
    private void syncPlanDate(CatReminder reminder, Date newDate, Date origin, CatReminderDeferResult result)
    {
        boolean isVaccine = "疫苗".equals(reminder.getReminderType());
        boolean isDeworm = "驱虫".equals(reminder.getReminderType());
        if (!isVaccine && !isDeworm)
        {
            // 回访 / 库存 / 转诊没有「计划日期」概念，不需要同步
            return;
        }
        String label = isVaccine ? "疫苗台账「下次接种」" : "驱虫台账「下次驱虫」";
        Date planDate = origin != null ? origin : reminder.getRemindDate();
        String dateText = com.ruoyi.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd", newDate);
        try
        {
            boolean ok = isVaccine
                ? updateVaccineNextDate(reminder, planDate, newDate)
                : updateDewormNextDate(reminder, planDate, newDate);
            if (ok)
            {
                result.setSynced(Boolean.TRUE);
                result.setSyncTip(label + "已同步改为 " + dateText);
            }
            else
            {
                result.setSynced(Boolean.FALSE);
                result.setSyncTip("未找到对应的" + (isVaccine ? "疫苗" : "驱虫")
                    + "记录，" + label + "日期未同步，请到台账手工核对");
            }
        }
        catch (Exception e)
        {
            result.setSynced(Boolean.FALSE);
            result.setSyncTip("提醒已推迟，但同步" + label + "日期失败，请到台账手工核对");
        }
    }

    private boolean updateVaccineNextDate(CatReminder reminder, Date planDate, Date newDate)
    {
        Long vaccineId = reminder.getSourceId() != null && "vaccine".equals(reminder.getSourceType())
            ? reminder.getSourceId()
            : findVaccineId(reminder, planDate);
        if (vaccineId == null)
        {
            return false;
        }
        CatVaccine update = new CatVaccine();
        update.setVaccineId(vaccineId);
        update.setNextDate(newDate);
        return catVaccineMapper.updateCatVaccine(update) > 0;
    }

    private boolean updateDewormNextDate(CatReminder reminder, Date planDate, Date newDate)
    {
        Long dewormId = reminder.getSourceId() != null && "deworm".equals(reminder.getSourceType())
            ? reminder.getSourceId()
            : findDewormId(reminder, planDate);
        if (dewormId == null)
        {
            return false;
        }
        CatDeworm update = new CatDeworm();
        update.setDewormId(dewormId);
        update.setNextDate(newDate);
        return catDewormMapper.updateCatDeworm(update) > 0;
    }

    /**
     * 历史提醒没有来源 id，按「同一只猫 + 原计划日期」反查并回写，
     * 后续再推迟就能直接用 source_id 定位
     */
    private Long findVaccineId(CatReminder reminder, Date planDate)
    {
        if (reminder.getCatId() == null || planDate == null)
        {
            return null;
        }
        CatVaccine query = new CatVaccine();
        query.setCatId(reminder.getCatId());
        query.setNextDate(planDate);
        List<CatVaccine> list = catVaccineMapper.selectCatVaccineList(query);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        Long vaccineId = list.get(0).getVaccineId();
        reminderIdMapper.updateReminderSource(reminder.getReminderId(), "vaccine", vaccineId);
        return vaccineId;
    }

    private Long findDewormId(CatReminder reminder, Date planDate)
    {
        if (reminder.getCatId() == null || planDate == null)
        {
            return null;
        }
        CatDeworm query = new CatDeworm();
        query.setCatId(reminder.getCatId());
        query.setNextDate(planDate);
        List<CatDeworm> list = catDewormMapper.selectCatDewormList(query);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        Long dewormId = list.get(0).getDewormId();
        reminderIdMapper.updateReminderSource(reminder.getReminderId(), "deworm", dewormId);
        return dewormId;
    }

    @Override
    public int selectDefaultDeferDays()
    {
        try
        {
            CatConfig query = new CatConfig();
            query.setConfigKey("reminder.defer.default.days");
            List<CatConfig> list = catConfigMapper.selectCatConfigList(query);
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null)
            {
                int days = Integer.parseInt(list.get(0).getConfigValue().trim());
                if (days > 0 && days <= MAX_DEFER_DAYS)
                {
                    return days;
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return 7;
    }
}
