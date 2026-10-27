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
     * 按岗位名称查询启用用户的登录名，用于指派提醒收件人
     */
    public List<String> selectUserNamesByPostNames(@Param("postNames") List<String> postNames);
}
