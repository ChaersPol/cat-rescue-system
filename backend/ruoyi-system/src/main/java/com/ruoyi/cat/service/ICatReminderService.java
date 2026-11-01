package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.domain.CatReminderDeferResult;

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

    /**
     * 推迟提醒：把提醒日期顺延 days 天，并清空已读记录让它重新提醒
     *
     * 疫苗 / 驱虫到期的提醒，若 syncPlanDate 为 true，会同时把
     * 疫苗台账的「下次接种」、驱虫台账的「下次」日期顺延到同一天，
     * 否则两处日期对不上，看台账会误判成已过期。
     *
     * @param reminderId   提醒 id
     * @param days         顺延天数（1~365）
     * @param syncPlanDate 是否同步顺延业务台账的计划日期
     * @return 推迟结果（含新的提醒日期与同步说明），rows 为 0 表示推迟失败
     */
    public CatReminderDeferResult defer(Long reminderId, int days, boolean syncPlanDate);

    /**
     * 读取配置 reminder.defer.default.days，缺失时返回 7
     */
    public int selectDefaultDeferDays();
}
