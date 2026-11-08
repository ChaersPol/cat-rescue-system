package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 用药执行打卡（组级：一只猫 + 一个日期 + 一个时间段）
 * <p>
 * 完成状态记在这一级，不是单个药：助理点「完成」，该猫当天该时段的整组药一起算完成。
 * 照片、不良反应、备注同样挂在这一级（整组共用），因为实际喂药时是一次拍完的。
 *
 * @author ruoyi
 */
public class CatMedicationExec
{
    private Long execId;
    private Long catId;
    /** 用药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date useDate;
    /** 时间段 */
    private String timePeriod;
    /** 执行状态（0未完成 1已完成） */
    private String status;
    /** 执行人 */
    private String executor;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date execTime;
    /** 用药照片（逗号分隔） */
    private String photos;
    /** 照片上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date photoTime;
    /** 不良反应 */
    private String adverseReaction;
    /** 备注 */
    private String remark;
    /** 创建者（不对外展示，落库用） */
    private String createBy;
    /** 更新者（不对外展示，落库用） */
    private String updateBy;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /** 更新时间（撤回、补照片都会刷新，用来显示「什么时间改过」） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }

    public Long getExecId()
    {
        return execId;
    }

    public void setExecId(Long execId)
    {
        this.execId = execId;
    }

    public Long getCatId()
    {
        return catId;
    }

    public void setCatId(Long catId)
    {
        this.catId = catId;
    }

    public Date getUseDate()
    {
        return useDate;
    }

    public void setUseDate(Date useDate)
    {
        this.useDate = useDate;
    }

    public String getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod)
    {
        this.timePeriod = timePeriod;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public Date getExecTime()
    {
        return execTime;
    }

    public void setExecTime(Date execTime)
    {
        this.execTime = execTime;
    }

    public String getPhotos()
    {
        return photos;
    }

    public void setPhotos(String photos)
    {
        this.photos = photos;
    }

    public Date getPhotoTime()
    {
        return photoTime;
    }

    public void setPhotoTime(Date photoTime)
    {
        this.photoTime = photoTime;
    }

    public String getAdverseReaction()
    {
        return adverseReaction;
    }

    public void setAdverseReaction(String adverseReaction)
    {
        this.adverseReaction = adverseReaction;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
