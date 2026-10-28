package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 提醒通知表
 * 
 * @author ruoyi
 */
public class CatReminder extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 提醒id */
    @Excel(name = "提醒ID", cellType = ColumnType.NUMERIC)
    private Long reminderId;
    /** 关联猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 提醒类型 */
    @Excel(name = "提醒类型", dictType = "cat_reminder_type")
    private String reminderType;
    /** 提醒标题 */
    @Excel(name = "提醒标题")
    private String title;
    /** 提醒内容 */
    @Excel(name = "提醒内容")
    private String content;
    /** 提醒日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "提醒日期", dateFormat = "yyyy-MM-dd")
    private Date remindDate;
    /** 状态（0未读 1已读） */
    @Excel(name = "状态", dictType = "cat_reminder_status")
    private String status;
    /** 目标用户（逗号分隔的登录名，为空表示全员可见） */
    @Excel(name = "目标用户")
    private String targetUser;
    /** 已读时间 */
    @Excel(name = "已读时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    /** 当前登录用户id（非表字段，用于按人取已读状态） */
    @JsonIgnore
    private Long currentUserId;
    /** 当前登录用户登录名（非表字段，用于目标用户匹配） */
    @JsonIgnore
    private String currentUserName;
    /** 是否不受目标用户限制（非表字段，管理员为 true） */
    @JsonIgnore
    private Boolean scopeAll;

    public Long getCurrentUserId()
    {
        return currentUserId;
    }
    public void setCurrentUserId(Long currentUserId)
    {
        this.currentUserId = currentUserId;
    }
    public String getCurrentUserName()
    {
        return currentUserName;
    }
    public void setCurrentUserName(String currentUserName)
    {
        this.currentUserName = currentUserName;
    }
    public Boolean getScopeAll()
    {
        return scopeAll;
    }
    public void setScopeAll(Boolean scopeAll)
    {
        this.scopeAll = scopeAll;
    }
    public Long getReminderId()
    {
        return reminderId;
    }
    public void setReminderId(Long reminderId)
    {
        this.reminderId = reminderId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public String getCatRefName()
    {
        return catRefName;
    }
    public void setCatRefName(String catRefName)
    {
        this.catRefName = catRefName;
    }
    public String getReminderType()
    {
        return reminderType;
    }
    public void setReminderType(String reminderType)
    {
        this.reminderType = reminderType;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }
    public Date getRemindDate()
    {
        return remindDate;
    }
    public void setRemindDate(Date remindDate)
    {
        this.remindDate = remindDate;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getTargetUser()
    {
        return targetUser;
    }
    public void setTargetUser(String targetUser)
    {
        this.targetUser = targetUser;
    }
    public Date getReadTime()
    {
        return readTime;
    }
    public void setReadTime(Date readTime)
    {
        this.readTime = readTime;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("reminderId", getReminderId())
            .append("catId", getCatId())
            .append("reminderType", getReminderType())
            .append("title", getTitle())
            .append("content", getContent())
            .append("remindDate", getRemindDate())
            .append("status", getStatus())
            .append("targetUser", getTargetUser())
            .append("readTime", getReadTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
