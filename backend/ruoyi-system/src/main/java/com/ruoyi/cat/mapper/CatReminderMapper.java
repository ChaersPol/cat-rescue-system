package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatReminder;

/**
 * 提醒通知 数据层
 *
 * @author ruoyi
 */
public interface CatReminderMapper
{
    public CatReminder selectCatReminderById(Long reminderId);

    /**
     * 查询提醒列表（携带当前用户的已读状态，并按目标用户过滤）
     */
    public List<CatReminder> selectCatReminderList(CatReminder reminderId);

    /**
     * 按业务键统计已生成过的提醒数量，供定时任务判重使用
     * 不区分已读状态，避免「有人读过就被重复生成」
     */
    public int selectCountByBizKey(CatReminder reminderId);

    /**
     * 查询顶栏铃铛列表（当前用户未读优先，按提醒日期倒序）
     */
    public List<CatReminder> selectHeaderReminders(@Param("currentUserId") Long currentUserId,
                                                   @Param("currentUserName") String currentUserName,
                                                   @Param("scopeAll") boolean scopeAll,
                                                   @Param("limit") int limit);

    /**
     * 查询当前用户的未读提醒数量
     */
    public int selectUnreadCount(@Param("currentUserId") Long currentUserId,
                                 @Param("currentUserName") String currentUserName,
                                 @Param("scopeAll") boolean scopeAll);

    public int insertCatReminder(CatReminder reminderId);

    public int updateCatReminder(CatReminder reminderId);

    public int deleteCatReminderById(Long reminderId);

    public int deleteCatReminderByIds(Long[] reminderIds);

    /**
     * 记录某用户已读某条提醒
     */
    public int insertReminderRead(@Param("reminderId") Long reminderId, @Param("userId") Long userId);

    /**
     * 批量记录某用户的已读提醒
     */
    public int insertReminderReadBatch(@Param("userId") Long userId, @Param("reminderIds") Long[] reminderIds);

    /**
     * 查询某用户全部未读提醒id（用于一键全部已读）
     */
    public List<Long> selectUnreadIds(@Param("currentUserId") Long currentUserId,
                                      @Param("currentUserName") String currentUserName,
                                      @Param("scopeAll") boolean scopeAll);

    /**
     * 删除提醒时清理对应已读记录
     */
    public int deleteReminderReadByReminderIds(Long[] reminderIds);

    /**
     * 推迟提醒：重置提醒日期，首次推迟时记录原始日期，推迟次数 +1
     */
    public int deferReminder(@Param("reminderId") Long reminderId,
                             @Param("remindDate") java.util.Date remindDate,
                             @Param("originDate") java.util.Date originDate);

    /**
     * 补齐历史提醒的来源业务记录（仅在 source_id 为空时写入）
     */
    public int updateReminderSource(@Param("reminderId") Long reminderId,
                                    @Param("sourceType") String sourceType,
                                    @Param("sourceId") Long sourceId);

    /**
     * 清空某条提醒的全部已读记录（推迟后让所有人重新看到）
     */
    public int deleteReminderReadByReminderId(Long reminderId);

    /**
     * 按岗位名称查询启用用户的登录名，用于指派提醒收件人
     */
    public List<String> selectUserNamesByPostNames(@Param("postNames") List<String> postNames);

    /**
     * 查询某条业务记录（疫苗/驱虫）对应的全部提醒
     * 台账改了「下次接种 / 下次驱虫」时，按这个把提醒日期一起改掉
     */
    public List<CatReminder> selectRemindersBySource(@Param("sourceType") String sourceType,
                                                     @Param("sourceId") Long sourceId);

    /**
     * 历史提醒没有来源 id 时，按「同一只猫 + 同类型 + 原提醒日期」兜底描述一条
     */
    public List<CatReminder> selectRemindersByCatAndDate(@Param("catId") Long catId,
                                                         @Param("reminderType") String reminderType,
                                                         @Param("remindDate") java.util.Date remindDate);

    /**
     * 台账改期后同步提醒日期：只动 remind_date，保留推迟次数与原计划日期
     */
    public int updateRemindDate(@Param("reminderId") Long reminderId,
                                @Param("remindDate") java.util.Date remindDate);

    /**
     * 台账记录被删除时，查询其关联提醒id（用于先清已读记录再删提醒）
     */
    public List<Long> selectReminderIdsBySourceIds(@Param("sourceType") String sourceType,
                                                   @Param("sourceIds") Long[] sourceIds);

    /**
     * 台账记录被删除时，连带删除其关联提醒
     */
    public int deleteRemindersBySourceIds(@Param("sourceType") String sourceType,
                                          @Param("sourceIds") Long[] sourceIds);
}
