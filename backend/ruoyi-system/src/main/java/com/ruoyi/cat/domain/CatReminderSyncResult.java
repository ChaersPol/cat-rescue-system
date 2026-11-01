package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 台账操作（改期 / 删除）反向同步提醒的结果
 *
 * 与「提醒推迟 → 台账跟着改」相反：这里是台账先改，提醒要跟着改。
 * 返回给前端的不只是成功与否，还要说明顺带动了几条提醒，
 * 否则用户改完台账根本不知道提醒也被改了。
 *
 * @author ruoyi
 */
public class CatReminderSyncResult
{
    /** 台账本身影响的行数，0 表示台账写入失败 */
    private int rows;
    /** 被同步改期的提醒条数 */
    private int syncCount;
    /** 同步后的提醒日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date newDate;
    /** 同步说明（为空表示本次没有动提醒，前端维持原提示） */
    private String syncTip;
    /** 是否同步成功 */
    private Boolean synced;

    public int getRows()
    {
        return rows;
    }
    public void setRows(int rows)
    {
        this.rows = rows;
    }
    public int getSyncCount()
    {
        return syncCount;
    }
    public void setSyncCount(int syncCount)
    {
        this.syncCount = syncCount;
    }
    public Date getNewDate()
    {
        return newDate;
    }
    public void setNewDate(Date newDate)
    {
        this.newDate = newDate;
    }
    public String getSyncTip()
    {
        return syncTip;
    }
    public void setSyncTip(String syncTip)
    {
        this.syncTip = syncTip;
    }
    public Boolean getSynced()
    {
        return synced;
    }
    public void setSynced(Boolean synced)
    {
        this.synced = synced;
    }
}
