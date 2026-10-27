package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.mapper.CatReminderMapper;
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
    @Autowired
    private CatReminderMapper reminderIdMapper;

    @Override
    public CatReminder selectCatReminderById(Long reminderId)
    {
        return reminderIdMapper.selectCatReminderById(reminderId);
    }

    @Override
    public List<CatReminder> selectCatReminderList(CatReminder reminderId)
    {
        return reminderIdMapper.selectCatReminderList(reminderId);
    }

    @Override
    public List<CatReminder> selectHeaderReminders(CatReminder reminderId, int limit)
    {
        return reminderIdMapper.selectHeaderReminders(
            reminderId.getCurrentUserId(),
            reminderId.getCurrentUserName(),
            Boolean.TRUE.equals(reminderId.getScopeAll()),
            limit);
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
}
