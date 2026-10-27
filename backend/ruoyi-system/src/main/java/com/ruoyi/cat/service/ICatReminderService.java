package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatReminder;

/**
 * 提醒通知 服务层
 *
 * @author ruoyi
 */
public interface ICatReminderService
{
    public CatReminder selectCatReminderById(Long reminderId);

    /**
     * 查询提醒列表（按当前用户的已读状态与目标用户范围过滤）
     */
    public List<CatReminder> selectCatReminderList(CatReminder reminderId);

    /**
     * 查询顶栏铃铛用的提醒列表
     */
    public List<CatReminder> selectHeaderReminders(CatReminder reminderId, int limit);

    /**
     * 查询当前用户的未读提醒数量
     */
    public int selectUnreadCount(CatReminder reminderId);

    public int insertCatReminder(CatReminder reminderId);

    public int updateCatReminder(CatReminder reminderId);

    public int deleteCatReminderByIds(Long[] reminderIds);

    /**
     * 标记某用户已读某条提醒
     */
    public int markRead(Long reminderId, Long userId);

    /**
     * 批量标记某用户已读多条提醒
     */
    public int markReadBatch(Long[] reminderIds, Long userId);

    /**
     * 某用户一键全部已读，返回标记条数
     */
    public int markAllRead(CatReminder reminderId);
}
