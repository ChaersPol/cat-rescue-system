package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 提醒推迟结果
 *
 * 除「推迟成功与否」之外，还要告诉前端这次有没有顺带把
 * 疫苗台账 / 驱虫台账的计划日期一起改掉，改了是哪一条。
 *
 * @author ruoyi
 */
public class CatReminderDeferResult
{
    /** 影响条数，0 表示推迟失败 */
    private int rows;
    /** 推迟后的提醒日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date newDate;
    /** 同步说明（为空表示本次未同步业务台账） */
    private String syncTip;
    /** 同步是否成功（false 表示该同步但没找到对应记录） */
    private Boolean synced;

    public int getRows()
    {
        return rows;
    }
    public void setRows(int rows)
    {
        this.rows = rows;
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
